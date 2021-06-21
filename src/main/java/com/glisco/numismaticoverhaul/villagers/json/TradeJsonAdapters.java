package com.glisco.numismaticoverhaul.villagers.json;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.currency.CurrencyStack;
import com.glisco.numismaticoverhaul.villagers.exceptions.DeserializationException;
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

    public static class SellMap extends TradeJsonAdapter {

        @Override
        @NotNull
        public TradeOffers.Factory deserialize(JsonObject json) {

            loadDefaultStats(json, true);

            VillagerJsonHelper.assertString(json, "structure");

            StructureFeature<?> feature = Registry.STRUCTURE_FEATURE.get(new Identifier(json.get("structure").getAsString()));
            if (feature == null) {
                throw new DeserializationException("Structure " + json.get("structure").getAsString() + " not found");
            }

            CurrencyStack price = new CurrencyStack(json.get("price").getAsInt());

            MapIcon.Type iconType = MapIcon.Type.TARGET_POINT;
            if (feature == StructureFeature.MONUMENT) iconType = MapIcon.Type.MONUMENT;
            if (feature == StructureFeature.MANSION) iconType = MapIcon.Type.MANSION;

            return new SellMapFactory(price, feature, iconType, max_uses, villager_experience, price_multiplier);
        }
    }

    private static class SellMapFactory implements TradeOffers.Factory {
        private final CurrencyStack price;
        private final StructureFeature<?> structure;
        private final MapIcon.Type iconType;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public SellMapFactory(CurrencyStack price, StructureFeature<?> feature, MapIcon.Type iconType, int maxUses, int experience, float multiplier) {
            this.price = price;
            this.structure = feature;
            this.iconType = iconType;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = multiplier;
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
                    MapState.addDecorationsNbt(itemStack, blockPos, "+", this.iconType);
                    itemStack.setCustomName(new TranslatableText("filled_map." + this.structure.getName().toLowerCase(Locale.ROOT)));
                    return new TradeOffer(CurrencyHelper.getAsStacks(price, 1).get(0), new ItemStack(Items.MAP), itemStack, this.maxUses, this.experience, multiplier);
                } else {
                    return null;
                }
            }
        }
    }

    public static class SellStack extends TradeJsonAdapter {

        @Override
        @NotNull
        public TradeOffers.Factory deserialize(JsonObject json) {

            loadDefaultStats(json, true);

            VillagerJsonHelper.assertJsonObject(json, "sell");

            ItemStack sell = VillagerJsonHelper.getItemStackFromJson(json.get("sell").getAsJsonObject());

            CurrencyStack price = new CurrencyStack(json.get("price").getAsInt());

            return new SellStackFactory(sell, price, max_uses, villager_experience, price_multiplier);
        }
    }

    private static class SellStackFactory implements TradeOffers.Factory {
        private final ItemStack sell;
        private final int maxUses;
        private final int experience;
        private final CurrencyStack price;
        private final float multiplier;

        public SellStackFactory(ItemStack sell, CurrencyStack price, int maxUses, int experience, float multiplier) {
            this.sell = sell;
            this.maxUses = maxUses;
            this.experience = experience;
            this.price = price;
            this.multiplier = multiplier;
        }

        public TradeOffer create(Entity entity, Random random) {

            List<ItemStack> priceStacks = CurrencyHelper.getAsStacks(price, 2);

            if (priceStacks.size() > 1) {
                return new TradeOffer(priceStacks.get(0), priceStacks.get(1), sell, this.maxUses, this.experience, multiplier);
            } else {
                return new TradeOffer(priceStacks.get(0), sell, this.maxUses, this.experience, multiplier);
            }
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

            CurrencyStack price = new CurrencyStack(json.get("price").getAsInt());

            return new DimensionAwareSellStackFactory(sell, price, dimension, max_uses, villager_experience, price_multiplier);
        }
    }

    private static class DimensionAwareSellStackFactory implements TradeOffers.Factory {
        private final ItemStack sell;
        private final int maxUses;
        private final int experience;
        private final CurrencyStack price;
        private final float multiplier;
        private final String targetDimensionId;

        public DimensionAwareSellStackFactory(ItemStack sell, CurrencyStack price, String targetDimensionId, int maxUses, int experience, float multiplier) {
            this.sell = sell;
            this.maxUses = maxUses;
            this.experience = experience;
            this.price = price;
            this.multiplier = multiplier;
            this.targetDimensionId = targetDimensionId;
        }

        public TradeOffer create(Entity entity, Random random) {

            if (!entity.world.getRegistryKey().getValue().toString().equals(targetDimensionId)) return null;

            List<ItemStack> priceStacks = CurrencyHelper.getAsStacks(price, 2);

            if (priceStacks.size() > 1) {
                return new TradeOffer(priceStacks.get(0), priceStacks.get(1), sell, this.maxUses, this.experience, multiplier);
            } else {
                return new TradeOffer(priceStacks.get(0), sell, this.maxUses, this.experience, multiplier);
            }
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
            int cost = 2 + random.nextInt(5 + enchantmentLevel * 10) + 3 * enchantmentLevel;
            if (enchantment.isTreasure()) {
                cost *= 2;
            }

            return new TradeOffer(CurrencyHelper.getAsStacks(new CurrencyStack(cost), 1).get(0), new ItemStack(Items.BOOK), itemStack, maxUses, this.experience, multiplier);
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

            return new EnchantItemFactory(item, max_uses, villager_experience, level, allow_treasure, price_multiplier);
        }
    }

    private static class EnchantItemFactory implements TradeOffers.Factory {
        private final int experience;
        private final int maxUses;
        private final int level;
        private final boolean allowTreasure;
        private final ItemStack toEnchant;
        private final float multiplier;

        public EnchantItemFactory(ItemStack item, int maxUses, int experience, int level, boolean allowTreasure, float multiplier) {
            this.experience = experience;
            this.maxUses = maxUses;
            this.level = level;
            this.allowTreasure = allowTreasure;
            this.toEnchant = item;
            this.multiplier = multiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            ItemStack itemStack = toEnchant.copy();
            itemStack = EnchantmentHelper.enchant(random, itemStack, level, allowTreasure);

            int price = 8;

            for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get(itemStack).entrySet()) {
                price += entry.getKey().isTreasure() ? 1.0f : 2.0f * ((float) entry.getValue() / (float) entry.getKey().getMaxLevel()) * 12.0f * 0.5f * (10.0f / (float) entry.getKey().getRarity().getWeight());
            }

            return new TradeOffer(CurrencyHelper.getAsStacks(new CurrencyStack(price), 1).get(0), toEnchant, itemStack, maxUses, this.experience, multiplier);
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

            CurrencyStack price = new CurrencyStack(json.get("price").getAsInt());

            return new ProcessItemFactory(buy, sell, price, max_uses, villager_experience, price_multiplier);
        }
    }

    private static class ProcessItemFactory implements TradeOffers.Factory {
        private final ItemStack buy;
        private final CurrencyStack price;
        private final ItemStack sell;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public ProcessItemFactory(ItemStack buy, ItemStack sell, CurrencyStack price, int maxUses, int experience, float multiplier) {
            this.buy = buy;
            this.price = price;
            this.sell = sell;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = multiplier;
        }

        @Nullable
        public TradeOffer create(Entity entity, Random random) {
            return new TradeOffer(CurrencyHelper.getAsStacks(price, 1).get(0), buy, sell, this.maxUses, this.experience, this.multiplier);
        }
    }


    public static class SellDyedArmor extends TradeJsonAdapter {

        @Override
        public @NotNull TradeOffers.Factory deserialize(JsonObject json) {

            loadDefaultStats(json, true);

            VillagerJsonHelper.assertString(json, "item");

            CurrencyStack price = new CurrencyStack(json.get("price").getAsInt());
            Item item = VillagerJsonHelper.getItemFromID(json.get("item").getAsString());

            return new SellDyedArmorFactory(item, price, max_uses, villager_experience, price_multiplier);
        }
    }

    private static class SellDyedArmorFactory implements TradeOffers.Factory {
        private final Item sell;
        private final CurrencyStack price;
        private final int maxUses;
        private final int experience;
        private final float priceMultiplier;

        public SellDyedArmorFactory(Item item, CurrencyStack price, int maxUses, int experience, float priceMultiplier) {
            this.sell = item;
            this.price = price;
            this.maxUses = maxUses;
            this.experience = experience;
            this.priceMultiplier = priceMultiplier;
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
                return new TradeOffer(priceStacks.get(0), priceStacks.get(1), itemStack2, this.maxUses, this.experience, priceMultiplier);
            } else {
                return new TradeOffer(priceStacks.get(0), itemStack2, this.maxUses, this.experience, priceMultiplier);
            }

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

            CurrencyStack price = new CurrencyStack(json.get("price").getAsInt());
            ItemStack container_item = VillagerJsonHelper.getItemStackFromJson(json.get("container_item").getAsJsonObject());
            ItemStack buy_item = VillagerJsonHelper.getItemStackFromJson(json.get("buy_item").getAsJsonObject());

            return new SellPotionHoldingItemFactory(container_item, buy_item, price, max_uses, villager_experience, price_multiplier);
        }
    }

    private static class SellPotionHoldingItemFactory implements TradeOffers.Factory {
        private final ItemStack containerItem;
        private final ItemStack buyItem;

        private final CurrencyStack price;
        private final int maxUses;
        private final int experience;

        private final float priceMultiplier;

        public SellPotionHoldingItemFactory(ItemStack containerItem, ItemStack buyItem, CurrencyStack price, int maxUses, int experience, float priceMultiplier) {
            this.containerItem = containerItem;
            this.buyItem = buyItem;
            this.price = price;
            this.maxUses = maxUses;
            this.experience = experience;
            this.priceMultiplier = priceMultiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            ItemStack priceStack = CurrencyHelper.getAsStacks(price, 1).get(0);

            List<Potion> list = Registry.POTION.stream().filter((potionx) -> !potionx.getEffects().isEmpty() && BrewingRecipeRegistry.isBrewable(potionx)).collect(Collectors.toList());

            Potion potion = list.get(random.nextInt(list.size()));
            ItemStack itemStack2 = PotionUtil.setPotion(containerItem.copy(), potion);
            return new TradeOffer(priceStack, buyItem, itemStack2, this.maxUses, this.experience, this.priceMultiplier);
        }
    }

    public static class BuyItem extends TradeJsonAdapter {

        @Override
        @NotNull
        TradeOffers.Factory deserialize(JsonObject json) {

            loadDefaultStats(json, true);

            VillagerJsonHelper.assertJsonObject(json, "buy");

            CurrencyStack price = new CurrencyStack(json.get("price").getAsInt());
            ItemStack buy = VillagerJsonHelper.getItemStackFromJson(json.get("buy").getAsJsonObject());

            return new BuyItemFactory(buy, price, max_uses, villager_experience, price_multiplier);
        }
    }

    private static class BuyItemFactory implements TradeOffers.Factory {
        private final ItemStack buy;
        private final CurrencyStack price;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public BuyItemFactory(ItemStack buy, CurrencyStack price, int maxUses, int experience, float multiplier) {
            this.buy = buy;
            this.price = price;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = multiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            return new TradeOffer(buy, CurrencyHelper.getAsStacks(price, 1).get(0), this.maxUses, this.experience, this.multiplier);
        }
    }

}
