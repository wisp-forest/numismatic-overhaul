package com.glisco.numismaticoverhaul.block.pawn;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.network.PawnShopScreenHandlerRequestC2SPacket;
import com.glisco.numismaticoverhaul.network.UpdatePawnShopScreenS2CPacket;
import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import io.wispforest.owo.client.screens.SyncedProperty;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class PawnShopScreenHandler extends ScreenHandler {

    private final PlayerEntity owner;

    private final Inventory shopInventory;
    private final SyncedProperty<ItemStack> tradeEditBuffer;

    private final List<PawnShopOffer> offers;

    private PawnShopBlockEntity shop = null;
    private int currentTab = 0;

    public PawnShopScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(27));
    }

    public PawnShopScreenHandler(int syncId, PlayerInventory playerInventory, Inventory shopInventory) {
        super(NumismaticOverhaul.PAWN_SHOP_SCREEN_HANDLER_TYPE, syncId);
        this.shopInventory = shopInventory;
        this.owner = playerInventory.player;

        if (!this.owner.getWorld().isClient()) {
            this.shop = (PawnShopBlockEntity) shopInventory;
            this.offers = shop.getOffers();
        } else {
            this.offers = new ArrayList<>();
        }

        SlotGenerator.begin(this::addSlot, 8, 17)
                .slotFactory((inv, index, x, y) -> new AutoHidingSlot(inv, index, x, y, 0, false, () -> this.currentTab))
                .grid(this.shopInventory, 0, 9, 3)
                .slotFactory(Slot::new)
                .moveTo(8, 85)
                .playerInventory(playerInventory);

        this.tradeEditBuffer = this.createProperty(ItemStack.class, ItemStack.EMPTY);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (this.shop != null) {
            if (this.shop.getWorld() == null || this.shop.getWorld().getBlockEntity(this.shop.getPos()) != this.shop) {
                return false;
            }
            var pos = this.shop.getPos();
            if (player.squaredDistanceTo((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5) > 64.0) {
                return false;
            }
            return (this.shop.getOwner() != null && player.getUuid().equals(this.shop.getOwner())) || player.isCreative() || player.hasPermissionLevel(2);
        }
        return this.shopInventory.canPlayerUse(player);
    }

    public boolean isOwner(PlayerEntity player) {
        if (this.shop == null) return false;
        var shopOwner = this.shop.getOwner();
        return (shopOwner != null && player.getUuid().equals(shopOwner)) || player.isCreative() || player.hasPermissionLevel(2);
    }

    public PawnShopBlockEntity getShop() {
        return this.shop;
    }

    public void loadOffer(long index) {
        if (!this.owner.getWorld().isClient) {
            if (!this.isOwner(this.owner)) return;
            if (index < 0 || index >= this.offers.size()) {
                NumismaticOverhaul.LOGGER.error("Player {} attempted to load invalid trade at index {}", owner.getName(), index);
                return;
            }

            this.tradeEditBuffer.set(this.offers.get((int) index).getBuyStack());
        } else {
            NumismaticOverhaul.CHANNEL.clientHandle().send(new PawnShopScreenHandlerRequestC2SPacket(this.syncId, PawnShopScreenHandlerRequestC2SPacket.Action.LOAD_OFFER, index));
        }
    }

    public void createOffer(long price) {
        if (price <= 0) return;

        if (!this.owner.getWorld().isClient) {
            if (!this.isOwner(this.owner)) return;
            final var stack = this.tradeEditBuffer.get();
            if (stack.isEmpty()) return;

            this.shop.addOrReplaceOffer(new PawnShopOffer(stack, price));
            this.updateClient();
        } else {
            NumismaticOverhaul.CHANNEL.clientHandle().send(new PawnShopScreenHandlerRequestC2SPacket(this.syncId, PawnShopScreenHandlerRequestC2SPacket.Action.CREATE_OFFER, price));
        }
    }

    public void insertCurrency() {
        if (!this.owner.getWorld().isClient) {
            if (!this.isOwner(this.owner)) return;
            var money = CurrencyHelper.getMoneyInInventory(this.owner, true);
            this.shop.addCurrency(money);
            this.updateClient();
        } else {
            NumismaticOverhaul.CHANNEL.clientHandle().send(new PawnShopScreenHandlerRequestC2SPacket(this.syncId, PawnShopScreenHandlerRequestC2SPacket.Action.INSERT_CURRENCY));
        }
    }

    public void extractCurrency() {
        if (!this.owner.getWorld().isClient) {
            if (!this.isOwner(this.owner)) return;
            ModComponents.CURRENCY.get(owner).modify(shop.getStoredCurrency());
            this.shop.setStoredCurrency(0);
            this.updateClient();
        } else {
            NumismaticOverhaul.CHANNEL.clientHandle().send(new PawnShopScreenHandlerRequestC2SPacket(this.syncId, PawnShopScreenHandlerRequestC2SPacket.Action.EXTRACT_CURRENCY));
        }
    }

    public void deleteOffer() {
        if (!this.owner.getWorld().isClient) {
            if (!this.isOwner(this.owner)) return;
            this.shop.deleteOffer(this.tradeEditBuffer.get());
            this.updateClient();
        } else {
            NumismaticOverhaul.CHANNEL.clientHandle().send(new PawnShopScreenHandlerRequestC2SPacket(this.syncId, PawnShopScreenHandlerRequestC2SPacket.Action.DELETE_OFFER));
        }
    }

    public void toggleTransfer() {
        if (!this.owner.getWorld().isClient) {
            if (!this.isOwner(this.owner)) return;
            this.shop.toggleTransfer();
            this.updateClient();
        } else {
            NumismaticOverhaul.CHANNEL.clientHandle().send(new PawnShopScreenHandlerRequestC2SPacket(this.syncId, PawnShopScreenHandlerRequestC2SPacket.Action.TOGGLE_TRANSFER));
        }
    }

    public void handleBufferClick() {
        if (!this.owner.getWorld().isClient) {
            if (!this.isOwner(this.owner)) return;
            this.tradeEditBuffer.set(this.getCursorStack().copy());
        } else {
            NumismaticOverhaul.CHANNEL.clientHandle().send(new PawnShopScreenHandlerRequestC2SPacket(this.syncId, PawnShopScreenHandlerRequestC2SPacket.Action.CLICK_BUFFER));
        }
    }

    private void updateClient() {
        NumismaticOverhaul.CHANNEL.serverHandle(owner).send(new UpdatePawnShopScreenS2CPacket(shop));
    }

    public void setCurrentTab(int currentTab) {
        this.currentTab = currentTab;
    }

    public int getCurrentTab() {
        return this.currentTab;
    }

    public SyncedProperty<ItemStack> tradeEditBuffer() {
        return this.tradeEditBuffer;
    }

    public void addTradeEditBufferObserver(Consumer<ItemStack> observer) {
        this.tradeEditBuffer.observe(observer);
    }

    public SyncedProperty<ItemStack> getTradeEditBufferProperty() {
        return this.tradeEditBuffer;
    }

    public ItemStack getBufferStack() {
        return this.tradeEditBuffer.get();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        return ScreenUtils.handleSlotTransfer(this, invSlot, this.shopInventory.size());
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (this.shop != null) this.shop.busy = false;
    }

    private static class AutoHidingSlot extends Slot {

        private final int targetTab;
        private final boolean hide;
        private final IntSupplier currentTabSupplier;

        public AutoHidingSlot(Inventory inventory, int index, int x, int y, int targetTab, boolean hide, IntSupplier currentTabSupplier) {
            super(inventory, index, x, y);
            this.targetTab = targetTab;
            this.hide = hide;
            this.currentTabSupplier = currentTabSupplier;
        }

        @Override
        public boolean isEnabled() {
            //noinspection SimplifiableConditionalExpression
            return hide
                    ? this.currentTabSupplier.getAsInt() != targetTab
                    : this.currentTabSupplier.getAsInt() == targetTab;
        }
    }
}
