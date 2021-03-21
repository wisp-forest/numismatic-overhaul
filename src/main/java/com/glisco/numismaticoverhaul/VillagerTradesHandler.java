package com.glisco.numismaticoverhaul;

import com.glisco.numismaticoverhaul.currency.CurrencyStack;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class VillagerTradesHandler {

    public static final Gson GSON = new Gson();

    public static final HashMap<String, Integer> professionKeys = new HashMap<>();

    static {
        professionKeys.put("novice", 1);
        professionKeys.put("apprentice", 2);
        professionKeys.put("journeyman", 3);
        professionKeys.put("expert", 4);
        professionKeys.put("master", 5);
    }

    public static void init() throws IOException {
        TradeOffers.PROFESSION_TO_LEVELED_TRADE.clear();

        Iterator<Path> tradeFiles = Files.walk(FabricLoader.getInstance().getModContainer("numismatic-overhaul").get().getRootPath().resolve("data/numismatic-overhaul/villager_trades/")).iterator();

        while (tradeFiles.hasNext()) {
            File tradesFile = new File(tradeFiles.next().toString());
            if (!tradesFile.getPath().endsWith(".json")) continue;

            JsonObject trades = GSON.fromJson(new BufferedReader(new FileReader(tradesFile)), JsonObject.class);

            VillagerProfession profession = Registry.VILLAGER_PROFESSION.get(Identifier.tryParse(trades.get("profession").getAsString()));
            Int2ObjectOpenHashMap<TradeOffers.Factory[]> tradesMap = new Int2ObjectOpenHashMap<>();

            innerLoop:
            for (Map.Entry<String, JsonElement> entry : trades.get("trades").getAsJsonObject().entrySet()) {

                TradeOffers.Factory[] factories = new TradeOffers.Factory[entry.getValue().getAsJsonArray().size()];
                int i = 0;

                for (JsonElement tradeElement : entry.getValue().getAsJsonArray()) {

                    JsonObject trade = tradeElement.getAsJsonObject();

                    if (new CurrencyStack(trade.get("price").getAsInt()).getRequiredCurrencyTypes() > 2) {
                        NumismaticOverhaul.LOGGER.error("Not adding trades for profession " + trades.get("profession").getAsString() + ", would require too many coins");
                        break innerLoop;
                    }

                    ItemStack sell = getItemStackFromJson(trade.get("sell").getAsJsonObject());

                    factories[i] = new SellForCurrencyTradeOffer(sell, trade.get("price").getAsInt(), trade.get("maxUses").getAsInt(), 1000);
                    i++;
                }
                tradesMap.put(professionKeys.get(entry.getKey()).intValue(), factories);
            }

            TradeOffers.PROFESSION_TO_LEVELED_TRADE.put(profession, tradesMap);
        }

    }

    public static ItemStack getItemStackFromJson(JsonObject json) {
        Item item = Registry.ITEM.getOrEmpty(Identifier.tryParse(json.get("item").getAsString())).orElseThrow(() -> new JsonSyntaxException("Invalid item:" + json.get("item").getAsString()));
        int count = json.has("count") ? json.get("count").getAsInt() : 1;

        return new ItemStack(item, count);
    }

    public static class SellForCurrencyTradeOffer implements TradeOffers.Factory {
        private final ItemStack sell;
        private final int maxUses;
        private final int experience;
        private final float multiplier;
        private final CurrencyStack price;

        public SellForCurrencyTradeOffer(ItemStack sell, int price, int maxUses, int experience) {
            this.sell = sell;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = 0.05F;
            this.price = new CurrencyStack(price);
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
