package com.glisco.numismaticoverhaul.villagers;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import javafx.util.Pair;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NumismaticVillagerTradesRegistry {

    private static final HashMap<VillagerProfession, Int2ObjectOpenHashMap<List<TradeOffers.Factory>>> TRADES_REGISTRY = new HashMap<>();
    private static final Int2ObjectOpenHashMap<List<TradeOffers.Factory>> WANDERING_TRADER_REGISTRY = new Int2ObjectOpenHashMap<>();

    public static void registerVillagerTrade(VillagerProfession profession, int level, TradeOffers.Factory trade) {
        Int2ObjectOpenHashMap<List<TradeOffers.Factory>> villagerMap = getOrDefaultAndAdd(TRADES_REGISTRY, profession, new Int2ObjectOpenHashMap<>());
        List<TradeOffers.Factory> trades = getOrDefaultAndAdd(villagerMap, level, new ArrayList<>());
        trades.add(trade);
    }

    public static void registerWanderingTraderTrade(int level, TradeOffers.Factory trade) {
        List<TradeOffers.Factory> trades = getOrDefaultAndAdd(WANDERING_TRADER_REGISTRY, level, new ArrayList<>());
        trades.add(trade);
    }

    public static <K, V> V getOrDefaultAndAdd(Map<K, V> map, K key, V defaultValue) {
        if (map.containsKey(key)) return map.get(key);
        map.put(key, defaultValue);
        return defaultValue;
    }

    public static void clearRegistries() {
        TRADES_REGISTRY.clear();
        WANDERING_TRADER_REGISTRY.clear();
    }

    public static Pair<HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>>, Int2ObjectOpenHashMap<TradeOffers.Factory[]>> getRegistryForLoading() {

        HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>> villagerRegistry = new HashMap<>();
        TRADES_REGISTRY.forEach((profession, listInt2ObjectOpenHashMap) -> {

            Int2ObjectOpenHashMap<TradeOffers.Factory[]> factories = new Int2ObjectOpenHashMap<>();

            listInt2ObjectOpenHashMap.forEach((integer, factoryList) -> {
                factories.put(integer.intValue(), factoryList.toArray(new TradeOffers.Factory[0]));
            });

            villagerRegistry.put(profession, factories);

        });

        Int2ObjectOpenHashMap<TradeOffers.Factory[]> wanderingTraderRegistry = new Int2ObjectOpenHashMap<>();

        WANDERING_TRADER_REGISTRY.forEach((integer, factories) -> wanderingTraderRegistry.put(integer.intValue(), factories.toArray(new TradeOffers.Factory[0])));

        return new Pair<>(villagerRegistry, wanderingTraderRegistry);
    }

}
