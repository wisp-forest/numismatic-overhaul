package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.item.CoinItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleItem.class)
public class BundleItemMixin {

    @Inject(method = "onClicked", at = @At("HEAD"), cancellable = true)
    private void noCoinsInBundle(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
        if (!(otherStack.getItem() instanceof CoinItem)) return;
        cir.setReturnValue(false);
    }

    @Inject(method = "onStackClicked", at = @At("HEAD"), cancellable = true)
    private void noCoinsInBundle(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (!(slot.getStack().getItem() instanceof CoinItem)) return;
        cir.setReturnValue(false);
    }

}
