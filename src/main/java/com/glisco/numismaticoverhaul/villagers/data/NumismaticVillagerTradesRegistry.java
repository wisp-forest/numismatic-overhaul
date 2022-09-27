package com.glisco.numismaticoverhaul.villagers.data;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.Pair;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class NumismaticVillagerTradesRegistry {

    private static final HashMap<VillagerProfession, Int2ObjectOpenHashMap<List<TradeOffers.Factory>>> TRADES_REGISTRY = new HashMap<>();
    private static final Int2ObjectOpenHashMap<List<TradeOffers.Factory>> WANDERING_TRADER_REGISTRY = new Int2ObjectOpenHashMap<>();

    private static final HashMap<VillagerProfession, Int2ObjectOpenHashMap<List<TradeOffers.Factory>>> REMAPPED_FABRIC_TRADES = new HashMap<>();
    private static final Int2ObjectOpenHashMap<List<TradeOffers.Factory>> REMAPPED_FABRIC_WANDERING_TRADER_TRADES = new Int2ObjectOpenHashMap<>();

    private static final AtomicBoolean MOD_VILLAGERS_WRAPPED = new AtomicBoolean(false);

    // -- Fabric API trades - these are stored persistently --

    public static void registerFabricVillagerTrades(VillagerProfession profession, int level, List<TradeOffers.Factory> factories) {
        getVillagerTradeList(REMAPPED_FABRIC_TRADES, profession, level).addAll(factories.stream().map(RemappingTradeWrapper::wrap).toList());
    }

    public static void registerFabricWanderingTraderTrades(int level, List<TradeOffers.Factory> factories) {
        getOrDefaultAndAdd(REMAPPED_FABRIC_WANDERING_TRADER_TRADES, level, new ArrayList<>()).addAll(factories.stream().map(RemappingTradeWrapper::wrap).toList());
    }

    // -- NO datapack trades - this registry is cleared on reload--

    public static void registerVillagerTrade(VillagerProfession profession, int level, TradeOffers.Factory trade) {
        getVillagerTradeList(TRADES_REGISTRY, profession, level).add(trade);
    }

    public static void registerWanderingTraderTrade(int level, TradeOffers.Factory trade) {
        getOrDefaultAndAdd(WANDERING_TRADER_REGISTRY, level, new ArrayList<>()).add(trade);
    }

    // -- Helper Methods --

    public static void wrapModVillagers() {
        if (MOD_VILLAGERS_WRAPPED.get()) return;

        TradeOffers.PROFESSION_TO_LEVELED_TRADE.forEach((profession, int2TradesMap) -> {
            if (TRADES_REGISTRY.containsKey(profession)) return;
            int2TradesMap.forEach((integer, factories) -> {
                registerFabricVillagerTrades(profession, integer, Arrays.asList(factories));
            });
        });

        MOD_VILLAGERS_WRAPPED.set(true);
    }

    private static List<TradeOffers.Factory> getVillagerTradeList(HashMap<VillagerProfession, Int2ObjectOpenHashMap<List<TradeOffers.Factory>>> registry, VillagerProfession profession, int level) {
        Int2ObjectOpenHashMap<List<TradeOffers.Factory>> villagerMap = getOrDefaultAndAdd(registry, profession, new Int2ObjectOpenHashMap<>());
        return getOrDefaultAndAdd(villagerMap, level, new ArrayList<>());
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

        final var processor = RegistryProcessor.begin();

        TRADES_REGISTRY.forEach(processor::processProfession);
        REMAPPED_FABRIC_TRADES.forEach((villagerProfession, listInt2ObjectOpenHashMap) -> {
            if (TRADES_REGISTRY.containsKey(villagerProfession)) return;
            processor.processProfession(villagerProfession, listInt2ObjectOpenHashMap);
        });

        WANDERING_TRADER_REGISTRY.forEach(processor::processWanderingTrader);
        REMAPPED_FABRIC_WANDERING_TRADER_TRADES.forEach(processor::processWanderingTrader);

        return processor.finish();
    }

    private static class RegistryProcessor {

        private final HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>> villagerTrades;
        private final Int2ObjectOpenHashMap<TradeOffers.Factory[]> wanderingTraderTrades;

        private RegistryProcessor() {
            this.villagerTrades = new HashMap<>();
            this.wanderingTraderTrades = new Int2ObjectOpenHashMap<>();
        }

        public static RegistryProcessor begin() {
            return new RegistryProcessor();
        }

        public void processProfession(VillagerProfession profession, Int2ObjectOpenHashMap<List<TradeOffers.Factory>> professionTradesPerLevel) {
            Int2ObjectOpenHashMap<TradeOffers.Factory[]> factories = villagerTrades.getOrDefault(profession, new Int2ObjectOpenHashMap<>());

            professionTradesPerLevel.forEach((level, factoryList) -> {
                final var oldFactories = factories.getOrDefault(level.intValue(), new TradeOffers.Factory[0]);
                factories.put(level.intValue(), ArrayUtils.addAll(oldFactories, factoryList.toArray(new TradeOffers.Factory[0])));
            });

            villagerTrades.put(profession, factories);
        }

        public void processWanderingTrader(Integer level, List<TradeOffers.Factory> trades) {
            final var oldFactories = wanderingTraderTrades.getOrDefault(level.intValue(), new TradeOffers.Factory[0]);
            wanderingTraderTrades.put(level.intValue(), ArrayUtils.addAll(oldFactories, trades.toArray(new TradeOffers.Factory[0])));
        }

        public Pair<HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>>, Int2ObjectOpenHashMap<TradeOffers.Factory[]>> finish() {
            return new Pair<>(villagerTrades, wanderingTraderTrades);
        }

    }

}
