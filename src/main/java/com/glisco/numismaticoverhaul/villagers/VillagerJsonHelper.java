package com.glisco.numismaticoverhaul.villagers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class VillagerJsonHelper {

    public static void assertElement(JsonObject object, String key) {
        if (!object.has(key)) throw new JsonSyntaxException("Missing property " + key);
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
        Item item = getItemFromID(json.get("item").getAsString());
        int count = json.has("count") ? json.get("count").getAsInt() : 1;

        ItemStack stack = new ItemStack(item, count);

        if (json.has("tag")) {

            String toParse = json.get("tag").getAsJsonObject().toString();
            CompoundTag stackTag = null;

            try {
                stackTag = new StringNbtReader(new StringReader(toParse)).parseCompoundTag();
            } catch (CommandSyntaxException e) {
                VillagerTradesHandler.addLoadingException(e);
            }

            if (stackTag != null) stack.setTag(stackTag);
        }

        return stack;
    }

    public static Item getItemFromID(String id) {
        return Registry.ITEM.getOrEmpty(Identifier.tryParse(id)).orElseThrow(() -> new JsonSyntaxException("Invalid item: \"" + id + "\""));
    }
}
