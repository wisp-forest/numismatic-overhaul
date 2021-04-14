package com.glisco.numismaticoverhaul.villagers;

import com.google.gson.JsonObject;

public class DeserializationContext {

    private static String file_cache;
    private static String profession_cache;
    private static int level_cache;
    private static JsonObject trade_cache;

    public final String file;
    public final String profession;
    public final int level;
    public final JsonObject trade;

    static {
        clear();
    }

    private DeserializationContext() {
        file = file_cache;
        profession = profession_cache;
        trade = VillagerJsonHelper.deepCopy(trade_cache, JsonObject.class);
        level = level_cache;
    }

    public static DeserializationContext getCurrentState() {
        return new DeserializationContext();
    }

    public static void clear() {
        file_cache = "none";
        profession_cache = "none";
        trade_cache = null;
        level_cache = -1;
    }

    public static void setFile(String file) {
        file_cache = file;
    }

    public static void setProfession(String profession) {
        profession_cache = profession;
    }

    public static void setLevel(int level) {
        level_cache = level;
    }

    public static void setTrade(JsonObject trade) {
        trade_cache = trade;
    }

}
