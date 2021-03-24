package com.glisco.numismaticoverhaul.villagers;

import com.glisco.numismaticoverhaul.currency.CurrencyStack;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.gen.feature.StructureFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public class TradeJsonAdapters {

    public static class SellMapForCurrency implements TradeJsonAdapter {

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

            return new SellMapForCurrencyFactory(price, feature, iconType, maxUses, villagerExperience);
        }
    }

    public static class SellStackForCurrency implements TradeJsonAdapter {

        @Override
        @NotNull
        public TradeOffers.Factory deserialize(JsonObject json) {

            VillagerJsonHelper.assertElement(json, "price");
            VillagerJsonHelper.assertElement(json, "sell");

            int maxUses = VillagerJsonHelper.int_getOrDefault(json, "max_uses", 12);
            int villagerExperience = VillagerJsonHelper.int_getOrDefault(json, "villager_experience", 5);

            ItemStack sell = TradeJsonAdapter.getItemStackFromJson(json.get("sell").getAsJsonObject());

            CurrencyStack price = new CurrencyStack(json.get("price").getAsInt());
            if (price.getRequiredCurrencyTypes() > 2) {
                throw new JsonSyntaxException("Price " + price.getRawValue() + " requires more than two currency types");
            }

            return new SellStackForCurrencyTradeOffer(sell, price, maxUses, villagerExperience);
        }
    }

    static class SellMapForCurrencyFactory implements TradeOffers.Factory {
        private final CurrencyStack price;
        private final StructureFeature<?> structure;
        private final MapIcon.Type iconType;
        private final int maxUses;
        private final int experience;

        public SellMapForCurrencyFactory(CurrencyStack price, StructureFeature<?> feature, MapIcon.Type iconType, int maxUses, int experience) {
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
                    return new TradeOffer(new ItemStack(Items.MAP), price.getAsItemStackList().get(0), itemStack, this.maxUses, this.experience, 0.2F);
                } else {
                    return null;
                }
            }
        }
    }

    static class SellStackForCurrencyTradeOffer implements TradeOffers.Factory {
        private final ItemStack sell;
        private final int maxUses;
        private final int experience;
        private final float multiplier;
        private final CurrencyStack price;

        public SellStackForCurrencyTradeOffer(ItemStack sell, CurrencyStack price, int maxUses, int experience) {
            this.sell = sell;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = 0.05F;
            this.price = price;
        }

        public TradeOffer create(Entity entity, Random random) {
            if (price.getRequiredCurrencyTypes() == 2) {
                List<ItemStack> buy = price.getAsItemStackList();
                return new TradeOffer(buy.get(0), buy.get(1), sell.copy(), this.maxUses, this.experience, this.multiplier);
            } else {
                return new TradeOffer(price.getAsItemStackList().get(0), sell.copy(), this.maxUses, this.experience, this.multiplier);
            }
        }
    }

}
