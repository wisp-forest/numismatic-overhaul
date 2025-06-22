package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.villagers.data.NumismaticVillagerTradesRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.impl.object.builder.TradeOfferInternals;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(value = TradeOfferInternals.class, priority = 500)
public class TradeOfferInternalsMixin {

    /**
     * Redirect Fabric API trade helper to integrate with NO
     *
     * @author glisco
     */
    @Inject(method = "registerVillagerOffers", at = @At("HEAD"), cancellable = true, remap = false)
    private static synchronized void registerVillagerOffers(VillagerProfession profession, int level, Consumer<List<TradeOffers.Factory>> factory, CallbackInfo ci) {
        if (!NumismaticOverhaul.CONFIG.enableVillagerTrading()) return;
        final var factories = new ArrayList<TradeOffers.Factory>();
        factory.accept(factories);

        NumismaticVillagerTradesRegistry.registerFabricVillagerTrades(profession, level, factories);
        ci.cancel();
    }

    /**
     * Redirect Fabric API trade helper to integrate with NO
     *
     * @author glisco
     */
    @Inject(method = "registerWanderingTraderOffers", at = @At("HEAD"), cancellable = true, remap = false)
    private static synchronized void registerWanderingTraderOffers(int level, Consumer<List<TradeOffers.Factory>> factory, CallbackInfo ci) {
        if (!NumismaticOverhaul.CONFIG.enableVillagerTrading()) return;
        final var factories = new ArrayList<TradeOffers.Factory>();
        factory.accept(factories);

        NumismaticVillagerTradesRegistry.registerFabricWanderingTraderTrades(level, factories);
        ci.cancel();
    }

    /**
     * Disable registering trades completely to make sure that no one ever interferes with the NO system
     *
     * @author glisco
     */
    @Inject(method = "registerOffers", at = @At("HEAD"), cancellable = true, remap = false)
    private static void registerOffers(Int2ObjectMap<TradeOffers.Factory[]> leveledTradeMap, int level, Consumer<List<TradeOffers.Factory>> factory, CallbackInfo ci) {
        if (NumismaticOverhaul.CONFIG.enableVillagerTrading()) {
            ci.cancel();
        }
    }
}
