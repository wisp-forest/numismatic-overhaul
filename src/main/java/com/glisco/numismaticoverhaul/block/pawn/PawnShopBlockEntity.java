package com.glisco.numismaticoverhaul.block.pawn;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.NumismaticOverhaulBlocks;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.util.ImplementedInventory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.village.Merchant;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class PawnShopBlockEntity extends LockableContainerBlockEntity implements ImplementedInventory, SidedInventory, NamedScreenHandlerFactory {

    private static final int[] SLOTS = IntStream.range(0, 27).toArray();
    private static final int[] NO_SLOTS = new int[0];

    private final DefaultedList<ItemStack> INVENTORY = DefaultedList.ofSize(27, ItemStack.EMPTY);

    public boolean busy = false;
    private final Merchant merchant;
    private final List<PawnShopOffer> offers;

    private long storedCurrency;
    private UUID owner;
    private boolean allowsTransfer = false;

    private int tradeIndex;

    public PawnShopBlockEntity(BlockPos pos, BlockState state) {
        super(NumismaticOverhaulBlocks.Entities.PAWN_SHOP, pos, state);

        boolean inexhaustible = (state.getBlock() instanceof PawnShopBlock shop) && shop.inexhaustible();
        this.merchant = new PawnShopMerchant(this, inexhaustible);

        this.offers = new ArrayList<>();
        this.storedCurrency = 0;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return INVENTORY;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return allowsTransfer ? SLOTS : NO_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return allowsTransfer;
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("gui.numismatic-overhaul.pawn_shop.inventory_title");
    }

    @NotNull
    public Merchant getMerchant() {
        return merchant;
    }

    public List<PawnShopOffer> getOffers() {
        return offers;
    }

    public long getStoredCurrency() {
        return storedCurrency;
    }

    public boolean isTransferEnabled() {
        return allowsTransfer;
    }

    public void toggleTransfer() {
        this.allowsTransfer = !this.allowsTransfer;
    }

    public void setStoredCurrency(long storedCurrency) {
        this.storedCurrency = storedCurrency;
        markDirty();
    }

    public void addCurrency(long value) {
        this.storedCurrency += value;
        markDirty();
    }

    public void removeCurrency(int value) {
        this.storedCurrency -= value;
        markDirty();
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        Inventories.writeNbt(tag, INVENTORY);
        PawnShopOffer.writeAll(tag, offers);
        tag.putBoolean("AllowsTransfer", this.allowsTransfer);
        tag.putLong("StoredCurrency", storedCurrency);
        if (owner != null) {
            tag.putUuid("Owner", owner);
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        Inventories.readNbt(tag, INVENTORY);
        PawnShopOffer.readAll(tag, offers);
        if (tag.contains("Owner")) {
            owner = tag.getUuid("Owner");
        }
        this.allowsTransfer = tag.getBoolean("AllowsTransfer");
        this.storedCurrency = tag.getLong("StoredCurrency");
    }

    public void addOrReplaceOffer(PawnShopOffer offer) {
        int indexToReplace = -1;

        for (int i = 0; i < offers.size(); i++) {
            if (!ItemStack.areEqual(offer.getBuyStack(), offers.get(i).getBuyStack())) continue;
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
        if (!offers.removeIf(offer -> ItemStack.areEqual(stack, offer.getBuyStack()))) {
            NumismaticOverhaul.LOGGER.error("Tried to delete invalid trade for {} from shop at {}", stack, this.pos);
            return;
        }

        this.markDirty();
    }

    public static void tick(World world, BlockPos pos, BlockState state, PawnShopBlockEntity blockEntity) {
        blockEntity.tick();
    }

    public void tick() {
        if (world.getTime() % 60 == 0) tradeIndex++;
    }

    @Environment(EnvType.CLIENT)
    public ItemStack getItemToRender() {
        if (tradeIndex > offers.size() - 1) tradeIndex = 0;
        return offers.get(tradeIndex).getBuyStack();
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new PawnShopScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return player.getUuid().equals(this.owner) && this.world.getBlockEntity(this.pos) == this && this.pos.getSquaredDistance(player.getX(), player.getY(), player.getZ()) <= 100;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = new NbtCompound();
        this.writeNbt(tag);
        tag.remove("Items");
        tag.remove("StoredCurrency");
        return tag;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        markDirty();
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        WorldOps.updateIfOnServer(world, pos);
    }
}
