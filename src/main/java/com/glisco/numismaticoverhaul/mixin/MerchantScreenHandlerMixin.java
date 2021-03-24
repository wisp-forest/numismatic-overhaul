package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.currency.CurrencyComponent;
import com.glisco.numismaticoverhaul.item.CoinItem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreenHandler.class)
public class MerchantScreenHandlerMixin {

    //Autofill with coins from the player's purse if the trade requires it
    //Injected at TAIL to let normal autofill run and fill up if anything is missing
    @Inject(method = "autofill", at = @At("TAIL"))
    public void autofillOverride(int slot, ItemStack stack, CallbackInfo ci) {
        MerchantScreenHandler handler = (MerchantScreenHandler) (Object) this;
        CurrencyComponent playerBalance = ModComponents.CURRENCY.get(((PlayerInventory) handler.getSlot(3).inventory).player);

        autofillWithCoins(slot, stack, handler, playerBalance);

        if (slot == 1) playerBalance.commitTransactions();

    }

    //TODO remove unnecessary commits
    private static void autofillWithCoins(int slot, ItemStack stack, MerchantScreenHandler handler, CurrencyComponent playerBalance) {
        //Bail if this autofill is not about coins, but commit any possible transactions if we're onto the second slot
        if (!(stack.getItem() instanceof CoinItem)) return;

        //See how much is required and how much was already autofilled
        int requiredCurrency = ((CoinItem) stack.getItem()).currency.getRawValue(stack.getCount());
        int presentCurrency = ((CoinItem) stack.getItem()).currency.getRawValue(handler.getSlot(slot).getStack().getCount());

        if (requiredCurrency <= presentCurrency) return;


        //Find out how we still need to fill
        int neededCurrency = requiredCurrency - presentCurrency;

        //Is that even possible?
        if (!(neededCurrency <= playerBalance.getValue())) if (slot == 1) playerBalance.commitTransactions();

        //Push a transaction and commit it if we're onto the second slot
        playerBalance.pushTransaction(-neededCurrency);
        if (slot == 1) playerBalance.commitTransactions();

        System.out.println(neededCurrency + " deduced from player balance");
        handler.slots.get(slot).setStack(stack.copy());
    }

}
