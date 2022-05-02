package com.glisco.numismaticoverhaul.villagers.data;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.villagers.json.VillagerTradesHandler;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

import java.util.HashMap;
import java.util.Map;

public class VillagerTradesResourceListener extends JsonDataLoader implements IdentifiableResourceReloadListener {

    public VillagerTradesResourceListener() {
        //Fortnite
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "villager_trades");
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(NumismaticOverhaul.MOD_ID, "villager_data_loader");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager, Profiler profiler) {
        if (!NumismaticOverhaul.getConfig().enableVillagerTrading) return;

        NumismaticVillagerTradesRegistry.clearRegistries();

        loader.forEach((identifier, jsonElement) -> {
            if (!jsonElement.isJsonObject()) return;
            JsonObject root = jsonElement.getAsJsonObject();
            VillagerTradesHandler.loadProfession(identifier, root);
        });

        NumismaticVillagerTradesRegistry.wrapModVillagers();

        final Pair<HashMap<VillagerProfession, Int2ObjectOpenHashMap<TradeOffers.Factory[]>>, Int2ObjectOpenHashMap<TradeOffers.Factory[]>> registry = NumismaticVillagerTradesRegistry.getRegistryForLoading();
        registry.getLeft().forEach(TradeOffers.PROFESSION_TO_LEVELED_TRADE::replace);

        if (!registry.getRight().isEmpty()) {
            TradeOffers.WANDERING_TRADER_TRADES.clear();
            TradeOffers.WANDERING_TRADER_TRADES.putAll(registry.getRight());
        }

    }
}
