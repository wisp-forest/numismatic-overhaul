package com.glisco.numismaticoverhaul.block;

import io.wispforest.owo.util.ImplementedInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class PiggyBankBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);

    public PiggyBankBlockEntity(BlockPos pos, BlockState state) {
        super(NumismaticOverhaulBlocks.Entities.PIGGY_BANK, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.inventory.clear();
        Inventories.readNbt(nbt, this.inventory);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
    }

    @Override
    public Text getDisplayName() {
        return this.getCachedState().getBlock().getName();
    }

    public DefaultedList<ItemStack> inventory() {
        return this.inventory;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new PiggyBankScreenHandler(
                syncId,
                player.getInventory(),
                ScreenHandlerContext.create(this.world, this.pos),
                (ImplementedInventory) () -> PiggyBankBlockEntity.this.inventory
        );
    }
}
