package com.glisco.numismaticoverhaul.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.impl.object.builder.TradeOfferInternals;
import net.minecraft.village.TradeOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

@Mixin(TradeOfferInternals.class)
public class TradeOfferInternalsMixin {

    //TODO this really should convert to coins and integrate
    /**
     * Disable Fabric API trades module for now to prevent it interfering with the NO datapack system
     *
     * @author glisco
     */
    @Overwrite(remap = false)
    private static void registerOffers(Int2ObjectMap<TradeOffers.Factory[]> leveledTradeMap, int level, Consumer<List<TradeOffers.Factory>> factory){
    }
}
