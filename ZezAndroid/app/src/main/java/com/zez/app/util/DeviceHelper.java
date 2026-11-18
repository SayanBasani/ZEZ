package com.zez.app.util;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Locale;

public class DeviceHelper {

    public static String getAndroidId(Context context) {
        String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return id != null ? id : "unknown_android_id";
    }

    public static JsonObject getDeviceDetails(Context context) {
        JsonObject details = new JsonObject();
        JsonObject device = new JsonObject();
        device.addProperty("deviceName", Build.MODEL);
        device.addProperty("manufacturer", Build.MANUFACTURER);
        device.addProperty("brand", Build.BRAND);
        device.addProperty("modelName", Build.MODEL);
        device.addProperty("modelId", Build.ID);
        device.addProperty("deviceType", "HANDSET");
        device.addProperty("isPhysicalDevice", true);
        details.add("device", device);

        JsonObject os = new JsonObject();
        os.addProperty("name", "Android");
        os.addProperty("version", Build.VERSION.RELEASE);
        os.addProperty("androidApiLevel", Build.VERSION.SDK_INT);
        details.add("operatingSystem", os);

        JsonObject app = new JsonObject();
        app.addProperty("applicationId", context.getPackageName());
        app.addProperty("appName", "Zez");
        app.addProperty("appVersion", "1.0.0");
        details.add("application", app);

        JsonObject sim = new JsonObject();
        sim.addProperty("primaryPhoneNumber", "");
        details.add("simInformation", sim);

        return details;
    }
}
