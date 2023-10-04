package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.villagers.data.NumismaticVillagerTradesRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.impl.object.builder.TradeOfferInternals;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(TradeOfferInternals.class)
public class TradeOfferInternalsMixin {

    /**
     * Redirect Fabric API trade helper to integrate with NO
     *
     * @author glisco
     */
    @Overwrite(remap = false)
    public static synchronized void registerVillagerOffers(VillagerProfession profession, int level, TradeOfferHelper.VillagerOffersAdder factory) {
        final var factories = new ArrayList<TradeOffers.Factory>();
        factory.onRegister(factories, false);

        NumismaticVillagerTradesRegistry.registerFabricVillagerTrades(profession, level, factories);
    }

    /**
     * Redirect Fabric API trade helper to integrate with NO
     *
     * @author glisco
     */
    @Overwrite(remap = false)
    public static synchronized void registerWanderingTraderOffers(int level, Consumer<List<TradeOffers.Factory>> factory) {
        final var factories = new ArrayList<TradeOffers.Factory>();
        factory.accept(factories);

        NumismaticVillagerTradesRegistry.registerFabricWanderingTraderTrades(level, factories);
    }

    /**
     * Disable registering trades completely to make sure that no one ever interferes with the NO system
     *
     * @author glisco
     */
    @Overwrite(remap = false)
    private static void registerOffers(Int2ObjectMap<TradeOffers.Factory[]> leveledTradeMap, int level, Consumer<List<TradeOffers.Factory>> factory) {}
}
