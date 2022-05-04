package com.glisco.numismaticoverhaul.villagers.json.adapters;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import com.glisco.numismaticoverhaul.villagers.json.VillagerJsonHelper;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class SellDyedArmorAdapter extends TradeJsonAdapter {

    @Override
    public @NotNull TradeOffers.Factory deserialize(JsonObject json) {

        loadDefaultStats(json, true);

        VillagerJsonHelper.assertString(json, "item");

        int price = json.get("price").getAsInt();
        Item item = VillagerJsonHelper.getItemFromID(json.get("item").getAsString());

        return new Factory(item, price, max_uses, villager_experience, price_multiplier);
    }

    private static class Factory implements TradeOffers.Factory {
        private final Item sell;
        private final int price;
        private final int maxUses;
        private final int experience;
        private final float priceMultiplier;

        public Factory(Item item, int price, int maxUses, int experience, float priceMultiplier) {
            this.sell = item;
            this.price = price;
            this.maxUses = maxUses;
            this.experience = experience;
            this.priceMultiplier = priceMultiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            ItemStack itemStack2 = new ItemStack(this.sell);
            if (this.sell instanceof DyeableItem) {
                List<DyeItem> list = Lists.newArrayList();
                list.add(getDye(random));
                if (random.nextFloat() > 0.7F) {
                    list.add(getDye(random));
                }

                if (random.nextFloat() > 0.8F) {
                    list.add(getDye(random));
                }

                itemStack2 = DyeableItem.blendAndSetColor(itemStack2, list);
            }

            return new TradeOffer(CurrencyHelper.getClosest(price), itemStack2, this.maxUses, this.experience, priceMultiplier);

        }

        private static DyeItem getDye(Random random) {
            return DyeItem.byColor(DyeColor.byId(random.nextInt(16)));
        }
    }
}
