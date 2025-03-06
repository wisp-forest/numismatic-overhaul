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
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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
            int cost;
            ItemStack itemStack;

            Optional<RegistryEntry<Enchantment>> optionalEnchantment = entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getRandomEntry(EnchantmentTags.TRADEABLE, random);
            if (optionalEnchantment.isPresent()) {
                RegistryEntry<Enchantment> enchantmentEntry = optionalEnchantment.get();
                Enchantment enchantment = enchantmentEntry.value();

                int enchantmentLevel = MathHelper.nextInt(random, enchantment.getMinLevel(), enchantment.getMaxLevel());
                itemStack = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantmentEntry, enchantmentLevel));

                // TODO: Rarity/Weight no longer exists in Enchantment removed the extra math for now.
                cost = 100 * ((random.nextInt(50) + enchantmentLevel) * enchantmentLevel * enchantmentLevel);
                if (enchantmentEntry.isIn(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
                    cost *= 2;
                }
            } else {
                cost = 1;
                itemStack = new ItemStack(Items.BOOK);
            }
            return new TradeOffer(new TradedItem(CurrencyHelper.getClosest(cost).getItem(), cost), Optional.of(new TradedItem(Items.BOOK)), itemStack, maxUses, this.experience, multiplier);
        }
    }
}
