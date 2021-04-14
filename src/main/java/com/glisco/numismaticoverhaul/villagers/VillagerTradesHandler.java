package com.glisco.numismaticoverhaul.villagers;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.FileNameUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;

public class VillagerTradesHandler {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final HashMap<String, Integer> professionKeys = new HashMap<>();
    public static final HashMap<Identifier, TradeJsonAdapter> tradeTypesRegistry = new HashMap<>();

    private static final List<DeserializationException> EXCEPTIONS_DURING_LOADING = new ArrayList<>();

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

            //Clear context
            DeserializationContext.clear();

            File tradesFile = new File(tradeFiles.next().toString());
            if (!tradesFile.getPath().endsWith(".json")) continue;

            String fileName = "§a" + tradesFile.getParentFile().getParentFile().getName() + "§f:§6" + tradesFile.getName();

            //Push path to context
            DeserializationContext.setFile(fileName);

            JsonObject jsonRoot;
            Identifier professionId;

            try {
                jsonRoot = GSON.fromJson(new FileReader(tradesFile), JsonObject.class);
                professionId = Identifier.tryParse(jsonRoot.get("profession").getAsString());
            } catch (Exception e) {
                addLoadingException(new DeserializationException(e.getMessage()));
                continue;
            }

            if (professionId == null) {
                addLoadingException(new DeserializationException("Could not parse profession id \"" + jsonRoot.get("profession").getAsString() + "\""));
                continue;
            }

            //Push profession to context
            DeserializationContext.setProfession(professionId.getPath());

            try {
                if (professionId.getPath().equals("wandering_trader")) {
                    deserializeTrades(jsonRoot, NumismaticVillagerTradesRegistry::registerWanderingTraderTrade);
                } else {
                    VillagerProfession profession = Registry.VILLAGER_PROFESSION.get(professionId);
                    deserializeTrades(jsonRoot, (integer, factory) -> NumismaticVillagerTradesRegistry.registerVillagerTrade(profession, integer, factory));
                }
            } catch (DeserializationException e) {
                addLoadingException(e);
            }

        }

    }

    private static void deserializeTrades(@NotNull JsonObject jsonRoot, BiConsumer<Integer, TradeOffers.Factory> tradeConsumer) {

        if (!jsonRoot.get("trades").isJsonObject()) throw new DeserializationException(jsonRoot.get("trades") + " is not a JsonObject");

        //Iterate villager levels
        for (Map.Entry<String, JsonElement> entry : jsonRoot.get("trades").getAsJsonObject().entrySet()) {

            int level = professionKeys.get(entry.getKey());

            if (!entry.getValue().isJsonArray()) throw new DeserializationException(entry.getValue() + " is not a JsonArray");
            JsonArray tradesArray = entry.getValue().getAsJsonArray();

            //Iterate trades in that level
            for (JsonElement tradeElement : tradesArray) {

                if (!tradeElement.isJsonObject()) {
                    addLoadingException(new DeserializationException(tradeElement + " is not a JsonObject"));
                    continue;
                }

                JsonObject trade = tradeElement.getAsJsonObject();

                //Push trade to context
                DeserializationContext.setTrade(trade);
                DeserializationContext.setLevel(level);

                if (!trade.has("type")) {
                    throw new DeserializationException("Type missing");
                }

                TradeJsonAdapter adapter = tradeTypesRegistry.get(Identifier.tryParse(trade.get("type").getAsString()));

                if (adapter == null) {
                    throw new DeserializationException("Unknown trade type " + trade.get("type").getAsString());
                }

                //Register trade
                try {
                    tradeConsumer.accept(level, adapter.deserialize(trade));
                } catch (DeserializationException e) {
                    addLoadingException(e);
                }
            }
        }

    }

    public static void addLoadingException(DeserializationException e) {
        EXCEPTIONS_DURING_LOADING.add(e);

        NumismaticOverhaul.LOGGER.error("");
        NumismaticOverhaul.LOGGER.error(" -- Caught exception during loading trade definitions, full stacktrace commencing -- ");
        NumismaticOverhaul.LOGGER.error("");

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
                EXCEPTIONS_DURING_LOADING.forEach(e -> {

                    MutableText message = new LiteralText("§7-> " + e.getMessage() + " §8(hover for more info)");

                    MutableText hoverText = new LiteralText("");
                    hoverText.append(new LiteralText("File: §7" + e.getContext().file + "\n\n"));
                    hoverText.append(new LiteralText("Profession: §a" + e.getContext().profession + "\n"));
                    hoverText.append(new LiteralText("Level: §6" + e.getContext().level + "\n\n"));

                    hoverText.append(new LiteralText("Problematic trade: \n§7" + GSON.toJson(e.getContext().trade)));

                    message.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));

                    playerEntity.sendMessage(message, false);

                });
            });
            EXCEPTIONS_DURING_LOADING.clear();
        }
    }
}
