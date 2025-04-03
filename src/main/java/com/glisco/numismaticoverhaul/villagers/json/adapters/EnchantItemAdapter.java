package com.glisco.numismaticoverhaul.villagers.json.adapters;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import com.glisco.numismaticoverhaul.villagers.json.VillagerJsonHelper;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnchantItemAdapter extends TradeJsonAdapter {

    @Override
    @NotNull
    public TradeOffers.Factory deserialize(JsonObject json) {

        loadDefaultStats(json, false);
        VillagerJsonHelper.assertInt(json, "level");

        boolean allow_treasure = VillagerJsonHelper.boolean_getOrDefault(json, "allow_treasure", false);

        int level = json.get("level").getAsInt();
        ItemStack item = VillagerJsonHelper.ItemStack_getOrDefault(json, "item", new ItemStack(Items.BOOK));
        int base_price = JsonHelper.getInt(json, "base_price", 200);

        return new Factory(item, max_uses, villager_experience, level, allow_treasure, price_multiplier, base_price);
    }

    private static class Factory implements TradeOffers.Factory {
        private final int experience;
        private final int maxUses;
        private final int level;
        private final boolean allowTreasure;
        private final ItemStack toEnchant;
        private final float multiplier;
        private final int basePrice;

        public Factory(ItemStack item, int maxUses, int experience, int level, boolean allowTreasure, float multiplier, int basePrice) {
            this.experience = experience;
            this.maxUses = maxUses;
            this.level = level;
            this.allowTreasure = allowTreasure;
            this.toEnchant = item;
            this.multiplier = multiplier;
            this.basePrice = basePrice;
        }


        // TODO: Review
        public TradeOffer create(Entity entity, Random random) {
            var itemStack = toEnchant.copy();

            var finalItemStack = itemStack.copy();
            if (finalItemStack.isOf(Items.BOOK)) {
                finalItemStack = new ItemStack(Items.ENCHANTED_BOOK);
            }

            var enchantmentRegistry = entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT);
            var tradeRegistry = enchantmentRegistry.getEntryList(EnchantmentTags.TRADEABLE);
            var treasureRegistry = enchantmentRegistry.getEntryList(EnchantmentTags.TREASURE);

            // Can't be var
            List<Enchantment> possibleEnchantments = new ArrayList<>();

            if (tradeRegistry.isPresent()) {
                for (RegistryEntry<Enchantment> enchantment : tradeRegistry.get()) {
                    if (!enchantment.value().isAcceptableItem(finalItemStack)) continue;

                    possibleEnchantments.add(enchantment.value());
                }
            }

            if (allowTreasure && treasureRegistry.isPresent()) {
                for (RegistryEntry<Enchantment> enchantment : treasureRegistry.get()) {
                    if (!enchantment.value().isAcceptableItem(finalItemStack)) continue;

                    possibleEnchantments.add(enchantment.value());
                }
            }

            var hasEnchantment = false;
            var enchantLevel = 1;

            // Randomizer to apply from Enchant List
            for (Enchantment enchantment : possibleEnchantments) {
                if (random.nextInt(5) <= level && (!hasEnchantment || random.nextBoolean())) {
                    enchantLevel = MathHelper.nextInt(random, Math.max(1, enchantment.getMinLevel()), Math.min(enchantment.getMaxLevel(), level));
                    var registeredEnchantment = enchantmentRegistry.getEntry(enchantment);

                    if (finalItemStack.isOf(Items.ENCHANTED_BOOK)) {
                        finalItemStack = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(registeredEnchantment, enchantLevel));
                    } else {
                        finalItemStack.addEnchantment(registeredEnchantment, enchantLevel);
                    }

                    hasEnchantment = true;
                }
            }

            // Random Fallback in case nothing was added
            if (!hasEnchantment) {
                var optional = entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getRandomEntry(EnchantmentTags.TRADEABLE, random);
                if (optional.isPresent()) {
                    var registryEntry = optional.get();
                    var enchantment = registryEntry.value();
                    enchantLevel = MathHelper.nextInt(random, Math.max(1, enchantment.getMinLevel()), Math.min(enchantment.getMaxLevel(), level));
                    if (finalItemStack.isOf(Items.ENCHANTED_BOOK)) {
                        finalItemStack = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(registryEntry, enchantLevel));
                    } else {
                        finalItemStack.addEnchantment(registryEntry, enchantLevel);
                    }
                }
            }

            int price = basePrice;
            var enchantments = EnchantmentHelper.getEnchantments(finalItemStack);

            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantments.getEnchantmentEntries()) {
                var enchantment = entry.getKey();
                var isTreasure = enchantment.isIn(EnchantmentTags.TREASURE);

                if (enchantment.isIn(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
                    price *= 2;
                }

                // TODO: Review, not sure if math is correct
                price += (int) (price * 0.10f + basePrice * (isTreasure ? 2f : 1f) *
                        enchantLevel * MathHelper.nextFloat(random, .8f, 1.2f)
                        * (5f / (float) enchantment.value().getWeight()));
            }

            var itemAndCost = CurrencyHelper.getClosest(price);

            return new TradeOffer(new TradedItem(itemAndCost.getItem(), itemAndCost.getCount()), Optional.of(new TradedItem(toEnchant.getItem())), finalItemStack, maxUses, this.experience, multiplier);
        }
    }
}
