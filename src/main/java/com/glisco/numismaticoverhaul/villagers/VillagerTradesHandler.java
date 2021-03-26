package com.glisco.numismaticoverhaul.villagers;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.MessageType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class VillagerTradesHandler implements SimpleResourceReloadListener<HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>>> {

    public static final Gson GSON = new Gson();

    public static final HashMap<String, Integer> professionKeys = new HashMap<>();
    public static final HashMap<Identifier, TradeJsonAdapter> tradeTypesRegistry = new HashMap<>();

    private static final List<Exception> EXCEPTIONS_DURING_LOADING = new ArrayList<>();

    static {
        professionKeys.put("novice", 1);
        professionKeys.put("apprentice", 2);
        professionKeys.put("journeyman", 3);
        professionKeys.put("expert", 4);
        professionKeys.put("master", 5);
    }

    public static void init() {
        tradeTypesRegistry.put(new Identifier("numismatic-overhaul", "sell_stack"), new TradeJsonAdapters.SellStack());
        tradeTypesRegistry.put(new Identifier("numismatic-overhaul", "sell_map"), new TradeJsonAdapters.SellMap());
        tradeTypesRegistry.put(new Identifier("numismatic-overhaul", "sell_single_enchantment"), new TradeJsonAdapters.SellSingleEnchantment());
        tradeTypesRegistry.put(new Identifier("numismatic-overhaul", "enchant_book"), new TradeJsonAdapters.EnchantBook());
        tradeTypesRegistry.put(new Identifier("numismatic-overhaul", "process_item"), new TradeJsonAdapters.ProcessItem());
    }

    private static HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>> reloadTrades() throws IOException {

        System.out.println("--- RELOAD TRADES ---");

        HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>> tradesCache = new HashMap<>();

        Iterator<Path> tradeFiles = Files.walk(FabricLoader.getInstance().getModContainer("numismatic-overhaul").get().getRootPath().resolve("data/numismatic-overhaul/villager_trades/")).iterator();

        while (tradeFiles.hasNext()) {
            File tradesFile = new File(tradeFiles.next().toString());
            if (!tradesFile.getPath().endsWith(".json")) continue;

            JsonObject jsonRoot = GSON.fromJson(new BufferedReader(new FileReader(tradesFile)), JsonObject.class);

            VillagerProfession profession = Registry.VILLAGER_PROFESSION.get(Identifier.tryParse(jsonRoot.get("profession").getAsString()));
            Int2ObjectOpenHashMap<TradeOffers.Factory[]> tradesMap = new Int2ObjectOpenHashMap<>();

            try {
                for (Map.Entry<String, JsonElement> entry : jsonRoot.get("trades").getAsJsonObject().entrySet()) {

                    TradeOffers.Factory[] factories = new TradeOffers.Factory[entry.getValue().getAsJsonArray().size()];
                    int i = 0;

                    for (JsonElement tradeElement : entry.getValue().getAsJsonArray()) {

                        JsonObject trade = tradeElement.getAsJsonObject();

                        if (!trade.has("type")) {
                            throw new JsonSyntaxException("Not adding trades for profession " + jsonRoot.get("profession").getAsString() + ", type missing");
                        }

                        TradeJsonAdapter adapter = tradeTypesRegistry.get(Identifier.tryParse(trade.get("type").getAsString()));

                        if (adapter == null) {
                            throw new JsonSyntaxException("Not adding trades for profession " + jsonRoot.get("profession").getAsString() + ", unknown trade type " + trade.get("type").getAsString());
                        }

                        factories[i] = adapter.deserialize(trade);
                        i++;
                    }
                    tradesMap.put(professionKeys.get(entry.getKey()).intValue(), factories);
                }
            } catch (Exception e) {
                EXCEPTIONS_DURING_LOADING.add(e);
            }

            tradesCache.put(profession, tradesMap);

        }

        return tradesCache;
    }

    public static void broadcastErrors(MinecraftServer server) {
        if (!EXCEPTIONS_DURING_LOADING.isEmpty()) {
            server.getPlayerManager().broadcastChatMessage(new LiteralText("§cThe following errors have occured during numismatic-overhaul reload:"), MessageType.SYSTEM, null);
            EXCEPTIONS_DURING_LOADING.forEach(e -> {
                server.getPlayerManager().broadcastChatMessage(new LiteralText("§7- " + e.getMessage()), MessageType.SYSTEM, null);
            });
        }
        EXCEPTIONS_DURING_LOADING.clear();
    }

    @Override
    public CompletableFuture<HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>>> load(ResourceManager manager, Profiler profiler, Executor executor) {

        CompletableFuture<HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>>> future = new CompletableFuture<>();

        try {
            future.complete(reloadTrades());
        } catch (IOException e) {
            e.printStackTrace();
            future.complete(new HashMap<>());
        }

        return future;
    }

    @Override
    public CompletableFuture<Void> apply(HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>> data, ResourceManager manager, Profiler profiler, Executor executor) {

        TradeOffers.PROFESSION_TO_LEVELED_TRADE.clear();
        data.forEach(TradeOffers.PROFESSION_TO_LEVELED_TRADE::put);

        System.out.println("--- APPLY TRADES ---");

        EXCEPTIONS_DURING_LOADING.forEach(e -> {

        });

        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        return future;
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(NumismaticOverhaul.MOD_ID, "villager_data_loader");
    }
}
