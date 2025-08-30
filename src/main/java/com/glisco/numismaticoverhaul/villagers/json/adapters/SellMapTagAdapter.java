package com.glisco.numismaticoverhaul.villagers.json.adapters;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import com.glisco.numismaticoverhaul.villagers.json.VillagerJsonHelper;
import com.google.gson.JsonObject;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Locale;
import java.util.Optional;

public class SellMapTagAdapter extends TradeJsonAdapter {

    @Override
    @NotNull
    public TradeOffers.Factory deserialize(JsonObject json) {

        loadDefaultStats(json, true);

        VillagerJsonHelper.assertString(json, "tag");
        int price = json.get("price").getAsInt();

        final var structure = Identifier.of(JsonHelper.getString(json, "tag"));
        return new Factory(price, structure, max_uses, villager_experience, price_multiplier);
    }

    private static class Factory implements TradeOffers.Factory {
        private final int price;
        private final Identifier structureTagId;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public Factory(int price, Identifier feature, int maxUses, int experience, float multiplier) {
            this.price = price;
            this.structureTagId = feature;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = multiplier;
        }

        @Nullable
        public TradeOffer create(Entity entity, Random random) {
            if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return null;

            final var registry = serverWorld.getRegistryManager().get(RegistryKeys.STRUCTURE);
            final var entryList = registry.getOrCreateEntryList(TagKey.of(RegistryKeys.STRUCTURE, structureTagId));
            final var features = entryList.stream().toList();
            if (features.isEmpty()) {
                NumismaticOverhaul.LOGGER.error("Tried to create map to invalid structure " + this.structureTagId);
                return null;
            }

            final var result = serverWorld.getChunkManager().getChunkGenerator().locateStructure(
                    serverWorld,
                    RegistryEntryList.of(features.stream().toList()),
                    entity.getBlockPos(),
                    1500,
                    true
            );

            if (result == null) return null;
            final var blockPos = result.getFirst();
            final var feature = result.getSecond();

            // I "love" hardcoding these kinds of things
            var iconType = MapDecorationTypes.TARGET_X;
            if (feature.isIn(StructureTags.ON_TREASURE_MAPS)) {
                iconType = MapDecorationTypes.RED_X;
            }
            if (feature.isIn(StructureTags.ON_OCEAN_EXPLORER_MAPS)) {
                iconType = MapDecorationTypes.MONUMENT;
            }
            if (feature.isIn(StructureTags.ON_WOODLAND_EXPLORER_MAPS)) {
                iconType = MapDecorationTypes.MANSION;
            }
            if (feature.isIn(StructureTags.ON_DESERT_VILLAGE_MAPS)) {
                iconType = MapDecorationTypes.VILLAGE_DESERT;
            }
            if (feature.isIn(StructureTags.ON_SAVANNA_VILLAGE_MAPS)) {
                iconType = MapDecorationTypes.VILLAGE_SAVANNA;
            }
            if (feature.isIn(StructureTags.ON_PLAINS_VILLAGE_MAPS)) {
                iconType = MapDecorationTypes.VILLAGE_PLAINS;
            }
            if (feature.isIn(StructureTags.ON_TAIGA_VILLAGE_MAPS)) {
                iconType = MapDecorationTypes.VILLAGE_TAIGA;
            }
            if (feature.isIn(StructureTags.ON_SNOWY_VILLAGE_MAPS)) {
                iconType = MapDecorationTypes.VILLAGE_SNOWY;
            }
            if (feature.isIn(StructureTags.ON_JUNGLE_EXPLORER_MAPS)) {
                iconType = MapDecorationTypes.JUNGLE_TEMPLE;
            }
            if (feature.isIn(StructureTags.ON_SWAMP_EXPLORER_MAPS)) {
                iconType = MapDecorationTypes.SWAMP_HUT;
            }
            if (feature.isIn(StructureTags.ON_TRIAL_CHAMBERS_MAPS)) {
                iconType = MapDecorationTypes.TRIAL_CHAMBERS;
            }

            ItemStack itemStack = FilledMapItem.createMap(serverWorld, blockPos.getX(), blockPos.getZ(), (byte) 2, true, true);
            FilledMapItem.fillExplorationMap(serverWorld, itemStack);
            MapState.addDecorationsNbt(itemStack, blockPos, "+", iconType);
            itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("filled_map." + feature.getKey().get().getValue().getPath().toLowerCase(Locale.ROOT)));
            return new TradeOffer(CurrencyHelper.getClosestTradeItem(price), Optional.of(new TradedItem(Items.MAP)), itemStack, this.maxUses, this.experience, multiplier);
        }
    }
}
