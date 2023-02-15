package com.huanchengfly.tieba.post.utils

import android.os.Environment
import android.os.StatFs
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.regex.Pattern

object DeviceUtils {
    var coreNum = -1
    private const val CPU_MAX_INFO_FORMAT = "/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_max_freq"
    private const val MEM_INFO_FILE = "/proc/meminfo"

    fun getDeviceScore(): Float {
        val cpuCores = getDeviceCpuCore().takeIf { it > 0 } ?: 6.9822063f
        val cpuAverageFrequency = getDeviceCpuAverageFrequency().takeIf { it > 0 } ?: 1.7859616f
        val totalMemory = getTotalMemory().takeIf { it > 0 } ?: 3.5425532f
        val totalSDCardSize = getTotalSDCardSize().takeIf { it >= 0 } ?: 51.957294f
//        val deviceScore = round(totalMemory)*0.0572301f + roundUpRom
        return 0f
    }

    fun getTotalSDCardSize(): Float {
        return runCatching {
            if (Environment.getExternalStorageState().equals("mounted")) {
                val path = Environment.getExternalStorageDirectory().path
                val stat = StatFs(path)
                val blockSize = stat.blockSizeLong
                val totalBlocks = stat.blockCountLong
                val totalSize = totalBlocks * blockSize
                totalSize / 1024f / 1024 / 1024
            } else -1f
        }.getOrDefault(-1f)
    }

    fun getTotalMemory(): Float {
        val memory = runCatching {
            File(MEM_INFO_FILE).bufferedReader(bufferSize = 8192).use { reader ->
                reader.readLine().split("\\s+".toRegex()).takeIf { it.size >= 2 }?.get(1)
                    ?.toLongOrNull() ?: 0
            }
        }.getOrDefault(0L)
        return if (memory > 0) memory.toFloat() / 1024 / 1024
        else -1f
    }

    // 获取手机 CPU 平均核心频率
    fun getDeviceCpuAverageFrequency(): Float {
        var totalFrequency = 0f
        val coreNum = getDeviceCpuCore()
        for (i in 0 until coreNum) {
            totalFrequency += getDeviceCpuFrequency(i)
        }
        return totalFrequency / coreNum
    }

    // 获取手机 CPU 某个核心的频率
    fun getDeviceCpuFrequency(core: Int): Float {
        return getContentFromFileInfo(
            CPU_MAX_INFO_FORMAT.format(
                Locale.ENGLISH,
                core
            )
        ).toFloatOrNull() ?: -1f
    }

    // 获取手机 CPU 核心数
    fun getDeviceCpuCore(): Int {
        return coreNum.takeIf { it > 0 } ?: runCatching {
            File("/sys/devices/system/cpu").listFiles { file ->
                Pattern.matches("cpu[0-9]", file.name)
            }?.size ?: -1
        }.getOrDefault(-1).also { coreNum = it }
    }

    private fun getContentFromFileInfo(filePath: String): String {
        return try {
            File(filePath).bufferedReader().use { it.readLine() }
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }
}