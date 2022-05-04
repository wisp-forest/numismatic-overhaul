package com.glisco.numismaticoverhaul.villagers.json.adapters;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import com.glisco.numismaticoverhaul.villagers.json.VillagerJsonHelper;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class SellStackAdapter extends TradeJsonAdapter {

    @Override
    @NotNull
    public TradeOffers.Factory deserialize(JsonObject json) {

        loadDefaultStats(json, true);

        VillagerJsonHelper.assertJsonObject(json, "sell");

        ItemStack sell = VillagerJsonHelper.getItemStackFromJson(json.get("sell").getAsJsonObject());
        int price = json.get("price").getAsInt();

        return new Factory(sell, price, max_uses, villager_experience, price_multiplier);
    }

    private static class Factory implements TradeOffers.Factory {
        private final ItemStack sell;
        private final int maxUses;
        private final int experience;
        private final int price;
        private final float multiplier;

        public Factory(ItemStack sell, int price, int maxUses, int experience, float multiplier) {
            this.sell = sell;
            this.maxUses = maxUses;
            this.experience = experience;
            this.price = price;
            this.multiplier = multiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            return new TradeOffer(CurrencyHelper.getClosest(price), sell, this.maxUses, this.experience, multiplier);
        }
    }
}
