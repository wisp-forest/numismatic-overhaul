package com.glisco.numismaticoverhaul.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {

    //@Inject(method = "getMaxItemCount(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    public void onMaxSize(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getMaxCount() > 64) cir.setReturnValue(stack.getMaxCount());
    }

}
