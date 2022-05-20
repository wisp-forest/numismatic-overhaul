package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.villagers.data.NumismaticTradeOfferExtensions;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TradeOfferList.class)
public class TradeOfferListMixin {

    @Inject(method = "method_43715", at = @At(value = "INVOKE", ordinal = 4, target = "Lnet/minecraft/network/PacketByteBuf;writeInt(I)Lio/netty/buffer/ByteBuf;", shift = At.Shift.AFTER))
    private static void writeReputation(PacketByteBuf buf, TradeOffer offer, CallbackInfo ci) {
        buf.writeVarInt(((NumismaticTradeOfferExtensions) offer).numismatic$getReputation());
    }

    @Inject(method = "method_43716", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/TradeOffer;setSpecialPrice(I)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void readReputation(PacketByteBuf buf, CallbackInfoReturnable<TradeOffer> cir, ItemStack itemStack, ItemStack itemStack2, ItemStack itemStack3, boolean bl, int i, int j, int k, int l, float f, int m, TradeOffer tradeOffer) {
        ((NumismaticTradeOfferExtensions) tradeOffer).numismatic$setReputation(buf.readVarInt());
    }

}
