package com.errymaricha.dafydiobooth.ui.booth.external

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.util.Log
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

class PtpSession(
    private val connection: UsbDeviceConnection,
    private val bulkIn: UsbEndpoint,
    private val bulkOut: UsbEndpoint,
) {
    private val commandLock = Any()
    data class ObjectInfo(
        val handle: Int,
        val objectFormat: Int,
        val compressedSize: Long,
    ) {
        val isJpeg: Boolean
            get() = objectFormat == 0x3801 || objectFormat == 0x3808
    }

    private var transactionId: Int = 1
    private var opened: Boolean = false
    private var liveViewStarted: Boolean = false
    private var evfMissCount: Int = 0

    private fun nextTransactionId(): Int = transactionId++

    private fun bulkWrite(data: ByteArray, timeoutMs: Int = 3000): Boolean {
        return connection.bulkTransfer(bulkOut, data, data.size, timeoutMs) == data.size
    }

    private fun bulkRead(bufferSize: Int = 64 * 1024, timeoutMs: Int = 5000): ByteArray? {
        val buffer = ByteArray(bufferSize)
        val read = connection.bulkTransfer(bulkIn, buffer, buffer.size, timeoutMs)
        if (read <= 0) return null
        return buffer.copyOf(read)
    }

    fun openSession(): Boolean {
        synchronized(commandLock) {
            if (opened) return true
            val packet = PtpPacket.buildCommand(
                code = PtpPacket.OC_OPEN_SESSION,
                transactionId = nextTransactionId(),
                params = intArrayOf(1),
            )
            if (!bulkWrite(packet)) return false
            val responseBytes = bulkRead() ?: return false
            val response = PtpPacket.parseContainer(responseBytes, responseBytes.size) ?: return false
            opened = response.type == PtpPacket.CONTAINER_TYPE_RESPONSE &&
                (response.code == PtpPacket.RC_OK || response.code == PtpPacket.RC_SESSION_ALREADY_OPEN)
            return opened
        }
    }

    fun getDeviceInfo(): Boolean {
        if (!openSession()) return false
        val packet = PtpPacket.buildCommand(PtpPacket.OC_GET_DEVICE_INFO, nextTransactionId())
        if (!bulkWrite(packet)) return false
        val data = bulkRead() ?: return false
        val response = bulkRead() ?: return false
        return PtpPacket.parseContainer(data, data.size) != null &&
            PtpPacket.parseContainer(response, response.size)?.code == PtpPacket.RC_OK
    }

    fun triggerShutter(): Boolean {
        synchronized(commandLock) {
            if (!openSession()) return false
            val half = sendCommand(PtpPacket.OC_EOS_REMOTE_RELEASE_ON, intArrayOf(1, 0))
            if (half != PtpPacket.RC_OK) {
                sendCommand(PtpPacket.OC_EOS_REMOTE_RELEASE_OFF, intArrayOf(1))
                return false
            }
            // Give AF a brief settle window before full press.
            Thread.sleep(180)
            val full = sendCommand(PtpPacket.OC_EOS_REMOTE_RELEASE_ON, intArrayOf(2, 0))
            sendCommand(PtpPacket.OC_EOS_REMOTE_RELEASE_OFF, intArrayOf(2))
            sendCommand(PtpPacket.OC_EOS_REMOTE_RELEASE_OFF, intArrayOf(1))
            return full == PtpPacket.RC_OK
        }
    }

    fun downloadLatestObjectJpeg(): ByteArray? {
        if (!openSession()) return null
        val handles = listObjectHandles() ?: return null
        if (handles.isEmpty()) return null

        // Try a few latest handles because Canon may not expose newest object immediately.
        handles.asReversed().take(6).forEach { handle ->
            downloadObjectPayload(handle)?.let { payload ->
                if (payload.size > 32 * 1024) return payload
                extractJpeg(payload)?.let { return it }
            }
            downloadThumbPayload(handle)?.let { payload ->
                extractJpeg(payload)?.let { return it }
            }
        }
        return null
    }

    fun downloadLatestPreviewJpeg(): ByteArray? {
        if (!openSession()) return null
        val evfFrame = readLiveViewJpeg()
        if (evfFrame != null) {
            Log.d("PtpSession", "preview evf ok bytes=${evfFrame.size}")
            return evfFrame
        }
        // Keep EVF loop lightweight. Fallback to object/thumb can block several seconds
        // and causes visible freeze. For live preview, prefer fast null and next poll.
        if (liveViewStarted) {
            return null
        }
        val handles = listObjectHandles() ?: return null
        if (handles.isEmpty()) return null
        Log.d("PtpSession", "preview poll handles=${handles.size}")
        handles.asReversed().take(3).forEach { handle ->
            downloadThumbPayload(handle)?.let { payload ->
                extractJpeg(payload)?.let { jpeg ->
                    Log.d("PtpSession", "preview thumb ok handle=$handle bytes=${jpeg.size}")
                    return jpeg
                }
            }
        }
        // Fallback for cameras that do not provide fresh thumb during idle:
        // use latest object jpeg as pseudo-preview frame.
        val fallback = downloadLatestObjectJpeg()
        if (fallback != null) {
            Log.d("PtpSession", "preview fallback latest object bytes=${fallback.size}")
        } else {
            Log.d("PtpSession", "preview unavailable: no thumb and no object fallback")
        }
        return fallback
    }

    private fun startLiveView(): Boolean {
        if (!openSession()) return false
        if (liveViewStarted) return true
        sendCommand(PtpPacket.OC_EOS_SET_REMOTE_MODE, intArrayOf(1))
        sendCommand(PtpPacket.OC_EOS_SET_EVENT_MODE, intArrayOf(1))
        sendCommand(PtpPacket.OC_EOS_KEEP_DEVICE_ON)
        val hostAndCamera = setEosIntProperty(PtpPacket.DPC_EOS_EVF_OUTPUT_DEVICE, 3)
        val hostOnly = if (!hostAndCamera) setEosIntProperty(PtpPacket.DPC_EOS_EVF_OUTPUT_DEVICE, 2) else true
        liveViewStarted = hostOnly
        Log.d("PtpSession", "startLiveView started=$liveViewStarted")
        return liveViewStarted
    }

    fun stopLiveView(): Boolean {
        synchronized(commandLock) {
            if (!liveViewStarted) return true
            val ok = setEosIntProperty(PtpPacket.DPC_EOS_EVF_OUTPUT_DEVICE, 0)
            liveViewStarted = false
            evfMissCount = 0
            Log.d("PtpSession", "stopLiveView ok=$ok")
            return ok
        }
    }

    private fun readLiveViewJpeg(): ByteArray? {
        if (!liveViewStarted && !startLiveView()) return null
        val data = sendCommandExpectData(
            code = PtpPacket.OC_EOS_GET_VIEWFINDER_DATA,
            params = intArrayOf(0x00200000, 0, 0),
            timeoutMs = 1200,
        )
        if (data == null) {
            evfMissCount++
            if (evfMissCount >= 4) {
                Log.d("PtpSession", "evf no-data threshold reached, retry live view init")
                liveViewStarted = false
                evfMissCount = 0
            }
            return null
        }
        val jpeg = extractJpeg(data.payload)
        if (jpeg != null) {
            evfMissCount = 0
            return jpeg
        }
        evfMissCount++
        if (evfMissCount >= 6) {
            Log.d("PtpSession", "evf miss threshold reached, retry live view init")
            liveViewStarted = false
            evfMissCount = 0
        }
        return null
    }

    fun listObjectHandles(): List<Int>? {
        if (!openSession()) return null
        val handlesData = sendCommandExpectData(PtpPacket.OC_GET_OBJECT_HANDLES, intArrayOf(-1, 0, 0)) ?: return null
        return parseHandles(handlesData.payload)
    }

    fun getObjectInfo(handle: Int): ObjectInfo? {
        if (!openSession()) return null
        val data = sendCommandExpectData(PtpPacket.OC_GET_OBJECT_INFO, intArrayOf(handle)) ?: return null
        if (data.payload.size < 12) return null
        val buffer = ByteBuffer.wrap(data.payload).order(ByteOrder.LITTLE_ENDIAN)
        buffer.int // storage_id
        val objectFormat = buffer.short.toInt() and 0xFFFF
        buffer.short // protection_status
        val compressedSize = buffer.int.toLong() and 0xFFFFFFFFL
        return ObjectInfo(
            handle = handle,
            objectFormat = objectFormat,
            compressedSize = compressedSize,
        )
    }

    fun downloadJpegFromHandle(handle: Int): ByteArray? {
        if (!openSession()) return null
        val info = getObjectInfo(handle)
        if (info != null && info.compressedSize > 128 * 1024) {
            val partialObject = downloadObjectPayloadByPartial(handle, info.compressedSize.toInt())
            if (partialObject != null) {
                val minExpected = (info.compressedSize * 0.6).toInt()
                if (partialObject.size >= minExpected) {
                    // For Canon object transfer, payload is already JPEG object bytes.
                    return partialObject
                }
                Log.d(
                    "PtpSession",
                    "partial object too small handle=$handle bytes=${partialObject.size} expected=${info.compressedSize}",
                )
            }
        }
        downloadObjectPayload(handle)?.let { payload ->
            if (payload.size > 32 * 1024) return payload
            extractJpeg(payload)?.let { return it }
        }
        return null
    }

    fun downloadJpegFromHandleWithThumbFallback(handle: Int): ByteArray? {
        if (!openSession()) return null
        downloadObjectPayload(handle)?.let { payload ->
            extractJpeg(payload)?.let { return it }
        }
        downloadThumbPayload(handle)?.let { payload ->
            extractJpeg(payload)?.let { return it }
        }
        return null
    }

    private fun downloadObjectPayload(handle: Int): ByteArray? {
        val packet = PtpPacket.buildCommand(PtpPacket.OC_GET_OBJECT, nextTransactionId(), intArrayOf(handle))
        if (!bulkWrite(packet)) return null
        val dataBytes = readContainerBytes(timeoutMs = 15_000) ?: return null
        val responseBytes = readContainerBytes(timeoutMs = 15_000) ?: return null
        val response = PtpPacket.parseContainer(responseBytes, responseBytes.size) ?: return null
        if (response.code != PtpPacket.RC_OK) return null
        val data = PtpPacket.parseContainer(dataBytes, dataBytes.size) ?: return null
        return data.payload
    }

    private fun downloadThumbPayload(handle: Int): ByteArray? {
        val packet = PtpPacket.buildCommand(PtpPacket.OC_GET_THUMB, nextTransactionId(), intArrayOf(handle))
        if (!bulkWrite(packet)) return null
        val dataBytes = readContainerBytes(timeoutMs = 8_000) ?: return null
        val responseBytes = readContainerBytes(timeoutMs = 8_000) ?: return null
        val response = PtpPacket.parseContainer(responseBytes, responseBytes.size) ?: return null
        if (response.code != PtpPacket.RC_OK) return null
        val data = PtpPacket.parseContainer(dataBytes, dataBytes.size) ?: return null
        return data.payload
    }

    private fun downloadObjectPayloadByPartial(handle: Int, expectedSize: Int): ByteArray? {
        val output = ByteArrayOutputStream(expectedSize.coerceAtLeast(64 * 1024))
        var offset = 0
        val chunkSize = 64 * 1024
        var guard = 0
        var repeatChunkCount = 0
        var lastChunkHash = 0
        while (offset < expectedSize && guard < 300) {
            guard++
            val requestSize = min(chunkSize, expectedSize - offset).coerceAtLeast(1024)
            val data = sendCommandExpectData(
                code = PtpPacket.OC_GET_PARTIAL_OBJECT,
                params = intArrayOf(handle, offset, requestSize),
            ) ?: break
            if (data.payload.isEmpty()) break
            val currentHash = data.payload.contentHashCode()
            if (currentHash == lastChunkHash) {
                repeatChunkCount++
            } else {
                repeatChunkCount = 0
            }
            lastChunkHash = currentHash
            output.write(data.payload)
            offset += data.payload.size
            if (repeatChunkCount >= 3) {
                Log.d(
                    "PtpSession",
                    "partial repeated chunk detected handle=$handle offset=$offset payload=${data.payload.size}",
                )
                break
            }
            if (data.payload.size <= 512) break
        }
        Log.d("PtpSession", "partial download handle=$handle bytes=${output.size()} expected=$expectedSize")
        return if (output.size() > 0) output.toByteArray() else null
    }

    private fun sendCommand(code: Int, params: IntArray = intArrayOf()): Int? {
        val packet = PtpPacket.buildCommand(code, nextTransactionId(), params)
        if (!bulkWrite(packet)) return null
        val responseBytes = bulkRead() ?: return null
        val response = PtpPacket.parseContainer(responseBytes, responseBytes.size) ?: return null
        return response.code
    }

    private fun sendCommandWithData(code: Int, payload: ByteArray): Int? {
        val tx = nextTransactionId()
        val command = PtpPacket.buildCommand(code = code, transactionId = tx)
        val data = PtpPacket.buildData(code = code, transactionId = tx, payload = payload)
        if (!bulkWrite(command)) return null
        if (!bulkWrite(data)) return null
        val responseBytes = bulkRead(timeoutMs = 5000) ?: return null
        val response = PtpPacket.parseContainer(responseBytes, responseBytes.size) ?: return null
        return response.code
    }

    private fun setEosIntProperty(propertyCode: Int, value: Int): Boolean {
        val payload = ByteBuffer.allocate(12)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(12)
            .putInt(propertyCode)
            .putInt(value)
            .array()
        val response = sendCommandWithData(
            code = PtpPacket.OC_EOS_SET_DEVICE_PROP_VALUE_EX,
            payload = payload,
        )
        Log.d("PtpSession", "setEosIntProperty code=0x${propertyCode.toString(16)} value=$value ok=${response == PtpPacket.RC_OK}")
        return response == PtpPacket.RC_OK
    }

    private fun sendCommandExpectData(
        code: Int,
        params: IntArray = intArrayOf(),
        timeoutMs: Int = 15_000,
    ): PtpPacket.ParsedContainer? {
        val packet = PtpPacket.buildCommand(code, nextTransactionId(), params)
        if (!bulkWrite(packet)) return null
        val dataBytes = readContainerBytes(timeoutMs = timeoutMs) ?: return null
        val responseBytes = readContainerBytes(timeoutMs = timeoutMs) ?: return null
        val response = PtpPacket.parseContainer(responseBytes, responseBytes.size) ?: return null
        if (response.code != PtpPacket.RC_OK) return null
        return PtpPacket.parseContainer(dataBytes, dataBytes.size)
    }

    private fun readContainerBytes(timeoutMs: Int): ByteArray? {
        val first = bulkRead(timeoutMs = timeoutMs) ?: return null
        if (first.size < 12) return first
        val length = ByteBuffer.wrap(first, 0, 4).order(ByteOrder.LITTLE_ENDIAN).int
        if (length <= first.size || length < 12 || length > 80 * 1024 * 1024) return first
        val output = ByteArrayOutputStream(length)
        output.write(first)
        while (output.size() < length) {
            val chunk = bulkRead((length - output.size()).coerceAtMost(64 * 1024), timeoutMs) ?: return null
            output.write(chunk)
        }
        return output.toByteArray()
    }

    private fun parseHandles(payload: ByteArray): List<Int> {
        if (payload.size < 4) return emptyList()
        val buffer = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN)
        val count = buffer.int
        if (count < 0 || payload.size < 4 + count * 4) return emptyList()
        return List(count) { buffer.int }
    }

    private fun extractJpeg(payload: ByteArray): ByteArray? {
        var bestStart = -1
        var bestEnd = -1
        var bestSize = 0
        var start = -1
        var index = 0
        while (index < payload.lastIndex) {
            val b0 = payload[index].toInt() and 0xFF
            val b1 = payload[index + 1].toInt() and 0xFF
            if (start < 0 && b0 == 0xFF && b1 == 0xD8) {
                start = index
                index += 2
                continue
            }
            if (start >= 0 && b0 == 0xFF && b1 == 0xD9) {
                val end = index + 2
                val size = end - start
                if (size > bestSize) {
                    bestSize = size
                    bestStart = start
                    bestEnd = end
                }
                start = -1
                index += 2
                continue
            }
            index += 1
        }
        return if (bestStart >= 0 && bestEnd > bestStart) {
            payload.copyOfRange(bestStart, bestEnd)
        } else {
            null
        }
    }
}
