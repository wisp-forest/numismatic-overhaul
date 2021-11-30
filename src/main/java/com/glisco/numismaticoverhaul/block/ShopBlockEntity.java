package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.ImplementedInventory;
import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import io.wispforest.owo.ops.WorldOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
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
//TODO - Look over the new methods from this, and see if any need changing
public class ShopBlockEntity extends BlockEntity implements ImplementedInventory, SidedInventory, NamedScreenHandlerFactory {

    private final DefaultedList<ItemStack> INVENTORY = DefaultedList.ofSize(27, ItemStack.EMPTY);
    private final Merchant merchant;
    private final List<ShopOffer> offers;
    private int storedCurrency;
    private UUID owner;

    private int tradeIndex;

    public ShopBlockEntity(BlockPos pos, BlockState state) {
        super(NumismaticOverhaul.SHOP_BLOCK_ENTITY, pos, state);
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
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        Inventories.writeNbt(tag, INVENTORY);
        ShopOffer.toTag(tag, offers);
        tag.putInt("StoredCurrency", storedCurrency);
        if (owner != null) {
            tag.putUuid("Owner", owner);
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        Inventories.readNbt(tag, INVENTORY);
        ShopOffer.fromTag(tag, offers);
        if (tag.contains("Owner")) {
            owner = tag.getUuid("Owner");
        }
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

    public static void tick(World world, BlockPos pos, BlockState state, ShopBlockEntity blockEntity) {
        blockEntity.tick();
    }

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
