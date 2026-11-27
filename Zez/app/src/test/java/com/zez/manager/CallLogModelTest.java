package com.zez.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.zez.manager.model.CallLogItem;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CallLogModelTest {
    @Test
    public void callLogItemSerializesCleanly() throws Exception {
        CallLogItem item = new CallLogItem("42", "Alice", "+15551234", "INCOMING", 1700000000000L, 65L);
        String json = item.toJsonObject().toString();
        assertEquals("android:42", item.externalId);
        assertFalse(json.contains("fromBackup"));
        assertEquals("INCOMING", JsonParser.parseString(json).getAsJsonObject().get("type").getAsString());

        JsonArray arr = new JsonArray();
        arr.add(item.toJsonObject());
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(arr.toString().getBytes(StandardCharsets.UTF_8));
        assertEquals(32, digest.length);
        List<String> types = Arrays.asList("INCOMING", "OUTGOING", "MISSED", "REJECTED");
        assertEquals(4, types.size());
    }
}
