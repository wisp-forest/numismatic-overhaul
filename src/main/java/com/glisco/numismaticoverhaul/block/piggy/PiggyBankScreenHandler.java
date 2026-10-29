package com.glisco.numismaticoverhaul.block.piggy;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandlerContext;

@Deprecated
public class PiggyBankScreenHandler extends com.glisco.numismaticoverhaul.block.PiggyBankScreenHandler {

    public PiggyBankScreenHandler(int index, PlayerInventory playerInventory) {
        super(index, playerInventory);
    }

    public PiggyBankScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, Inventory piggyBankInventory) {
        super(syncId, playerInventory, context, piggyBankInventory);
    }
}
