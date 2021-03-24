package com.glisco.numismaticoverhaul.villagers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class VillagerJsonHelper {

    public static void assertElement(JsonObject object, String key) {
        if (!object.has(key)) throw new JsonSyntaxException("Missing property " + key);
    }

    public static int int_getOrDefault(JsonObject object, String key, int defaultValue) {
        return object.has("key") ? object.get(key).getAsInt() : defaultValue;
    }
}
