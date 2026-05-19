package com.errymaricha.dafydiobooth.ui.booth.external

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build

class CanonUsbController(context: Context) {
    private val appContext = context.applicationContext
    private val usbManager: UsbManager = appContext.getSystemService(Context.USB_SERVICE) as UsbManager
    private var connection: UsbDeviceConnection? = null
    private var claimedInterface: UsbInterface? = null
    private var bulkInEndpoint: UsbEndpoint? = null
    private var bulkOutEndpoint: UsbEndpoint? = null
    private var currentDevice: UsbDevice? = null

    fun findCanonDevice(vendorId: Int = CANON_VENDOR_ID): UsbDevice? {
        val canon = usbManager.deviceList.values
            .filter { it.vendorId == vendorId }
            .firstOrNull { resolveInterface(it) != null }
            ?: usbManager.deviceList.values.firstOrNull { it.vendorId == vendorId }
        currentDevice = canon
        return canon
    }

    fun hasPermission(device: UsbDevice): Boolean = usbManager.hasPermission(device)

    fun requestPermission(device: UsbDevice) {
        val intent = Intent(ACTION_USB_PERMISSION).apply { `package` = appContext.packageName }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val permissionIntent = PendingIntent.getBroadcast(
            appContext,
            device.deviceId,
            intent,
            flags,
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    fun connect(device: UsbDevice): Boolean {
        close()
        if (!hasPermission(device)) return false
        val intf = resolveInterface(device) ?: return false
        val bulkIn = findBulkEndpoint(intf, UsbConstants.USB_DIR_IN) ?: return false
        val bulkOut = findBulkEndpoint(intf, UsbConstants.USB_DIR_OUT) ?: return false
        val conn = usbManager.openDevice(device) ?: return false
        if (!conn.claimInterface(intf, true)) {
            conn.close()
            return false
        }
        connection = conn
        claimedInterface = intf
        bulkInEndpoint = bulkIn
        bulkOutEndpoint = bulkOut
        currentDevice = device
        return true
    }

    fun getConnection(): UsbDeviceConnection? = connection
    fun getBulkInEndpoint(): UsbEndpoint? = bulkInEndpoint
    fun getBulkOutEndpoint(): UsbEndpoint? = bulkOutEndpoint

    fun close() {
        runCatching {
            val intf = claimedInterface
            val conn = connection
            if (conn != null && intf != null) conn.releaseInterface(intf)
            conn?.close()
        }
        connection = null
        claimedInterface = null
        bulkInEndpoint = null
        bulkOutEndpoint = null
    }

    private fun resolveInterface(device: UsbDevice): UsbInterface? {
        for (index in 0 until device.interfaceCount) {
            val intf = device.getInterface(index)
            val hasIn = findBulkEndpoint(intf, UsbConstants.USB_DIR_IN) != null
            val hasOut = findBulkEndpoint(intf, UsbConstants.USB_DIR_OUT) != null
            if (intf.interfaceClass == UsbConstants.USB_CLASS_STILL_IMAGE && hasIn && hasOut) {
                return intf
            }
        }
        for (index in 0 until device.interfaceCount) {
            val intf = device.getInterface(index)
            val hasIn = findBulkEndpoint(intf, UsbConstants.USB_DIR_IN) != null
            val hasOut = findBulkEndpoint(intf, UsbConstants.USB_DIR_OUT) != null
            if (hasIn && hasOut) return intf
        }
        return null
    }

    private fun findBulkEndpoint(intf: UsbInterface, direction: Int): UsbEndpoint? {
        for (index in 0 until intf.endpointCount) {
            val endpoint = intf.getEndpoint(index)
            if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK && endpoint.direction == direction) {
                return endpoint
            }
        }
        return null
    }

    private companion object {
        const val ACTION_USB_PERMISSION = "com.errymaricha.dafydiobooth.USB_PERMISSION"
        const val CANON_VENDOR_ID = 0x04A9
    }
}
