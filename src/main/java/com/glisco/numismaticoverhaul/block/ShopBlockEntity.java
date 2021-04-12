package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.ImplementedInventory;
import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.village.Merchant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShopBlockEntity extends BlockEntity implements ImplementedInventory, SidedInventory, NamedScreenHandlerFactory, BlockEntityClientSerializable, Tickable {

    private final DefaultedList<ItemStack> INVENTORY = DefaultedList.ofSize(27, ItemStack.EMPTY);
    private final Merchant merchant;
    private final List<ShopOffer> offers;
    private int storedCurrency;

    private int tradeIndex;

    public ShopBlockEntity() {
        super(NumismaticOverhaul.SHOP_BLOCK_ENTITY);
        this.merchant = new ShopMerchant(this);
        this.offers = new ArrayList<>();
        this.storedCurrency = 0;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return INVENTORY;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[0];
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("gui.numismatic-overhaul.shop.inventory_title");
    }

    @NotNull
    public Merchant getMerchant() {
        return merchant;
    }

    public List<ShopOffer> getOffers() {
        return offers;
    }

    public int getStoredCurrency() {
        return storedCurrency;
    }

    public void setStoredCurrency(int storedCurrency) {
        this.storedCurrency = storedCurrency;
        markDirty();
    }

    public void addCurrency(int value) {
        this.storedCurrency += value;
        markDirty();
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        Inventories.toTag(tag, INVENTORY);
        ShopOffer.toTag(tag, offers);
        tag.putInt("StoredCurrency", storedCurrency);
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        Inventories.fromTag(tag, INVENTORY);
        ShopOffer.fromTag(tag, offers);
        this.storedCurrency = tag.getInt("StoredCurrency");
    }

    public void addOrReplaceOffer(ShopOffer offer) {

        int indexToReplace = -1;

        for (int i = 0; i < offers.size(); i++) {
            if (!ItemStack.areEqual(offer.getSellStack(), offers.get(i).getSellStack())) continue;
            indexToReplace = i;
            break;
        }

        if (indexToReplace == -1) {
            if (offers.size() >= 24) {
                NumismaticOverhaul.LOGGER.error("Tried adding more than 24 trades to shop at {}", this.pos);
                return;
            }
            offers.add(offer);
        } else {
            offers.set(indexToReplace, offer);
        }

        this.markDirty();
    }

    public void deleteOffer(ItemStack stack) {
        if (!offers.removeIf(offer -> ItemStack.areEqual(stack, offer.getSellStack()))) {
            NumismaticOverhaul.LOGGER.error("Tried to delete invalid trade for {} from shop at {}", stack, this.pos);
            return;
        }

        this.markDirty();
    }

    @Override
    public void tick() {
        if (world.getTime() % 60 == 0) tradeIndex++;
    }

    @Environment(EnvType.CLIENT)
    public ItemStack getItemToRender() {
        if (tradeIndex > offers.size() - 1) tradeIndex = 0;
        return offers.get(tradeIndex).getSellStack();
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new ShopScreenHandler(syncId, inv, this);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        fromTag(getCachedState(), tag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        toTag(tag);
        tag.remove("Items");
        tag.remove("StoredCurrency");
        return tag;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (!world.isClient()) this.sync();
    }
}
