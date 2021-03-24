package com.glisco.numismaticoverhaul.villagers;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VillagerTradesHandler {

    public static final Gson GSON = new Gson();

    public static final HashMap<String, Integer> professionKeys = new HashMap<>();
    public static final HashMap<Identifier, TradeJsonAdapter> tradeTypesRegistry = new HashMap<>();

    static {
        professionKeys.put("novice", 1);
        professionKeys.put("apprentice", 2);
        professionKeys.put("journeyman", 3);
        professionKeys.put("expert", 4);
        professionKeys.put("master", 5);
    }

    public static void init() {
        tradeTypesRegistry.put(new Identifier("numismatic-overhaul", "sell_for_currency"), new TradeJsonAdapters.SellStackForCurrency());
        tradeTypesRegistry.put(new Identifier("numismatic-overhaul", "sell_map_for_currency"), new TradeJsonAdapters.SellMapForCurrency());
    }

    public static void loadTrades() throws IOException {
        TradeOffers.PROFESSION_TO_LEVELED_TRADE.clear();

        Iterator<Path> tradeFiles = Files.walk(FabricLoader.getInstance().getModContainer("numismatic-overhaul").get().getRootPath().resolve("data/numismatic-overhaul/villager_trades/")).iterator();

        while (tradeFiles.hasNext()) {
            File tradesFile = new File(tradeFiles.next().toString());
            if (!tradesFile.getPath().endsWith(".json")) continue;

            JsonObject trades = GSON.fromJson(new BufferedReader(new FileReader(tradesFile)), JsonObject.class);

            VillagerProfession profession = Registry.VILLAGER_PROFESSION.get(Identifier.tryParse(trades.get("profession").getAsString()));
            Int2ObjectOpenHashMap<TradeOffers.Factory[]> tradesMap = new Int2ObjectOpenHashMap<>();

            //TODO catch exceptions and display on world join
            innerLoop:
            for (Map.Entry<String, JsonElement> entry : trades.get("trades").getAsJsonObject().entrySet()) {

                TradeOffers.Factory[] factories = new TradeOffers.Factory[entry.getValue().getAsJsonArray().size()];
                int i = 0;

                for (JsonElement tradeElement : entry.getValue().getAsJsonArray()) {

                    JsonObject trade = tradeElement.getAsJsonObject();

                    if (!trade.has("type")) {
                        NumismaticOverhaul.LOGGER.error("Not adding trades for profession " + trades.get("profession").getAsString() + ", type missing");
                        break innerLoop;
                    }

                    TradeJsonAdapter adapter = tradeTypesRegistry.get(Identifier.tryParse(trade.get("type").getAsString()));

                    if (adapter == null) {
                        NumismaticOverhaul.LOGGER.error("Not adding trades for profession " + trades.get("profession").getAsString() + ", unknown trade type " + trades.get("type"));
                        break innerLoop;
                    }

                    factories[i] = adapter.deserialize(trade);
                    i++;
                }
                tradesMap.put(professionKeys.get(entry.getKey()).intValue(), factories);
            }

            TradeOffers.PROFESSION_TO_LEVELED_TRADE.put(profession, tradesMap);
        }

    }
}
