package com.glisco.numismaticoverhaul.villagers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;

public class VillagerJsonHelper {

    private static final Gson gson = new Gson();

    public static void assertElement(JsonObject object, String key) {
        if (!object.has(key)) throw new DeserializationException("Missing property " + key);
    }

    public static void assertInt(JsonObject object, String key) {

        assertElement(object, key);

        if (!object.get(key).isJsonPrimitive()) throw new DeserializationException("Not an integer " + key);
        if (!object.get(key).getAsJsonPrimitive().isNumber()) throw new DeserializationException("Not an integer " + key);

        try {
            Integer.parseInt(object.get(key).getAsString());
        } catch (NumberFormatException e) {
            throw new DeserializationException("Not an integer " + key);
        }
    }

    public static void assertString(JsonObject object, String key) {
        assertElement(object, key);
        if (!object.get(key).isJsonPrimitive()) throw new DeserializationException("Not a String " + key);
        if (!object.get(key).getAsJsonPrimitive().isString()) throw new DeserializationException("Not a String " + key);
    }

    public static void assertJsonObject(JsonObject object, String key) {
        assertElement(object, key);
        if (!object.get(key).isJsonObject()) throw new DeserializationException("Not a JsonObject " + key);
    }

    public static int int_getOrDefault(JsonObject object, String key, int defaultValue) {
        return object.has(key) ? object.get(key).getAsInt() : defaultValue;
    }

    public static float float_getOrDefault(JsonObject object, String key, float defaultValue) {
        return object.has(key) ? object.get(key).getAsFloat() : defaultValue;
    }

    public static boolean boolean_getOrDefault(JsonObject object, String key, boolean defaultValue) {
        return object.has(key) ? object.get(key).getAsBoolean() : defaultValue;
    }

    public static ItemStack ItemStack_getOrDefault(JsonObject object, String key, ItemStack defaultValue) {
        return object.has(key) ? getItemStackFromJson(object.get(key).getAsJsonObject()) : defaultValue;
    }

    public static ItemStack getItemStackFromJson(JsonObject json) {

        if (!json.has("item")) throw new DeserializationException("ItemStack missing item ID " + json);

        Item item = getItemFromID(json.get("item").getAsString());
        int count = json.has("count") ? json.get("count").getAsInt() : 1;

        ItemStack stack = new ItemStack(item, count);

        if (json.has("tag")) {

            String toParse = json.get("tag").getAsJsonObject().toString();
            CompoundTag stackTag = null;

            try {
                stackTag = new StringNbtReader(new StringReader(toParse)).parseCompoundTag();
            } catch (CommandSyntaxException e) {
                VillagerTradesHandler.addLoadingException(new DeserializationException("Tag parsing error: " + e.getMessage()));
            }

            if (stackTag != null) stack.setTag(stackTag);
        }

        return stack;
    }

    public static Item getItemFromID(String id) {
        return Registry.ITEM.getOrEmpty(Identifier.tryParse(id)).orElseThrow(() -> new DeserializationException("Invalid item: \"" + id + "\""));
    }

    public static <T> T deepCopy(T object, Class<T> type) {
        try {
            return gson.fromJson(gson.toJson(object, type), type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
