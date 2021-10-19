package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.block.ShopOffer;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.item.CoinItem;
import com.glisco.numismaticoverhaul.item.CurrencyItem;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.glisco.numismaticoverhaul.villagers.data.NumismaticTradeOfferExtensions;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collections;

@Mixin(TradeOffer.class)
public class TradeOfferMixin implements NumismaticTradeOfferExtensions {

    @Shadow
    @Final
    private ItemStack firstBuyItem;
    private int numismatic$reputation = 1;

    @Override
    public void numismatic$setReputation(int reputation) {
        this.numismatic$reputation = reputation;
    }

    @Override
    public int numismatic$getReputation() {
        return numismatic$reputation;
    }

    @Inject(method = "toNbt", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void saveReputation(CallbackInfoReturnable<NbtCompound> cir, NbtCompound nbt) {
        nbt.putInt("Reputation", numismatic$reputation);
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("RETURN"))
    private void loadReputation(NbtCompound nbt, CallbackInfo ci) {
        this.numismatic$reputation = nbt.getInt("Reputation");
    }

    @Inject(method = "getAdjustedFirstBuyItem", at = @At("HEAD"), cancellable = true)
    private void adjustFirstStack(CallbackInfoReturnable<ItemStack> cir) {
        if (this.numismatic$reputation == -69420) return;

        if (!(this.firstBuyItem.getItem() instanceof CoinItem || this.firstBuyItem.getItem() instanceof MoneyBagItem)) return;

        int value = CurrencyHelper.getValue(Collections.singletonList(this.firstBuyItem));
        int adjustedValue = numismatic$reputation < 0 ?
                (int) (value + Math.abs(numismatic$reputation) * (Math.abs(value) * .02))
                :
                (int) Math.max(1, value - Math.abs(value) * (numismatic$reputation / (numismatic$reputation + 100f)));

        final var stack = CurrencyHelper.getClosest(adjustedValue);
        if (value != CurrencyHelper.getValue(Collections.singletonList(stack)) && !stack.isOf(this.firstBuyItem.getItem()))
            CurrencyItem.setOriginalValue(stack, value);
        cir.setReturnValue(stack);
    }

}
