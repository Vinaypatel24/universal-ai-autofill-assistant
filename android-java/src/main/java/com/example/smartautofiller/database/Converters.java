package com.example.smartautofiller.database;

import androidx.room.TypeConverter;

import com.example.smartautofiller.model.ProfileSection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Room TypeConverters using Google Gson to serialize complex objects
 * (customFields Map and ProfileSection List) to/from SQLite TEXT columns.
 */
public class Converters {
    private static final Gson gson = new GsonBuilder().create();

    // ── Map<String, String> Converters ─────────────────────────
    @TypeConverter
    public static String fromCustomFields(Map<String, String> value) {
        if (value == null) {
            return "{}";
        }
        try {
            return gson.toJson(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    @TypeConverter
    public static Map<String, String> toCustomFields(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            Type mapType = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> map = gson.fromJson(value, mapType);
            return map != null ? map : new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    // ── List<ProfileSection> Converters ─────────────────────────
    @TypeConverter
    public static String fromSections(List<ProfileSection> value) {
        if (value == null) {
            return "[]";
        }
        try {
            return gson.toJson(value);
        } catch (Exception e) {
            return "[]";
        }
    }

    @TypeConverter
    public static List<ProfileSection> toSections(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Type listType = new TypeToken<List<ProfileSection>>() {}.getType();
            List<ProfileSection> list = gson.fromJson(value, listType);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
