package com.programmingblitz.notoffice563.util

import android.os.Environment
import android.os.StatFs

data class DeviceStorageInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usedPercentage: Float
) {
    val totalGbFormatted: String get() = "${totalBytes / (1024 * 1024 * 1024)} GB"
    val availableGbFormatted: String get() = "${availableBytes / (1024 * 1024 * 1024)} GB"
    val usedGbFormatted: String get() = "${usedBytes / (1024 * 1024 * 1024)} GB"
}

object StorageUtils {
    fun getDeviceStorageInfo(): DeviceStorageInfo {
        return try {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val blockSize = statFs.blockSizeLong
            val totalBlocks = statFs.blockCountLong
            val availableBlocks = statFs.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val availableBytes = availableBlocks * blockSize
            val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L)
            val percent = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

            DeviceStorageInfo(
                totalBytes = totalBytes,
                availableBytes = availableBytes,
                usedBytes = usedBytes,
                usedPercentage = percent
            )
        } catch (_: Exception) {
            DeviceStorageInfo(0L, 0L, 0L, 0f)
        }
    }
}
