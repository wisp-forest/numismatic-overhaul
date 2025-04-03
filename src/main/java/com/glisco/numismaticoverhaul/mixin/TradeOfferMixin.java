package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.item.CurrencyItem;
import com.glisco.numismaticoverhaul.villagers.data.NumismaticTradeOfferExtensions;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;

@Mixin(TradeOffer.class)
public class TradeOfferMixin implements NumismaticTradeOfferExtensions {

    @Shadow
    @Final
    private TradedItem firstBuyItem;
    private int numismatic$reputation = 0;

    @Override
    public void numismatic$setReputation(int reputation) {
        this.numismatic$reputation = reputation;
    }

    @Override
    public int numismatic$getReputation() {
        return numismatic$reputation;
    }

    @Inject(method = "getFirstBuyItem", at = @At("HEAD"), cancellable = true)
    private void adjustFirstStack(CallbackInfoReturnable<ItemStack> cir) {
        if (this.numismatic$reputation == -69420) return;

        if (!(this.firstBuyItem.item() instanceof CurrencyItem currencyItem)) return;

        var originalValue = currencyItem.getValue(this.firstBuyItem.itemStack());
        var adjustedValue = numismatic$reputation < 0
                ? (long) (originalValue + Math.abs(numismatic$reputation) * (Math.abs(originalValue) * .02))
                : (long) Math.max(1, originalValue - Math.abs(originalValue) * (numismatic$reputation / (numismatic$reputation + 100f)));

        adjustedValue = Math.min(adjustedValue, 990000);

        final var roundedStack = CurrencyHelper.getClosest(adjustedValue);
        if (originalValue != CurrencyHelper.getValue(Collections.singletonList(roundedStack)) && !roundedStack.isOf(this.firstBuyItem.itemStack().getItem())) {
            CurrencyItem.setOriginalValue(roundedStack, originalValue);
        }
        cir.setReturnValue(roundedStack);
    }

    @Inject(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/RegistryByteBuf;writeInt(I)Lnet/minecraft/network/PacketByteBuf;", ordinal = 4, shift = At.Shift.AFTER))
    private static void writeReputation(RegistryByteBuf buf, TradeOffer offer, CallbackInfo ci) {
        buf.writeVarInt(((NumismaticTradeOfferExtensions) offer).numismatic$getReputation());
    }

    @Inject(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/TradeOffer;setSpecialPrice(I)V"))
    private static void readReputation(RegistryByteBuf buf, CallbackInfoReturnable<TradeOffer> cir, @Local TradeOffer tradeOffer) {
        ((NumismaticTradeOfferExtensions) tradeOffer).numismatic$setReputation(buf.readVarInt());
    }
}
