package com.glisco.numismaticoverhaul.villagers.json.adapters;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import com.google.gson.JsonObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class SellSingleEnchantmentAdapter extends TradeJsonAdapter {

    @Override
    @NotNull
    public TradeOffers.Factory deserialize(JsonObject json) {
        loadDefaultStats(json, false);
        return new Factory(max_uses, villager_experience, price_multiplier);
    }

    private static class Factory implements TradeOffers.Factory {
        private final int experience;
        private final int maxUses;
        private final float multiplier;

        public Factory(int maxUses, int experience, float multiplier) {
            this.experience = experience;
            this.maxUses = maxUses;
            this.multiplier = multiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            List<Enchantment> list = Registries.ENCHANTMENT.stream().filter(Enchantment::isAvailableForEnchantedBookOffer).collect(Collectors.toList());
            Enchantment enchantment = list.get(random.nextInt(list.size()));

            int enchantmentLevel = MathHelper.nextInt(random, enchantment.getMinLevel(), enchantment.getMaxLevel());

            ItemStack itemStack = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantment, enchantmentLevel));
            int cost = 100 * (10 / enchantment.getRarity().getWeight()) + (random.nextInt(50) + enchantmentLevel) * enchantmentLevel * enchantmentLevel * (10 / enchantment.getRarity().getWeight());
            if (enchantment.isTreasure()) {
                cost *= 2;
            }

            return new TradeOffer(CurrencyHelper.getClosest(cost), new ItemStack(Items.BOOK), itemStack, maxUses, this.experience, multiplier);
        }
    }
}
