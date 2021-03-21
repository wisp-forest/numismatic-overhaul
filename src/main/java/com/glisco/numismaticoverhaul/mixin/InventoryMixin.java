package com.glisco.numismaticoverhaul.mixin;

import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Inventory.class)
public interface InventoryMixin extends Inventory {

    //Very :concern: mixin that allows stacks to have sizes over 99, but unchecked and potentially dangerous?
    @Override
    default int getMaxCountPerStack() {
        return 99;
    }
}
