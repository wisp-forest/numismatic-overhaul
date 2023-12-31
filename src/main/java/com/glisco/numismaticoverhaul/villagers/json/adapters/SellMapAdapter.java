package com.glisco.numismaticoverhaul.villagers.json.adapters;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import com.glisco.numismaticoverhaul.villagers.json.VillagerJsonHelper;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import io.wispforest.owo.util.RegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapIcon.Type;
import net.minecraft.item.map.MapState;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.gen.structure.StructureKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class SellMapAdapter extends TradeJsonAdapter {

    @Override
    @NotNull
    public TradeOffers.Factory deserialize(JsonObject json) {

        loadDefaultStats(json, true);

        VillagerJsonHelper.assertString(json, "structure");
        int price = json.get("price").getAsInt();

        final var structure = new Identifier(JsonHelper.getString(json, "structure"));
        return new Factory(price, structure, max_uses, villager_experience, price_multiplier);
    }

    private static class Factory implements TradeOffers.Factory {
        private final int price;
        private final Identifier structureId;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public Factory(int price, Identifier feature, int maxUses, int experience, float multiplier) {
            this.price = price;
            this.structureId = feature;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = multiplier;
        }

        @Nullable
        public TradeOffer create(Entity entity, Random random) {
            if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return null;

            final var registry = serverWorld.getRegistryManager().get(RegistryKeys.STRUCTURE);
            final var feature = RegistryAccess.getEntry(registry, this.structureId);

            if (feature == null || feature.getKey().isEmpty()) {
                NumismaticOverhaul.LOGGER.error("Tried to create map to invalid structure " + this.structureId);
                return null;
            }

            final var result = serverWorld.getChunkManager().getChunkGenerator().locateStructure(
                    serverWorld,
                    RegistryEntryList.of(feature),
                    entity.getBlockPos(),
                    1500,
                    true
            );

            if (result == null) return null;
            final var blockPos = result.getFirst();

            var iconType = MapIcon.Type.TARGET_X;
            if (feature.matchesId(StructureKeys.MONUMENT.getValue()))
                iconType = MapIcon.Type.MONUMENT;
            if (feature.matchesId(StructureKeys.MANSION.getValue()))
                iconType = MapIcon.Type.MANSION;
            if (feature.matchesId(StructureKeys.PILLAGER_OUTPOST.getValue()))
                iconType = MapIcon.Type.TARGET_POINT;

            ItemStack itemStack = FilledMapItem.createMap(serverWorld, blockPos.getX(), blockPos.getZ(), (byte) 2, true, true);
            FilledMapItem.fillExplorationMap(serverWorld, itemStack);
            MapState.addDecorationsNbt(itemStack, blockPos, "+", iconType);
            itemStack.setCustomName(Text.translatable("filled_map." + feature.getKey().get().getValue().getPath().toLowerCase(Locale.ROOT)));
            return new TradeOffer(CurrencyHelper.getClosest(price), new ItemStack(Items.MAP), itemStack, this.maxUses, this.experience, multiplier);
        }
    }
}
