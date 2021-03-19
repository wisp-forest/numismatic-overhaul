package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.currency.CurrencyComponent;
import com.glisco.numismaticoverhaul.item.CurrencyItem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreenHandler.class)
public class MerchantScreenHandlerMixin {

    @Inject(method = "autofill", at = @At("TAIL"), cancellable = true)
    public void autofillOverride(int slot, ItemStack stack, CallbackInfo ci) {
        MerchantScreenHandler handler = (MerchantScreenHandler) (Object) this;

        CurrencyComponent playerBalance = ModComponents.CURRENCY.get(((PlayerInventory) handler.getSlot(3).inventory).player);

        if (!(stack.getItem() instanceof CurrencyItem)) {
            if (slot == 1) playerBalance.commitTransactions();
            return;
        }

        int requiredCurrency = ((CurrencyItem) stack.getItem()).currency.getRawValue(stack.getCount());
        int presentCurrency = ((CurrencyItem) stack.getItem()).currency.getRawValue(handler.getSlot(slot).getStack().getCount());

        if (requiredCurrency <= presentCurrency) return;

        int neededCurrency = requiredCurrency - presentCurrency;

        if (!(neededCurrency <= playerBalance.getValue())) return;

        playerBalance.pushTransaction(-neededCurrency);
        if (slot == 1) playerBalance.commitTransactions();

        System.out.println(neededCurrency + " deduced from player balance");
        handler.slots.get(slot).setStack(stack.copy());

        ci.cancel();

    }

}
