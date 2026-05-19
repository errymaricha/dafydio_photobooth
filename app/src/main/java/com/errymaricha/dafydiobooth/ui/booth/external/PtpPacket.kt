package com.errymaricha.dafydiobooth.ui.booth.external

import java.nio.ByteBuffer
import java.nio.ByteOrder

object PtpPacket {
    const val CONTAINER_TYPE_COMMAND = 1
    const val CONTAINER_TYPE_DATA = 2
    const val CONTAINER_TYPE_RESPONSE = 3

    const val OC_OPEN_SESSION = 0x1002
    const val OC_CLOSE_SESSION = 0x1003
    const val OC_GET_DEVICE_INFO = 0x1001
    const val OC_GET_OBJECT_HANDLES = 0x1007
    const val OC_GET_OBJECT_INFO = 0x1008
    const val OC_GET_OBJECT = 0x1009
    const val OC_GET_THUMB = 0x100A
    const val OC_GET_PARTIAL_OBJECT = 0x101B
    const val OC_EOS_REMOTE_RELEASE_ON = 0x9128
    const val OC_EOS_REMOTE_RELEASE_OFF = 0x9129
    const val OC_EOS_SET_REMOTE_MODE = 0x9114
    const val OC_EOS_SET_EVENT_MODE = 0x9115
    const val OC_EOS_KEEP_DEVICE_ON = 0x911D
    const val OC_EOS_SET_DEVICE_PROP_VALUE_EX = 0x9110
    const val OC_EOS_GET_VIEWFINDER_DATA = 0x9153
    const val DPC_EOS_EVF_OUTPUT_DEVICE = 0xD1B0

    const val RC_OK = 0x2001
    const val RC_OPERATION_NOT_SUPPORTED = 0x2005
    const val RC_SESSION_ALREADY_OPEN = 0x201E

    fun buildCommand(code: Int, transactionId: Int, params: IntArray = intArrayOf()): ByteArray {
        val length = 12 + params.size * 4
        val buffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(length)
        buffer.putShort(CONTAINER_TYPE_COMMAND.toShort())
        buffer.putShort(code.toShort())
        buffer.putInt(transactionId)
        params.forEach(buffer::putInt)
        return buffer.array()
    }

    fun buildData(code: Int, transactionId: Int, payload: ByteArray): ByteArray {
        val length = 12 + payload.size
        val buffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(length)
        buffer.putShort(CONTAINER_TYPE_DATA.toShort())
        buffer.putShort(code.toShort())
        buffer.putInt(transactionId)
        buffer.put(payload)
        return buffer.array()
    }

    data class ParsedContainer(
        val type: Int,
        val code: Int,
        val payload: ByteArray,
    )

    fun parseContainer(bytes: ByteArray, size: Int): ParsedContainer? {
        if (size < 12) return null
        val buffer = ByteBuffer.wrap(bytes, 0, size).order(ByteOrder.LITTLE_ENDIAN)
        val length = buffer.int
        if (length < 12 || length > size) return null
        val type = buffer.short.toInt() and 0xFFFF
        val code = buffer.short.toInt() and 0xFFFF
        buffer.int
        val payloadSize = (length - 12).coerceAtLeast(0)
        val payload = ByteArray(payloadSize)
        if (payloadSize > 0) buffer.get(payload)
        return ParsedContainer(type = type, code = code, payload = payload)
    }
}
