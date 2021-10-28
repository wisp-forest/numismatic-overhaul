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

    @Inject(method = "toPacket", at = @At(value = "INVOKE", ordinal = 4, target = "Lnet/minecraft/network/PacketByteBuf;writeInt(I)Lio/netty/buffer/ByteBuf;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void writeReputation(PacketByteBuf buf, CallbackInfo ci, int i, TradeOffer offer) {
        buf.writeVarInt(((NumismaticTradeOfferExtensions)offer).numismatic$getReputation());
    }

    @Inject(method = "fromPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/TradeOffer;setSpecialPrice(I)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void readReputation(PacketByteBuf buf, CallbackInfoReturnable<TradeOfferList> cir, TradeOfferList tradeOfferList, int i, int j, ItemStack itemStack, ItemStack itemStack2, ItemStack itemStack3, boolean bl, int k, int l, int m, int n, float f, int o, TradeOffer tradeOffer) {
        ((NumismaticTradeOfferExtensions)tradeOffer).numismatic$setReputation(buf.readVarInt());
    }

}
