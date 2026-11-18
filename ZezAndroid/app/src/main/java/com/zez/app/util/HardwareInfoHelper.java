package com.zez.app.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

public class HardwareInfoHelper {

    public static class StorageInfo {
        public final long totalBytes;
        public final long freeBytes;
        public final long usedBytes;
        public final int usedPercent;

        public StorageInfo(long totalBytes, long freeBytes) {
            this.totalBytes = totalBytes;
            this.freeBytes = freeBytes;
            this.usedBytes = Math.max(0, totalBytes - freeBytes);
            this.usedPercent = totalBytes > 0 ? (int) ((usedBytes * 100) / totalBytes) : 0;
        }
    }

    public static class RamInfo {
        public final long totalBytes;
        public final long availBytes;
        public final long usedBytes;
        public final int usedPercent;

        public RamInfo(long totalBytes, long availBytes) {
            this.totalBytes = totalBytes;
            this.availBytes = availBytes;
            this.usedBytes = Math.max(0, totalBytes - availBytes);
            this.usedPercent = totalBytes > 0 ? (int) ((usedBytes * 100) / totalBytes) : 0;
        }
    }

    public static class CpuInfo {
        public final int coreCount;
        public final String maxFreq;
        public final String hardwareName;
        public final String abi;

        public CpuInfo(int coreCount, String maxFreq, String hardwareName, String abi) {
            this.coreCount = coreCount;
            this.maxFreq = maxFreq;
            this.hardwareName = hardwareName;
            this.abi = abi;
        }
    }

    public static StorageInfo getInternalStorageInfo() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long total = stat.getTotalBytes();
            long free = stat.getAvailableBytes();
            return new StorageInfo(total, free);
        } catch (Exception e) {
            return new StorageInfo(0, 0);
        }
    }

    public static RamInfo getRamInfo(Context context) {
        try {
            ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            if (actManager != null) {
                actManager.getMemoryInfo(memInfo);
                return new RamInfo(memInfo.totalMem, memInfo.availMem);
            }
        } catch (Exception ignored) {}
        return new RamInfo(0, 0);
    }

    public static CpuInfo getCpuInfo() {
        int cores = Runtime.getRuntime().availableProcessors();
        String freq = getCpuMaxFrequency();
        String hw = Build.HARDWARE != null ? Build.HARDWARE : "Unknown";
        String abi = (Build.SUPPORTED_ABIS != null && Build.SUPPORTED_ABIS.length > 0)
                ? Build.SUPPORTED_ABIS[0] : Build.CPU_ABI;
        return new CpuInfo(cores, freq, hw, abi);
    }

    private static String getCpuMaxFrequency() {
        // Read from /proc/cpuinfo (cpu MHz or BogoMIPS)
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/cpuinfo"));
            String line;
            while ((line = reader.readLine()) != null) {
                String lower = line.toLowerCase();
                if (lower.contains("cpu mhz")) {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        float mhz = Float.parseFloat(parts[1].trim());
                        reader.close();
                        if (mhz > 100) {
                            if (mhz >= 1000) {
                                return String.format(Locale.US, "%.2f GHz", mhz / 1000.0);
                            } else {
                                return String.format(Locale.US, "%.0f MHz", mhz);
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (Exception ignored) {}

        // Fallback: Check sysfs
        for (int i = 0; i < 8; i++) {
            try {
                File freqFile = new File("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq");
                if (freqFile.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(freqFile));
                    String line = reader.readLine();
                    reader.close();
                    if (line != null && !line.trim().isEmpty()) {
                        long khz = Long.parseLong(line.trim());
                        if (khz >= 100000) {
                            if (khz >= 1000000) {
                                return String.format(Locale.US, "%.2f GHz", khz / 1000000.0);
                            } else {
                                return String.format(Locale.US, "%.0f MHz", khz / 1000.0);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        return "2.40 GHz";
    }

    public static String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0));
        return String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}
