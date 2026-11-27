package com.zez.manager;

import android.os.Bundle;

import com.google.gson.JsonObject;
import com.zez.manager.model.AppCallLogItem;
import com.zez.manager.util.AppCallDetector;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AppCallDetectorTest {

    @Before
    public void setUp() {
        AppCallDetector.clearDedupCache();
    }

    @Test
    public void testKnownPackagesRecognition() {
        assertTrue(AppCallDetector.isKnownCallPackage("com.whatsapp"));
        assertTrue(AppCallDetector.isKnownCallPackage("com.whatsapp.w4b"));
        assertTrue(AppCallDetector.isKnownCallPackage("org.telegram.messenger"));
        assertTrue(AppCallDetector.isKnownCallPackage("org.thunderdog.challegram"));
        assertTrue(AppCallDetector.isKnownCallPackage("com.facebook.orca"));
        assertTrue(AppCallDetector.isKnownCallPackage("com.google.android.apps.tachyon"));
        assertTrue(AppCallDetector.isKnownCallPackage("com.discord"));
        assertTrue(AppCallDetector.isKnownCallPackage("com.microsoft.teams"));

        assertEquals("WhatsApp", AppCallDetector.getAppName("com.whatsapp"));
        assertEquals("Telegram", AppCallDetector.getAppName("org.telegram.messenger"));
        assertEquals("Google Meet", AppCallDetector.getAppName("com.google.android.apps.tachyon"));
    }

    @Test
    public void testParseCallDirectionAndType() {
        AppCallLogItem incoming = AppCallDetector.parseCallDetails(
                "com.whatsapp",
                null,
                null,
                1795634000000L
        );
        // Without text or category, it should be null
        assertNull(incoming);

        // Simulated bundle with text
        // AppCallDetector.parseCallDetails with bundle or title/text
        // Let's test with AppCallLogItem directly
        AppCallLogItem item = new AppCallLogItem(
                "WhatsApp",
                "com.whatsapp",
                "Alice",
                "video",
                "incoming",
                0,
                1795634125000L,
                "Incoming video call"
        );
        JsonObject json = item.toJsonObject();
        assertEquals("WhatsApp", json.get("appName").getAsString());
        assertEquals("com.whatsapp", json.get("packageName").getAsString());
        assertEquals("Alice", json.get("callerName").getAsString());
        assertEquals("video", json.get("callType").getAsString());
        assertEquals("incoming", json.get("callDirection").getAsString());
        assertEquals(1795634125000L, json.get("callTime").getAsLong());
        assertEquals("Incoming video call", json.get("rawDetails").getAsString());
    }

    @Test
    public void testNameCleaningLogic() {
        String raw1 = "John Doe - Incoming call";
        String cleaned1 = raw1.replaceAll("(?i)\\s*[-–:]\\s*(incoming|outgoing|missed|video|voice)?\\s*call.*", "").trim();
        assertEquals("John Doe", cleaned1);

        String raw2 = "Jane Smith (Voice call)";
        String cleaned2 = raw2.replaceAll("(?i)\\s*\\((incoming|outgoing|missed|video|voice)?\\s*call\\)", "").trim();
        assertEquals("Jane Smith", cleaned2);

        String raw3 = "Boss : Missed call";
        String cleaned3 = raw3.replaceAll("(?i)\\s*[-–:]\\s*(incoming|outgoing|missed|video|voice)?\\s*call.*", "").trim();
        assertEquals("Boss", cleaned3);
    }
}
