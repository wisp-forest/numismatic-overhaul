package com.glisco.numismaticoverhaul.villagers.json.adapters;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import com.glisco.numismaticoverhaul.villagers.json.VillagerJsonHelper;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SellPotionContainerItemAdapter extends TradeJsonAdapter {

    @Override
    @NotNull
    public TradeOffers.Factory deserialize(JsonObject json) {

        loadDefaultStats(json, true);

        VillagerJsonHelper.assertJsonObject(json, "container_item");
        VillagerJsonHelper.assertJsonObject(json, "buy_item");

        int price = json.get("price").getAsInt();
        ItemStack container_item = VillagerJsonHelper.getItemStackFromJson(json.get("container_item").getAsJsonObject());
        ItemStack buy_item = VillagerJsonHelper.getItemStackFromJson(json.get("buy_item").getAsJsonObject());

        return new Factory(container_item, buy_item, price, max_uses, villager_experience, price_multiplier);
    }

    private static class Factory implements TradeOffers.Factory {
        private final ItemStack containerItem;
        private final ItemStack buyItem;

        private final int price;
        private final int maxUses;
        private final int experience;

        private final float priceMultiplier;

        public Factory(ItemStack containerItem, ItemStack buyItem, int price, int maxUses, int experience, float priceMultiplier) {
            this.containerItem = containerItem;
            this.buyItem = buyItem;
            this.price = price;
            this.maxUses = maxUses;
            this.experience = experience;
            this.priceMultiplier = priceMultiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            List<Potion> list = Registry.POTION.stream().filter((potion) -> !potion.getEffects().isEmpty() && BrewingRecipeRegistry.isBrewable(potion)).toList();

            Potion potion = list.get(random.nextInt(list.size()));
            ItemStack itemStack2 = PotionUtil.setPotion(containerItem.copy(), potion);
            return new TradeOffer(CurrencyHelper.getClosest(price), buyItem, itemStack2, this.maxUses, this.experience, this.priceMultiplier);
        }
    }
}
