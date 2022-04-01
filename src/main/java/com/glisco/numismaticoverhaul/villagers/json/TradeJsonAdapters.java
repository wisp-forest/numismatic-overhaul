package com.glisco.numismaticoverhaul.villagers.json;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.gen.feature.ConfiguredStructureFeatures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class TradeJsonAdapters {

    public static class SellMap extends TradeJsonAdapter {

        @Override
        @NotNull
        public TradeOffers.Factory deserialize(JsonObject json) {

            loadDefaultStats(json, true);

            VillagerJsonHelper.assertString(json, "structure");
            int price = json.get("price").getAsInt();

            final var structure = new Identifier(JsonHelper.getString(json, "structure"));
            return new SellMapFactory(price, structure, max_uses, villager_experience, price_multiplier);
        }
    }

    private static class SellMapFactory implements TradeOffers.Factory {
        private final int price;
        private final Identifier structureId;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public SellMapFactory(int price, Identifier feature, int maxUses, int experience, float multiplier) {
            this.price = price;
            this.structureId = feature;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = multiplier;
        }

        @Nullable
        public TradeOffer create(Entity entity, Random random) {
            if (!(entity.world instanceof ServerWorld serverWorld)) return null;

            final var registry = serverWorld.getRegistryManager().get(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY);
            final var feature = registry.getEntry(registry.getKey(
                    registry.get(this.structureId)).get()).orElse(null);

            if (feature == null) {
                NumismaticOverhaul.LOGGER.error("Tried to create map to invalid structure " + this.structureId);
                return null;
            }

            final var result = serverWorld.getChunkManager().getChunkGenerator().locateStructure(serverWorld, RegistryEntryList.of(feature),
                    entity.getBlockPos(), 100, true);

            if (result == null) return null;
            final var blockPos = result.getFirst();

            var iconType = MapIcon.Type.TARGET_POINT;
            if (feature.matchesId(ConfiguredStructureFeatures.MONUMENT.getKey().get().getValue()))
                iconType = MapIcon.Type.MONUMENT;
            if (feature.matchesId(ConfiguredStructureFeatures.MANSION.getKey().get().getValue()))
                iconType = MapIcon.Type.MANSION;

            ItemStack itemStack = FilledMapItem.createMap(serverWorld, blockPos.getX(), blockPos.getZ(), (byte) 2, true, true);
            FilledMapItem.fillExplorationMap(serverWorld, itemStack);
            MapState.addDecorationsNbt(itemStack, blockPos, "+", iconType);
            itemStack.setCustomName(new TranslatableText("filled_map." + feature.getKey().get().getValue().getPath().toLowerCase(Locale.ROOT)));
            return new TradeOffer(CurrencyHelper.getClosest(price), new ItemStack(Items.MAP), itemStack, this.maxUses, this.experience, multiplier);
        }
    }

    public static class SellStack extends TradeJsonAdapter {

        @Override
        @NotNull
        public TradeOffers.Factory deserialize(JsonObject json) {

            loadDefaultStats(json, true);

            VillagerJsonHelper.assertJsonObject(json, "sell");

            ItemStack sell = VillagerJsonHelper.getItemStackFromJson(json.get("sell").getAsJsonObject());
            int price = json.get("price").getAsInt();

            return new SellStackFactory(sell, price, max_uses, villager_experience, price_multiplier);
        }
    }

    private static class SellStackFactory implements TradeOffers.Factory {
        private final ItemStack sell;
        private final int maxUses;
        private final int experience;
        private final int price;
        private final float multiplier;

        public SellStackFactory(ItemStack sell, int price, int maxUses, int experience, float multiplier) {
            this.sell = sell;
            this.maxUses = maxUses;
            this.experience = experience;
            this.price = price;
            this.multiplier = multiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            return new TradeOffer(CurrencyHelper.getClosest(price), sell, this.maxUses, this.experience, multiplier);
        }
    }

    public static class DimensionAwareSellStack extends TradeJsonAdapter {

        @Override
        @NotNull
        public TradeOffers.Factory deserialize(JsonObject json) {

            loadDefaultStats(json, true);

            VillagerJsonHelper.assertJsonObject(json, "sell");
            VillagerJsonHelper.assertString(json, "dimension");

            ItemStack sell = VillagerJsonHelper.getItemStackFromJson(json.get("sell").getAsJsonObject());
            String dimension = json.get("dimension").getAsString();

            int price = json.get("price").getAsInt();

            return new DimensionAwareSellStackFactory(sell, price, dimension, max_uses, villager_experience, price_multiplier);
        }
    }

    private static class DimensionAwareSellStackFactory implements TradeOffers.Factory {
        private final ItemStack sell;
        private final int maxUses;
        private final int experience;
        private final int price;
        private final float multiplier;
        private final String targetDimensionId;

        public DimensionAwareSellStackFactory(ItemStack sell, int price, String targetDimensionId, int maxUses, int experience, float multiplier) {
            this.sell = sell;
            this.maxUses = maxUses;
            this.experience = experience;
            this.price = price;
            this.multiplier = multiplier;
            this.targetDimensionId = targetDimensionId;
        }

        public TradeOffer create(Entity entity, Random random) {
            if (!entity.world.getRegistryKey().getValue().toString().equals(targetDimensionId)) return null;

            return new TradeOffer(CurrencyHelper.getClosest(price), sell, this.maxUses, this.experience, multiplier);
        }
    }

    public static class SellSingleEnchantment extends TradeJsonAdapter {

        @Override
        @NotNull
        public TradeOffers.Factory deserialize(JsonObject json) {
            loadDefaultStats(json, false);
            return new SellSingleEnchantmentFactory(max_uses, villager_experience, price_multiplier);
        }

    }

    private static class SellSingleEnchantmentFactory implements TradeOffers.Factory {
        private final int experience;
        private final int maxUses;
        private final float multiplier;

        public SellSingleEnchantmentFactory(int maxUses, int experience, float multiplier) {
            this.experience = experience;
            this.maxUses = maxUses;
            this.multiplier = multiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            List<Enchantment> list = Registry.ENCHANTMENT.stream().filter(Enchantment::isAvailableForEnchantedBookOffer).collect(Collectors.toList());
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


    public static class EnchantItem extends TradeJsonAdapter {

        @Override
        @NotNull
        public TradeOffers.Factory deserialize(JsonObject json) {

            loadDefaultStats(json, false);
            VillagerJsonHelper.assertInt(json, "level");

            boolean allow_treasure = VillagerJsonHelper.boolean_getOrDefault(json, "allow_treasure", false);

            int level = json.get("level").getAsInt();
            ItemStack item = VillagerJsonHelper.ItemStack_getOrDefault(json, "item", new ItemStack(Items.BOOK));
            int base_price = JsonHelper.getInt(json, "base_price", 200);

            return new EnchantItemFactory(item, max_uses, villager_experience, level, allow_treasure, price_multiplier, base_price);
        }
    }

    private static class EnchantItemFactory implements TradeOffers.Factory {
        private final int experience;
        private final int maxUses;
        private final int level;
        private final boolean allowTreasure;
        private final ItemStack toEnchant;
        private final float multiplier;
        private final int basePrice;

        public EnchantItemFactory(ItemStack item, int maxUses, int experience, int level, boolean allowTreasure, float multiplier, int basePrice) {
            this.experience = experience;
            this.maxUses = maxUses;
            this.level = level;
            this.allowTreasure = allowTreasure;
            this.toEnchant = item;
            this.multiplier = multiplier;
            this.basePrice = basePrice;
        }

        public TradeOffer create(Entity entity, Random random) {
            ItemStack itemStack = toEnchant.copy();
            itemStack = EnchantmentHelper.enchant(random, itemStack, level, allowTreasure);

            int price = basePrice;
            for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get(itemStack).entrySet()) {
                price += price * 0.10f + basePrice * (entry.getKey().isTreasure() ? 2f : 1f) *
                        entry.getValue() * MathHelper.nextFloat(random, .8f, 1.2f)
                        * (5f / (float) entry.getKey().getRarity().getWeight());
            }

            return new TradeOffer(CurrencyHelper.getClosest(price), toEnchant, itemStack, maxUses, this.experience, multiplier);
        }
    }


    public static class ProcessItem extends TradeJsonAdapter {

        @Override
        @NotNull
        public TradeOffers.Factory deserialize(JsonObject json) {

            loadDefaultStats(json, true);

            VillagerJsonHelper.assertJsonObject(json, "buy");
            VillagerJsonHelper.assertJsonObject(json, "sell");

            ItemStack sell = VillagerJsonHelper.getItemStackFromJson(json.get("sell").getAsJsonObject());
            ItemStack buy = VillagerJsonHelper.getItemStackFromJson(json.get("buy").getAsJsonObject());

            int price = json.get("price").getAsInt();

            return new ProcessItemFactory(buy, sell, price, max_uses, villager_experience, price_multiplier);
        }
    }

    private static class ProcessItemFactory implements TradeOffers.Factory {
        private final ItemStack buy;
        private final int price;
        private final ItemStack sell;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public ProcessItemFactory(ItemStack buy, ItemStack sell, int price, int maxUses, int experience, float multiplier) {
            this.buy = buy;
            this.price = price;
            this.sell = sell;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = multiplier;
        }

        @Nullable
        public TradeOffer create(Entity entity, Random random) {
            return new TradeOffer(CurrencyHelper.getClosest(price), buy, sell, this.maxUses, this.experience, this.multiplier);
        }
    }


    public static class SellDyedArmor extends TradeJsonAdapter {

        @Override
        public @NotNull TradeOffers.Factory deserialize(JsonObject json) {

            loadDefaultStats(json, true);

            VillagerJsonHelper.assertString(json, "item");

            int price = json.get("price").getAsInt();
            Item item = VillagerJsonHelper.getItemFromID(json.get("item").getAsString());

            return new SellDyedArmorFactory(item, price, max_uses, villager_experience, price_multiplier);
        }
    }

    private static class SellDyedArmorFactory implements TradeOffers.Factory {
        private final Item sell;
        private final int price;
        private final int maxUses;
        private final int experience;
        private final float priceMultiplier;

        public SellDyedArmorFactory(Item item, int price, int maxUses, int experience, float priceMultiplier) {
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

    public static class SellPotionContainerItem extends TradeJsonAdapter {

        @Override
        @NotNull
        TradeOffers.Factory deserialize(JsonObject json) {

            loadDefaultStats(json, true);

            VillagerJsonHelper.assertJsonObject(json, "container_item");
            VillagerJsonHelper.assertJsonObject(json, "buy_item");

            int price = json.get("price").getAsInt();
            ItemStack container_item = VillagerJsonHelper.getItemStackFromJson(json.get("container_item").getAsJsonObject());
            ItemStack buy_item = VillagerJsonHelper.getItemStackFromJson(json.get("buy_item").getAsJsonObject());

            return new SellPotionHoldingItemFactory(container_item, buy_item, price, max_uses, villager_experience, price_multiplier);
        }
    }

    private static class SellPotionHoldingItemFactory implements TradeOffers.Factory {
        private final ItemStack containerItem;
        private final ItemStack buyItem;

        private final int price;
        private final int maxUses;
        private final int experience;

        private final float priceMultiplier;

        public SellPotionHoldingItemFactory(ItemStack containerItem, ItemStack buyItem, int price, int maxUses, int experience, float priceMultiplier) {
            this.containerItem = containerItem;
            this.buyItem = buyItem;
            this.price = price;
            this.maxUses = maxUses;
            this.experience = experience;
            this.priceMultiplier = priceMultiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            List<Potion> list = Registry.POTION.stream().filter((potionx) -> !potionx.getEffects().isEmpty() && BrewingRecipeRegistry.isBrewable(potionx)).collect(Collectors.toList());

            Potion potion = list.get(random.nextInt(list.size()));
            ItemStack itemStack2 = PotionUtil.setPotion(containerItem.copy(), potion);
            return new TradeOffer(CurrencyHelper.getClosest(price), buyItem, itemStack2, this.maxUses, this.experience, this.priceMultiplier);
        }
    }

    public static class BuyItem extends TradeJsonAdapter {

        @Override
        @NotNull
        TradeOffers.Factory deserialize(JsonObject json) {

            loadDefaultStats(json, true);

            VillagerJsonHelper.assertJsonObject(json, "buy");

            int price = json.get("price").getAsInt();
            ItemStack buy = VillagerJsonHelper.getItemStackFromJson(json.get("buy").getAsJsonObject());

            return new BuyItemFactory(buy, price, max_uses, villager_experience, price_multiplier);
        }
    }

    private static class BuyItemFactory implements TradeOffers.Factory {
        private final ItemStack buy;
        private final int price;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public BuyItemFactory(ItemStack buy, int price, int maxUses, int experience, float multiplier) {
            this.buy = buy;
            this.price = price;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = multiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            return new TradeOffer(buy, CurrencyHelper.getClosest(price), this.maxUses, this.experience, this.multiplier);
        }
    }

}
