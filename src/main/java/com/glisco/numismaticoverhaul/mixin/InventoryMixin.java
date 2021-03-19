package com.glisco.numismaticoverhaul.mixin;

import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Inventory.class)
public interface InventoryMixin extends Inventory {
    @Override
    default int getMaxCountPerStack() {
        return 99;
    }
}
