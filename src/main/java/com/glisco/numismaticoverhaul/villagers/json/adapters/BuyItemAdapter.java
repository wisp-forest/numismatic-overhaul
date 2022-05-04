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

public class BuyItemAdapter extends TradeJsonAdapter {

    @Override
    @NotNull
    public TradeOffers.Factory deserialize(JsonObject json) {

        loadDefaultStats(json, true);

        VillagerJsonHelper.assertJsonObject(json, "buy");

        int price = json.get("price").getAsInt();
        ItemStack buy = VillagerJsonHelper.getItemStackFromJson(json.get("buy").getAsJsonObject());

        return new Factory(buy, price, max_uses, villager_experience, price_multiplier);
    }

    private static class Factory implements TradeOffers.Factory {
        private final ItemStack buy;
        private final int price;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public Factory(ItemStack buy, int price, int maxUses, int experience, float multiplier) {
            this.buy = buy;
            this.price = price;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = multiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            return new TradeOffer(buy, CurrencyHelper.getClosest(price), this.maxUses, this.experience, this.multiplier);
        }
    }
}
