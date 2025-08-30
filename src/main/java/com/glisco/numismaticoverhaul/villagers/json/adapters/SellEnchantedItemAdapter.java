package com.glisco.numismaticoverhaul.villagers.json.adapters;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import com.glisco.numismaticoverhaul.villagers.json.VillagerJsonHelper;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.enchantment.*;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SellEnchantedItemAdapter extends TradeJsonAdapter {

    @Override
    @NotNull
    public TradeOffers.Factory deserialize(JsonObject json) {

        loadDefaultStats(json, false);
        VillagerJsonHelper.assertInt(json, "level");

        boolean allow_treasure = VillagerJsonHelper.boolean_getOrDefault(json, "allow_treasure", false);

        int level = json.get("level").getAsInt();
        ItemStack itemToEnchant = VillagerJsonHelper.ItemStack_getOrDefault(json, "item", new ItemStack(Items.BOOK));
        int base_price = JsonHelper.getInt(json, "base_price", 200);

        return new Factory(itemToEnchant, max_uses, villager_experience, level, allow_treasure, price_multiplier, base_price);
    }

    private static class Factory implements TradeOffers.Factory {
        private final int experience;
        private final int maxUses;
        private final int level;
        private final boolean allowTreasure;
        private final ItemStack itemToEnchant;
        private final float multiplier;
        private final int basePrice;

        public Factory(ItemStack item, int maxUses, int experience, int level, boolean allowTreasure, float multiplier, int basePrice) {
            this.experience = experience;
            this.maxUses = maxUses;
            this.level = level;
            this.allowTreasure = allowTreasure;
            this.itemToEnchant = item;
            this.multiplier = multiplier;
            this.basePrice = basePrice;
        }

        public TradeOffer create(Entity entity, Random random) {
            ItemStack itemStack = itemToEnchant.copy();
            var enchantmentRegistry = entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT);
            var nonTreasureEnchants = enchantmentRegistry.getEntryList(EnchantmentTags.NON_TREASURE);
            var treasureRegistry = enchantmentRegistry.getEntryList(EnchantmentTags.TRADEABLE);
            var enchants = List.<EnchantmentLevelEntry>of();
            if (allowTreasure && treasureRegistry.isPresent()) {
                enchants = EnchantmentHelper.generateEnchantments(random, itemStack, level, treasureRegistry.get().stream());
            }
            else if (nonTreasureEnchants.isPresent()) {
                enchants = EnchantmentHelper.generateEnchantments(random, itemStack, level, nonTreasureEnchants.get().stream());
            }

            for (EnchantmentLevelEntry enchant : enchants) {
                itemStack.addEnchantment(enchant.enchantment, enchant.level);
            }

            int price = basePrice;
            var enchantments = EnchantmentHelper.getEnchantments(itemStack);

            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantments.getEnchantmentEntries()) {
                var enchantment = entry.getKey();
                var isTreasure = enchantment.isIn(EnchantmentTags.TREASURE);

                if (enchantment.isIn(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
                    price *= 2;
                }

                price += (int) (price * 0.10f + basePrice * (isTreasure ? 2f : 1f) *
                    entry.getIntValue() * MathHelper.nextFloat(random, .8f, 1.2f)
                    * (5f / (float) enchantment.value().getWeight()));
            }

            var itemAndCost = CurrencyHelper.getClosest(price);
            return new TradeOffer(new TradedItem(itemAndCost.getItem(), itemAndCost.getCount()), itemStack, maxUses, this.experience, multiplier);
        }
    }
}
