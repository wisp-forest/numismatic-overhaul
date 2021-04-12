package com.glisco.numismaticoverhaul.villagers;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;

public class VillagerTradesHandler {

    public static final Gson GSON = new Gson();

    public static final HashMap<String, Integer> professionKeys = new HashMap<>();
    public static final HashMap<Identifier, TradeJsonAdapter> tradeTypesRegistry = new HashMap<>();

    private static final Map<String, List<String>> EXCEPTIONS_DURING_LOADING = new HashMap<>();
    private static String currentProfessionId = null;

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
        tradeTypesRegistry.put(new Identifier("numismatic-overhaul", "enchant_item"), new TradeJsonAdapters.EnchantItem());
        tradeTypesRegistry.put(new Identifier("numismatic-overhaul", "process_item"), new TradeJsonAdapters.ProcessItem());
        tradeTypesRegistry.put(new Identifier("numismatic-overhaul", "sell_dyed_armor"), new TradeJsonAdapters.SellDyedArmor());
        tradeTypesRegistry.put(new Identifier("numismatic-overhaul", "sell_potion_container"), new TradeJsonAdapters.SellPotionContainerItem());
        tradeTypesRegistry.put(new Identifier("numismatic-overhaul", "buy_item"), new TradeJsonAdapters.BuyItem());
    }

    public static void registerTrades() throws IOException {

        NumismaticVillagerTradesRegistry.clearRegistries();

        Iterator<Path> tradeFiles = Files.walk(FabricLoader.getInstance().getModContainer("numismatic-overhaul").get().getRootPath().resolve("data/numismatic-overhaul/villager_trades/")).iterator();

        while (tradeFiles.hasNext()) {
            File tradesFile = new File(tradeFiles.next().toString());
            if (!tradesFile.getPath().endsWith(".json")) continue;

            try {
                JsonObject jsonRoot = GSON.fromJson(new FileReader(tradesFile), JsonObject.class);

                Identifier professionId = Identifier.tryParse(jsonRoot.get("profession").getAsString());
                currentProfessionId = professionId.getPath();

                if (professionId.getPath().equals("wandering_trader")) {
                    deserializeTrades(jsonRoot, NumismaticVillagerTradesRegistry::registerWanderingTraderTrade);
                } else {
                    VillagerProfession profession = Registry.VILLAGER_PROFESSION.get(professionId);
                    deserializeTrades(jsonRoot, (integer, factory) -> NumismaticVillagerTradesRegistry.registerVillagerTrade(profession, integer, factory));
                }

            } catch (Exception e) {
                addLoadingException(e);
            }

        }

    }

    private static void deserializeTrades(JsonObject jsonRoot, BiConsumer<Integer, TradeOffers.Factory> tradeConsumer) {

        //Iterate villager levels
        for (Map.Entry<String, JsonElement> entry : jsonRoot.get("trades").getAsJsonObject().entrySet()) {

            int level = professionKeys.get(entry.getKey());
            JsonArray tradesArray = entry.getValue().getAsJsonArray();

            //Iterate trades in that level
            for (JsonElement tradeElement : tradesArray) {

                JsonObject trade = tradeElement.getAsJsonObject();

                if (!trade.has("type")) {
                    throw new JsonSyntaxException("Type missing");
                }

                TradeJsonAdapter adapter = tradeTypesRegistry.get(Identifier.tryParse(trade.get("type").getAsString()));

                if (adapter == null) {
                    throw new JsonSyntaxException("Unknown trade type " + trade.get("type").getAsString());
                }

                //Register trade
                tradeConsumer.accept(level, adapter.deserialize(trade));
            }
        }

    }

    public static void addLoadingException(Exception e) {
        if (currentProfessionId == null) {
            currentProfessionId = "JSON_PARSING";
        }

        if (EXCEPTIONS_DURING_LOADING.containsKey(currentProfessionId)) {
            EXCEPTIONS_DURING_LOADING.get(currentProfessionId).add(e.getMessage());
        } else {
            EXCEPTIONS_DURING_LOADING.put(currentProfessionId, new ArrayList<>(Collections.singletonList(e.getMessage())));
        }
        e.printStackTrace();
    }

    public static void broadcastErrors(MinecraftServer server) {
        broadcastErrors(server.getPlayerManager().getPlayerList());
    }

    public static void broadcastErrors(List<ServerPlayerEntity> players) {
        if (!EXCEPTIONS_DURING_LOADING.isEmpty()) {
            players.forEach(playerEntity -> {
                playerEntity.sendMessage(new LiteralText("§cThe following errors have occurred during numismatic-overhaul reload:"), false);
                playerEntity.sendMessage(new LiteralText(""), false);
                EXCEPTIONS_DURING_LOADING.forEach((profession, messages) -> {
                    playerEntity.sendMessage(new LiteralText("§7-> Profession: " + profession), false);
                    messages.forEach(s -> playerEntity.sendMessage(new LiteralText("§7    - " + s), false));
                });
            });
            EXCEPTIONS_DURING_LOADING.clear();
        }
    }
}
