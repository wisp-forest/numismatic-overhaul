package com.glisco.numismaticoverhaul.villagers;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyStack;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
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
            if (price.getRequiredCurrencyTypes() > 1) {
                throw new JsonSyntaxException("Price " + price.getRawValue() + " requires more than one currency type");
            }

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
                    return new TradeOffer(price.getAsItemStackList().get(0), new ItemStack(Items.MAP), itemStack, this.maxUses, this.experience, 0.2F);
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

            ItemStack sell = TradeJsonAdapter.getItemStackFromJson(json.get("sell").getAsJsonObject());

            CurrencyStack price = new CurrencyStack(json.get("price").getAsInt());

            return new SellStackFactory(sell, price, maxUses, villagerExperience);
        }
    }

    private static class SellStackFactory implements TradeOffers.Factory {
        private final ItemStack sell;
        private final int maxUses;
        private final int experience;
        private final float multiplier;
        private final CurrencyStack price;

        public SellStackFactory(ItemStack sell, CurrencyStack price, int maxUses, int experience) {
            this.sell = sell;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = 0.05F;
            this.price = price;
        }

        public TradeOffer create(Entity entity, Random random) {
            if (price.getRequiredCurrencyTypes() > 2) {
                return new TradeOffer(MoneyBagItem.create(price.getRawValue()), sell.copy(), this.maxUses, this.experience, this.multiplier);
            } else if (price.getRequiredCurrencyTypes() == 2) {
                List<ItemStack> buy = price.getAsItemStackList();
                return new TradeOffer(buy.get(0), buy.get(1), sell.copy(), this.maxUses, this.experience, this.multiplier);
            } else {
                return new TradeOffer(price.getAsItemStackList().get(0), sell.copy(), this.maxUses, this.experience, this.multiplier);
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

            if (cost > 99) {
                cost = 99;
            }

            return new TradeOffer(new ItemStack(NumismaticOverhaul.SILVER_COIN, cost), new ItemStack(Items.BOOK), itemStack, maxUses, this.experience, 0.2F);
        }
    }

    public static class EnchantBook implements TradeJsonAdapter {

        @Override
        @NotNull
        public TradeOffers.Factory deserialize(JsonObject json) {

            VillagerJsonHelper.assertElement(json, "level");

            int maxUses = VillagerJsonHelper.int_getOrDefault(json, "max_uses", 12);
            int villagerExperience = VillagerJsonHelper.int_getOrDefault(json, "villager_experience", 5);
            boolean allow_treasure = VillagerJsonHelper.boolean_getOrDefault(json, "allow_treasure", false);

            int level = json.get("level").getAsInt();

            return new EnchantBookFactory(maxUses, villagerExperience, level, allow_treasure);
        }
    }

    private static class EnchantBookFactory implements TradeOffers.Factory {
        private final int experience;
        private final int maxUses;
        private final int level;
        private final boolean allowTreasure;

        public EnchantBookFactory(int maxUses, int experience, int level, boolean allowTreasure) {
            this.experience = experience;
            this.maxUses = maxUses;
            this.level = level;
            this.allowTreasure = allowTreasure;
        }

        public TradeOffer create(Entity entity, Random random) {
            ItemStack itemStack = new ItemStack(Items.BOOK);
            itemStack = EnchantmentHelper.enchant(random, itemStack, level, allowTreasure);

            int price = 8;

            for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get(itemStack).entrySet()) {
                price += entry.getKey().isTreasure() ? 1.0f : 2.0f * ((float) entry.getValue() / (float) entry.getKey().getMaxLevel()) * 12.0f * 0.5f * (10.0f / (float) entry.getKey().getRarity().getWeight());
            }

            return new TradeOffer(new ItemStack(NumismaticOverhaul.SILVER_COIN, price), new ItemStack(Items.BOOK), itemStack, maxUses, this.experience, 0.2F);
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

            ItemStack sell = TradeJsonAdapter.getItemStackFromJson(json.get("sell").getAsJsonObject());
            ItemStack buy = TradeJsonAdapter.getItemStackFromJson(json.get("buy").getAsJsonObject());

            CurrencyStack price = new CurrencyStack(json.get("price").getAsInt());
            if (price.getRequiredCurrencyTypes() > 1) {
                throw new JsonSyntaxException("Price " + price.getRawValue() + " requires more than one currency type");
            }

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
            return new TradeOffer(price.getAsItemStackList().get(0), buy, sell, this.maxUses, this.experience, this.multiplier);
        }
    }

}
