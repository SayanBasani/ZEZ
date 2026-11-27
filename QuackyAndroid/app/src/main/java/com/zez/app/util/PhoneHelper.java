package com.zez.app.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.Locale;

public class PhoneHelper {

    /**
     * Normalizes a phone number for Indian mobile numbers & international formats.
     * Handles +91, 0091, 91, and leading 0 prefixes so 10-digit Indian numbers
     * map canonically to the same 10 digits.
     */
    public static String normalize(String raw) {
        if (raw == null) return "";
        String digits = raw.replaceAll("\\D", "");
        if (digits.startsWith("0091")) {
            digits = digits.substring(4);
        } else if (digits.length() == 12 && digits.startsWith("91")) {
            digits = digits.substring(2);
        } else if (digits.length() == 11 && digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        return digits;
    }

    /**
     * Checks if two phone numbers represent the same phone / contact.
     */
    public static boolean isSamePhone(String p1, String p2) {
        if (p1 == null || p2 == null) return false;
        String n1 = normalize(p1);
        String n2 = normalize(p2);
        if (n1.isEmpty() || n2.isEmpty()) return false;
        if (n1.equals(n2)) return true;
        if (n1.length() >= 10 && n2.length() >= 10) {
            String last10_1 = n1.substring(n1.length() - 10);
            String last10_2 = n2.substring(n2.length() - 10);
            return last10_1.equals(last10_2);
        }
        return false;
    }

    /**
     * Matches a search query against a contact name and phone number.
     * Overcomes the +91 confusion: whether the contact is saved with +91 or without,
     * and whether the user types with +91 or without, matches flexibly!
     */
    public static boolean matchesSearch(String query, String name, String phone) {
        if (query == null || query.trim().isEmpty()) return true;
        String q = query.trim().toLowerCase(Locale.ROOT);

        // 1. Match display name
        if (name != null && name.toLowerCase(Locale.ROOT).contains(q)) {
            return true;
        }

        // 2. Match raw phone string
        if (phone != null && phone.toLowerCase(Locale.ROOT).contains(q)) {
            return true;
        }

        // 3. Match normalized phone digits (handles +91, dashes, spaces, 0 prefixes)
        String qDigits = q.replaceAll("\\D", "");
        if (!qDigits.isEmpty() && phone != null) {
            String pNorm = normalize(phone);
            String qNorm = normalize(q);

            if (!pNorm.isEmpty()) {
                if (pNorm.contains(qNorm) || pNorm.contains(qDigits)) {
                    return true;
                }
                if (qNorm.length() >= 10 && pNorm.endsWith(qNorm.substring(qNorm.length() - 10))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Resolves the contact name for a given phone number.
     * Priority:
     * 1. Local device address book (ContactsContract) - if user saved them as "EFG" or "Dad", show that!
     * 2. Fallback to registered profile name (e.g. "ABCD")
     * 3. Clean up any placeholder names like "Physical Device User (...)"
     */
    public static String resolveContactName(Context context, String phoneNumber, String fallbackName) {
        if (context == null) return fallbackName;
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            // Fast lookup via PhoneLookup URI
            try {
                Uri lookupUri = Uri.withAppendedPath(
                        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                        Uri.encode(phoneNumber.trim())
                );
                Cursor cursor = context.getContentResolver().query(
                        lookupUri,
                        new String[]{ ContactsContract.PhoneLookup.DISPLAY_NAME },
                        null,
                        null,
                        null
                );
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            int nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                            if (nameIndex >= 0) {
                                String localName = cursor.getString(nameIndex);
                                if (localName != null && !localName.trim().isEmpty()) {
                                    return localName.trim();
                                }
                            }
                        }
                    } finally {
                        cursor.close();
                    }
                }
            } catch (Exception ignored) {}

            // Fallback: match by last 10 digits in CommonDataKinds.Phone
            String digits = normalize(phoneNumber);
            if (digits.length() >= 10) {
                String last10 = digits.substring(digits.length() - 10);
                try {
                    Cursor c2 = context.getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                            },
                            null,
                            null,
                            null
                    );
                    if (c2 != null) {
                        try {
                            int nameIdx = c2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                            int numIdx = c2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            while (c2.moveToNext()) {
                                String num = numIdx >= 0 ? c2.getString(numIdx) : null;
                                if (num != null) {
                                    String norm = normalize(num);
                                    if (norm.endsWith(last10)) {
                                        String localName = nameIdx >= 0 ? c2.getString(nameIdx) : null;
                                        if (localName != null && !localName.trim().isEmpty()) {
                                            return localName.trim();
                                        }
                                    }
                                }
                            }
                        } finally {
                            c2.close();
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // Clean fallback
        if (fallbackName != null && !fallbackName.trim().isEmpty()) {
            if (!fallbackName.toLowerCase(Locale.ROOT).contains("physical device user")) {
                return fallbackName.trim();
            }
        }
        return phoneNumber != null ? phoneNumber : "Unknown";
    }
}
