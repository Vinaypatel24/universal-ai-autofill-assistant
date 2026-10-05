package com.example.smartautofiller.database;

import com.example.smartautofiller.model.UserProfile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles exporting and importing user profiles to and from standard JSON files.
 */
public final class ProfileJsonSerializer {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private ProfileJsonSerializer() {}

    /**
     * Serializes a list of profiles to a formatted JSON string.
     */
    public static String exportProfilesToJson(List<UserProfile> profiles) {
        if (profiles == null) return "[]";
        try {
            return GSON.toJson(profiles);
        } catch (Exception e) {
            return "[]";
        }
    }

    /**
     * Deserializes a JSON string into a list of profiles.
     */
    public static List<UserProfile> importProfilesFromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Type listType = new TypeToken<List<UserProfile>>() {}.getType();
            List<UserProfile> list = GSON.fromJson(json, listType);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
