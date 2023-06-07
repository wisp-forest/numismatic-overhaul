package com.glisco.numismaticoverhaul.villagers.json.adapters;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import com.glisco.numismaticoverhaul.villagers.json.VillagerJsonHelper;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import org.jetbrains.annotations.NotNull;

public class DimensionAwareSellStackAdapter extends TradeJsonAdapter {

    @Override
    @NotNull
    public TradeOffers.Factory deserialize(JsonObject json) {

        loadDefaultStats(json, true);

        VillagerJsonHelper.assertJsonObject(json, "sell");
        VillagerJsonHelper.assertString(json, "dimension");

        ItemStack sell = VillagerJsonHelper.getItemStackFromJson(json.get("sell").getAsJsonObject());
        String dimension = json.get("dimension").getAsString();

        int price = json.get("price").getAsInt();

        return new Factory(sell, price, dimension, max_uses, villager_experience, price_multiplier);
    }

    private static class Factory implements TradeOffers.Factory {
        private final ItemStack sell;
        private final int maxUses;
        private final int experience;
        private final int price;
        private final float multiplier;
        private final String targetDimensionId;

        public Factory(ItemStack sell, int price, String targetDimensionId, int maxUses, int experience, float multiplier) {
            this.sell = sell;
            this.maxUses = maxUses;
            this.experience = experience;
            this.price = price;
            this.multiplier = multiplier;
            this.targetDimensionId = targetDimensionId;
        }

        public TradeOffer create(Entity entity, Random random) {
            if (!entity.getWorld().getRegistryKey().getValue().toString().equals(targetDimensionId)) return null;

            return new TradeOffer(CurrencyHelper.getClosest(price), sell, this.maxUses, this.experience, multiplier);
        }
    }
}
