package com.glisco.numismaticoverhaul.villagers;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import javafx.util.Pair;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class VillagerTradesResourceListener implements SimpleResourceReloadListener<Pair<HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>>, Int2ObjectOpenHashMap<TradeOffers.Factory[]>>> {

    @Override
    public CompletableFuture<Pair<HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>>, Int2ObjectOpenHashMap<TradeOffers.Factory[]>>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        CompletableFuture<Pair<HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>>, Int2ObjectOpenHashMap<TradeOffers.Factory[]>>> future = new CompletableFuture<>();

        try {
            VillagerTradesHandler.registerTrades();
            future.complete(NumismaticVillagerTradesRegistry.getRegistryForLoading());
        } catch (IOException e) {
            e.printStackTrace();
            future.complete(new Pair<>(new HashMap<>(), new Int2ObjectOpenHashMap<>()));
        }

        return future;
    }

    @Override
    public CompletableFuture<Void> apply(Pair<HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>>, Int2ObjectOpenHashMap<TradeOffers.Factory[]>> data, ResourceManager manager, Profiler profiler, Executor executor) {
        data.getKey().forEach(TradeOffers.PROFESSION_TO_LEVELED_TRADE::replace);

        if (!data.getValue().isEmpty()) {
            TradeOffers.WANDERING_TRADER_TRADES.clear();
            TradeOffers.WANDERING_TRADER_TRADES.putAll(data.getValue());
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(NumismaticOverhaul.MOD_ID, "villager_data_loader");
    }
}
