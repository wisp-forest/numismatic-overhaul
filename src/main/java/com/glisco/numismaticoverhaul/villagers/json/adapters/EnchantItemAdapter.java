package com.glisco.numismaticoverhaul.villagers.json.adapters;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import com.glisco.numismaticoverhaul.villagers.json.VillagerJsonHelper;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
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


        // TODO: Review, probably easier to copy usage in net.minecraft.village.TradeOffers.SellEnchantedToolFactory#create
        public TradeOffer create(Entity entity, Random random) {
            ItemStack itemStack = toEnchant.copy();

            ItemStack finalItemStack = itemStack.copy();
            if (finalItemStack.isOf(Items.BOOK)) {
                finalItemStack = new ItemStack(Items.ENCHANTED_BOOK);
            }

            Registry<Enchantment> enchantmentRegistry = entity.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT);

            List<Enchantment> possibleEnchantments = new ArrayList<>();

            for (Enchantment enchantment : enchantmentRegistry) {
                boolean isTreasure = enchantmentRegistry.getEntry(enchantment).isIn(EnchantmentTags.TREASURE);

                if (!allowTreasure && isTreasure) continue;
                if (!enchantment.isAcceptableItem(finalItemStack)) continue;

                possibleEnchantments.add(enchantment);
            }

            boolean hasEnchantment = false;

            for (Enchantment enchantment : possibleEnchantments) {
                if (random.nextInt(5) <= level && (!hasEnchantment || random.nextBoolean())) {
                    int enchantLevel = MathHelper.nextInt(random, Math.max(1, enchantment.getMinLevel()), Math.min(enchantment.getMaxLevel(), level));
                    RegistryEntry<Enchantment> registeredEnchantment = enchantmentRegistry.getEntry(enchantment);

                    if (finalItemStack.isOf(Items.ENCHANTED_BOOK)) {
                        EnchantmentHelper.apply(finalItemStack, builder -> builder.add(registeredEnchantment, enchantLevel));
                    } else {
                        finalItemStack.addEnchantment(registeredEnchantment, enchantLevel);
                    }

                    hasEnchantment = true;
                }
            }

            // Random Fallback in case nothing was added
            if (!hasEnchantment && !possibleEnchantments.isEmpty()) {
                Enchantment enchantment = possibleEnchantments.get(random.nextInt(possibleEnchantments.size()));
                int enchantLevel = MathHelper.nextInt(random, Math.max(1, enchantment.getMinLevel()), Math.min(enchantment.getMaxLevel(), level));
                RegistryEntry<Enchantment> registeredEnchantment = enchantmentRegistry.getEntry(enchantment);

                if (finalItemStack.isOf(Items.ENCHANTED_BOOK)) {
                    EnchantmentHelper.apply(finalItemStack, builder -> builder.add(registeredEnchantment, enchantLevel));
                } else {
                    finalItemStack.addEnchantment(registeredEnchantment, enchantLevel);
                }
            }

            int price = basePrice;
            ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(finalItemStack);

            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantments.getEnchantmentEntries()) {
                RegistryEntry<Enchantment> enchantment = entry.getKey();
                boolean isTreasure = enchantment.isIn(EnchantmentTags.TREASURE);

                // TODO: Review, not sure entry.GetIntValue is correct for level
                price += (int) (price * 0.10f + basePrice * (isTreasure ? 2f : 1f) *
                        entry.getIntValue() * MathHelper.nextFloat(random, .8f, 1.2f)
                        * (5f / (float) enchantment.value().getWeight()));
            }

            ItemStack itemAndCost = CurrencyHelper.getClosest(price);

            return new TradeOffer(new TradedItem(itemAndCost.getItem(), itemAndCost.getCount()), Optional.of(new TradedItem(toEnchant.getItem())), itemStack, maxUses, this.experience, multiplier);
        }
    }
}
