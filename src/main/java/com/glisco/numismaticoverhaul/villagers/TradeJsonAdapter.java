package com.glisco.numismaticoverhaul.villagers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffers;
import org.jetbrains.annotations.NotNull;

public interface TradeJsonAdapter {

    @NotNull
    TradeOffers.Factory deserialize(JsonObject json);

    static ItemStack getItemStackFromJson(JsonObject json) {
        Item item = Registry.ITEM.getOrEmpty(Identifier.tryParse(json.get("item").getAsString())).orElseThrow(() -> new JsonSyntaxException("Invalid item:" + json.get("item").getAsString()));
        int count = json.has("count") ? json.get("count").getAsInt() : 1;

        return new ItemStack(item, count);
    }
}
