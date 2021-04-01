package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.ScreenHandler;

public class ShopScreenHandler extends ScreenHandler {

    private final Inventory shopInventory;

    public ShopScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(27));
    }

    public ShopScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(NumismaticOverhaul.SHOP_SCREEN_HANDLER_TYPE, syncId);
        this.shopInventory = inventory;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.shopInventory.canPlayerUse(player);
    }
}
