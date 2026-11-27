package com.zez.manager.util;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;

import java.util.List;

public class DeviceHelper {

    public static class SimInfo {
        public String sim1PhoneNumber = null;
        public String sim2PhoneNumber = null;
        public String sim1Carrier = null;
        public String sim2Carrier = null;
        public String sim1CountryIso = null;
        public String sim2CountryIso = null;
        public String primaryPhoneNumber = null;
    }

    public static String getAndroidId(Context context) {
        try {
            String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (id != null && !id.trim().isEmpty()) {
                return id.trim();
            }
        } catch (Exception ignored) {}
        return "unknown-android-id";
    }

    public static String getAppVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName != null ? pInfo.versionName : "1.0.0";
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    public static int getAppVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return (int) pInfo.getLongVersionCode();
            }
            return pInfo.versionCode;
        } catch (Exception e) {
            return 1;
        }
    }

    public static boolean isWifiConnected(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            Network active = cm.getActiveNetwork();
            if (active == null) return false;
            NetworkCapabilities caps = cm.getNetworkCapabilities(active);
            return caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        } catch (Exception ignored) {
            return false;
        }
    }

    public static String getNetworkType(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return "Offline";
            Network active = cm.getActiveNetwork();
            if (active == null) return "Offline";
            NetworkCapabilities caps = cm.getNetworkCapabilities(active);
            if (caps == null) return "Offline";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return "WiFi";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return "Cellular";
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return "Ethernet";
            if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return "Connected";
        } catch (Exception ignored) {}
        return "Offline";
    }

    public static SimInfo getSimInfo(Context context) {
        SimInfo info = new SimInfo();
        if (context == null) return info;

        boolean hasStatePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED;
        boolean hasNumbersPerm = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            hasNumbersPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS)
                    == PackageManager.PERMISSION_GRANTED;
        }

        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                info.sim1Carrier = tm.getNetworkOperatorName();
                info.sim1CountryIso = tm.getNetworkCountryIso();
                if ((hasStatePerm || hasNumbersPerm) && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    try {
                        String line1 = tm.getLine1Number();
                        if (line1 != null && !line1.trim().isEmpty()) {
                            info.sim1PhoneNumber = line1.trim();
                            info.primaryPhoneNumber = line1.trim();
                        }
                    } catch (SecurityException ignored) {}
                }
            }
        } catch (Exception ignored) {}

        if ((hasStatePerm || hasNumbersPerm) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                SubscriptionManager sm = SubscriptionManager.from(context);
                if (sm != null) {
                    List<SubscriptionInfo> subList = sm.getActiveSubscriptionInfoList();
                    if (subList != null && !subList.isEmpty()) {
                        for (int i = 0; i < subList.size(); i++) {
                            SubscriptionInfo si = subList.get(i);
                            if (si == null) continue;
                            String carrier = si.getCarrierName() != null ? si.getCarrierName().toString() : null;
                            String countryIso = si.getCountryIso();
                            String number = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                try {
                                    number = sm.getPhoneNumber(si.getSubscriptionId());
                                } catch (SecurityException ignored) {}
                            } else {
                                number = si.getNumber();
                            }

                            if (i == 0) {
                                if (carrier != null && !carrier.isEmpty()) info.sim1Carrier = carrier;
                                if (countryIso != null && !countryIso.isEmpty()) info.sim1CountryIso = countryIso;
                                if (number != null && !number.trim().isEmpty()) {
                                    info.sim1PhoneNumber = number.trim();
                                    if (info.primaryPhoneNumber == null) info.primaryPhoneNumber = number.trim();
                                }
                            } else if (i == 1) {
                                if (carrier != null && !carrier.isEmpty()) info.sim2Carrier = carrier;
                                if (countryIso != null && !countryIso.isEmpty()) info.sim2CountryIso = countryIso;
                                if (number != null && !number.trim().isEmpty()) {
                                    info.sim2PhoneNumber = number.trim();
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        return info;
    }

    public static JsonObject getDeviceDetails(Context context) {
        JsonObject root = new JsonObject();

        JsonObject deviceObj = new JsonObject();
        deviceObj.addProperty("deviceName", Build.MODEL);
        deviceObj.addProperty("model", Build.MODEL);
        deviceObj.addProperty("manufacturer", Build.MANUFACTURER);
        deviceObj.addProperty("brand", Build.BRAND);
        deviceObj.addProperty("product", Build.PRODUCT);
        deviceObj.addProperty("hardware", Build.HARDWARE);
        deviceObj.addProperty("board", Build.BOARD);
        deviceObj.addProperty("host", Build.HOST);
        deviceObj.addProperty("androidId", getAndroidId(context));
        deviceObj.addProperty("persistentId", PreferencesHelper.getPersistentUuid(context));
        root.add("device", deviceObj);

        JsonObject osObj = new JsonObject();
        osObj.addProperty("os", "Android");
        osObj.addProperty("versionRelease", Build.VERSION.RELEASE);
        osObj.addProperty("sdkInt", Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            osObj.addProperty("securityPatch", Build.VERSION.SECURITY_PATCH);
        }
        root.add("operatingSystem", osObj);

        JsonObject appObj = new JsonObject();
        appObj.addProperty("appId", "zez");
        appObj.addProperty("packageName", context.getPackageName());
        appObj.addProperty("appVersion", getAppVersionName(context));
        appObj.addProperty("versionCode", getAppVersionCode(context));
        root.add("application", appObj);

        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            long total = stat.getTotalBytes();
            long avail = stat.getAvailableBytes();
            JsonObject storageObj = new JsonObject();
            storageObj.addProperty("internalTotalBytes", total);
            storageObj.addProperty("internalAvailableBytes", avail);
            storageObj.addProperty("internalUsedBytes", Math.max(0, total - avail));
            root.add("storage", storageObj);
        } catch (Exception ignored) {}

        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(mi);
                JsonObject memObj = new JsonObject();
                memObj.addProperty("totalRamBytes", mi.totalMem);
                memObj.addProperty("availableRamBytes", mi.availMem);
                memObj.addProperty("lowMemory", mi.lowMemory);
                root.add("memory", memObj);
            }
        } catch (Exception ignored) {}

        JsonObject netObj = new JsonObject();
        String netType = getNetworkType(context);
        netObj.addProperty("networkType", netType);
        netObj.addProperty("isConnected", !"Offline".equals(netType));
        root.add("network", netObj);

        return root;
    }
}
