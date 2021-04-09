package com.glisco.numismaticoverhaul.villagers;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.currency.CurrencyStack;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.gen.feature.StructureFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class TradeJsonAdapters {

    public static class SellMap implements TradeJsonAdapter {

        @Override
        @NotNull
        public TradeOffers.Factory deserialize(JsonObject json) {

            VillagerJsonHelper.assertElement(json, "structure");
            VillagerJsonHelper.assertElement(json, "price");

            int maxUses = VillagerJsonHelper.int_getOrDefault(json, "max_uses", 12);
            int villagerExperience = VillagerJsonHelper.int_getOrDefault(json, "villager_experience", 5);

            StructureFeature<?> feature = Registry.STRUCTURE_FEATURE.get(new Identifier(json.get("structure").getAsString()));
            if (feature == null) {
                throw new JsonSyntaxException("Structure " + json.get("structure").getAsString() + " not found");
            }

            CurrencyStack price = new CurrencyStack(json.get("price").getAsInt());

            MapIcon.Type iconType = MapIcon.Type.TARGET_POINT;
            if (feature == StructureFeature.MONUMENT) iconType = MapIcon.Type.MONUMENT;
            if (feature == StructureFeature.MANSION) iconType = MapIcon.Type.MANSION;

            return new SellMapFactory(price, feature, iconType, maxUses, villagerExperience);
        }
    }

    private static class SellMapFactory implements TradeOffers.Factory {
        private final CurrencyStack price;
        private final StructureFeature<?> structure;
        private final MapIcon.Type iconType;
        private final int maxUses;
        private final int experience;

        public SellMapFactory(CurrencyStack price, StructureFeature<?> feature, MapIcon.Type iconType, int maxUses, int experience) {
            this.price = price;
            this.structure = feature;
            this.iconType = iconType;
            this.maxUses = maxUses;
            this.experience = experience;
        }

        @Nullable
        public TradeOffer create(Entity entity, Random random) {
            if (!(entity.world instanceof ServerWorld)) {
                return null;
            } else {
                ServerWorld serverWorld = (ServerWorld) entity.world;
                BlockPos blockPos = serverWorld.locateStructure(this.structure, entity.getBlockPos(), 100, true);
                if (blockPos != null) {
                    ItemStack itemStack = FilledMapItem.createMap(serverWorld, blockPos.getX(), blockPos.getZ(), (byte) 2, true, true);
                    FilledMapItem.fillExplorationMap(serverWorld, itemStack);
                    MapState.addDecorationsTag(itemStack, blockPos, "+", this.iconType);
                    itemStack.setCustomName(new TranslatableText("filled_map." + this.structure.getName().toLowerCase(Locale.ROOT)));
                    return new TradeOffer(CurrencyHelper.getAsStacks(price, 1).get(0), new ItemStack(Items.MAP), itemStack, this.maxUses, this.experience, 0.2F);
                } else {
                    return null;
                }
            }
        }
    }

    public static class SellStack implements TradeJsonAdapter {

        @Override
        @NotNull
        public TradeOffers.Factory deserialize(JsonObject json) {

            VillagerJsonHelper.assertElement(json, "price");
            VillagerJsonHelper.assertElement(json, "sell");

            int maxUses = VillagerJsonHelper.int_getOrDefault(json, "max_uses", 12);
            int villagerExperience = VillagerJsonHelper.int_getOrDefault(json, "villager_experience", 5);

            ItemStack sell = VillagerJsonHelper.getItemStackFromJson(json.get("sell").getAsJsonObject());

            CurrencyStack price = new CurrencyStack(json.get("price").getAsInt());

            return new SellStackFactory(sell, price, maxUses, villagerExperience);
        }
    }

    private static class SellStackFactory implements TradeOffers.Factory {
        private final ItemStack sell;
        private final int maxUses;
        private final int experience;
        private final CurrencyStack price;

        public SellStackFactory(ItemStack sell, CurrencyStack price, int maxUses, int experience) {
            this.sell = sell;
            this.maxUses = maxUses;
            this.experience = experience;
            this.price = price;
        }

        public TradeOffer create(Entity entity, Random random) {

            List<ItemStack> priceStacks = CurrencyHelper.getAsStacks(price, 2);

            if (priceStacks.size() > 1) {
                return new TradeOffer(priceStacks.get(0), priceStacks.get(1), sell, this.maxUses, this.experience, 0.05F);
            } else {
                return new TradeOffer(priceStacks.get(0), sell, this.maxUses, this.experience, 0.05F);
            }
        }
    }

    public static class SellSingleEnchantment implements TradeJsonAdapter {

        @Override
        @NotNull
        public TradeOffers.Factory deserialize(JsonObject json) {

            int maxUses = VillagerJsonHelper.int_getOrDefault(json, "max_uses", 12);
            int villagerExperience = VillagerJsonHelper.int_getOrDefault(json, "villager_experience", 5);

            return new SellSingleEnchantmentFactory(maxUses, villagerExperience);
        }
    }

    private static class SellSingleEnchantmentFactory implements TradeOffers.Factory {
        private final int experience;
        private final int maxUses;

        public SellSingleEnchantmentFactory(int maxUses, int experience) {
            this.experience = experience;
            this.maxUses = maxUses;
        }

        public TradeOffer create(Entity entity, Random random) {
            List<Enchantment> list = Registry.ENCHANTMENT.stream().filter(Enchantment::isAvailableForEnchantedBookOffer).collect(Collectors.toList());
            Enchantment enchantment = list.get(random.nextInt(list.size()));

            int enchantmentLevel = MathHelper.nextInt(random, enchantment.getMinLevel(), enchantment.getMaxLevel());

            ItemStack itemStack = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantment, enchantmentLevel));
            int cost = 2 + random.nextInt(5 + enchantmentLevel * 10) + 3 * enchantmentLevel;
            if (enchantment.isTreasure()) {
                cost *= 2;
            }

            return new TradeOffer(CurrencyHelper.getAsStacks(new CurrencyStack(cost), 1).get(0), new ItemStack(Items.BOOK), itemStack, maxUses, this.experience, 0.2F);
        }
    }


    public static class EnchantItem implements TradeJsonAdapter {

        @Override
        @NotNull
        public TradeOffers.Factory deserialize(JsonObject json) {

            VillagerJsonHelper.assertElement(json, "level");

            int maxUses = VillagerJsonHelper.int_getOrDefault(json, "max_uses", 12);
            int villagerExperience = VillagerJsonHelper.int_getOrDefault(json, "villager_experience", 5);
            boolean allow_treasure = VillagerJsonHelper.boolean_getOrDefault(json, "allow_treasure", false);

            int level = json.get("level").getAsInt();
            ItemStack item = VillagerJsonHelper.ItemStack_getOrDefault(json, "item", new ItemStack(Items.BOOK));

            return new EnchantItemFactory(item, maxUses, villagerExperience, level, allow_treasure);
        }
    }

    private static class EnchantItemFactory implements TradeOffers.Factory {
        private final int experience;
        private final int maxUses;
        private final int level;
        private final boolean allowTreasure;
        private final ItemStack toEnchant;

        public EnchantItemFactory(ItemStack item, int maxUses, int experience, int level, boolean allowTreasure) {
            this.experience = experience;
            this.maxUses = maxUses;
            this.level = level;
            this.allowTreasure = allowTreasure;
            this.toEnchant = item;
        }

        public TradeOffer create(Entity entity, Random random) {
            ItemStack itemStack = toEnchant.copy();
            itemStack = EnchantmentHelper.enchant(random, itemStack, level, allowTreasure);

            int price = 8;

            for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get(itemStack).entrySet()) {
                price += entry.getKey().isTreasure() ? 1.0f : 2.0f * ((float) entry.getValue() / (float) entry.getKey().getMaxLevel()) * 12.0f * 0.5f * (10.0f / (float) entry.getKey().getRarity().getWeight());
            }

            return new TradeOffer(CurrencyHelper.getAsStacks(new CurrencyStack(price), 1).get(0), toEnchant, itemStack, maxUses, this.experience, 0.2F);
        }
    }


    public static class ProcessItem implements TradeJsonAdapter {

        @Override
        @NotNull
        public TradeOffers.Factory deserialize(JsonObject json) {

            VillagerJsonHelper.assertElement(json, "price");
            VillagerJsonHelper.assertElement(json, "buy");
            VillagerJsonHelper.assertElement(json, "sell");

            int maxUses = VillagerJsonHelper.int_getOrDefault(json, "max_uses", 12);
            int villagerExperience = VillagerJsonHelper.int_getOrDefault(json, "villager_experience", 5);

            ItemStack sell = VillagerJsonHelper.getItemStackFromJson(json.get("sell").getAsJsonObject());
            ItemStack buy = VillagerJsonHelper.getItemStackFromJson(json.get("buy").getAsJsonObject());

            CurrencyStack price = new CurrencyStack(json.get("price").getAsInt());

            return new ProcessItemFactory(buy, sell, price, maxUses, villagerExperience);
        }
    }

    private static class ProcessItemFactory implements TradeOffers.Factory {
        private final ItemStack buy;
        private final CurrencyStack price;
        private final ItemStack sell;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public ProcessItemFactory(ItemStack buy, ItemStack sell, CurrencyStack price, int maxUses, int experience) {
            this.buy = buy;
            this.price = price;
            this.sell = sell;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = 0.05F;
        }

        @Nullable
        public TradeOffer create(Entity entity, Random random) {
            return new TradeOffer(CurrencyHelper.getAsStacks(price, 1).get(0), buy, sell, this.maxUses, this.experience, this.multiplier);
        }
    }


    public static class SellDyedArmor implements TradeJsonAdapter {

        @Override
        public @NotNull TradeOffers.Factory deserialize(JsonObject json) {

            VillagerJsonHelper.assertElement(json, "item");
            VillagerJsonHelper.assertElement(json, "price");

            int maxUses = VillagerJsonHelper.int_getOrDefault(json, "max_uses", 12);
            int villagerExperience = VillagerJsonHelper.int_getOrDefault(json, "villager_experience", 5);

            CurrencyStack price = new CurrencyStack(json.get("price").getAsInt());
            Item item = VillagerJsonHelper.getItemFromID(json.get("item").getAsString());

            return new SellDyedArmorFactory(item, price, maxUses, villagerExperience);
        }
    }

    private static class SellDyedArmorFactory implements TradeOffers.Factory {
        private final Item sell;
        private final CurrencyStack price;
        private final int maxUses;
        private final int experience;

        public SellDyedArmorFactory(Item item, CurrencyStack price, int maxUses, int experience) {
            this.sell = item;
            this.price = price;
            this.maxUses = maxUses;
            this.experience = experience;
        }

        public TradeOffer create(Entity entity, Random random) {
            ItemStack itemStack2 = new ItemStack(this.sell);
            if (this.sell instanceof DyeableArmorItem) {
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

            List<ItemStack> priceStacks = CurrencyHelper.getAsStacks(price, 2);

            if (priceStacks.size() > 1) {
                return new TradeOffer(priceStacks.get(0), priceStacks.get(1), itemStack2, this.maxUses, this.experience, 0.05F);
            } else {
                return new TradeOffer(priceStacks.get(0), itemStack2, this.maxUses, this.experience, 0.05F);
            }

        }

        private static DyeItem getDye(Random random) {
            return DyeItem.byColor(DyeColor.byId(random.nextInt(16)));
        }
    }

}
