package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.item.NumismaticOverhaulItems;
import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import io.wispforest.owo.client.screens.ValidatingSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

public class PiggyBankScreenHandler extends ScreenHandler {

    private final ScreenHandlerContext context;

    public PiggyBankScreenHandler(int index, PlayerInventory playerInventory) {
        this(index, playerInventory, ScreenHandlerContext.EMPTY, new SimpleInventory(3));
    }

    public PiggyBankScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, Inventory piggyBankInventory) {
        super(NumismaticOverhaul.PIGGY_BANK_SCREEN_HANDLER_TYPE, syncId);
        this.context = context;

        this.addSlot(new ValidatingSlot(piggyBankInventory, 0, 62, 26, stack -> stack.isOf(NumismaticOverhaulItems.BRONZE_COIN)));
        this.addSlot(new ValidatingSlot(piggyBankInventory, 1, 80, 26, stack -> stack.isOf(NumismaticOverhaulItems.SILVER_COIN)));
        this.addSlot(new ValidatingSlot(piggyBankInventory, 2, 98, 26, stack -> stack.isOf(NumismaticOverhaulItems.GOLD_COIN)));

        SlotGenerator.begin(this::addSlot, 8, 63).playerInventory(playerInventory);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return ScreenUtils.handleSlotTransfer(this, index, 3);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, NumismaticOverhaulBlocks.PIGGY_BANK);
    }
}
