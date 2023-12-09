package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.client.gui.ShopScreen;
import com.glisco.numismaticoverhaul.network.ShopScreenHandlerRequestC2SPacket;
import com.glisco.numismaticoverhaul.network.UpdateShopScreenS2CPacket;
import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import io.wispforest.owo.client.screens.SyncedProperty;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.List;

public class ShopScreenHandler extends ScreenHandler {

    private final PlayerEntity owner;

    private final Inventory shopInventory;
    private final SyncedProperty<ItemStack> tradeEditBuffer;

    private final List<ShopOffer> offers;

    private ShopBlockEntity shop = null;

    public ShopScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(27));
    }

    public ShopScreenHandler(int syncId, PlayerInventory playerInventory, Inventory shopInventory) {
        super(NumismaticOverhaul.SHOP_SCREEN_HANDLER_TYPE, syncId);
        this.shopInventory = shopInventory;
        this.owner = playerInventory.player;

        if (!this.owner.world.isClient) {
            this.shop = (ShopBlockEntity) shopInventory;
            this.offers = shop.getOffers();
        } else {
            this.offers = new ArrayList<>();
        }

        SlotGenerator.begin(this::addSlot, 8, 17)
                .slotFactory((inv, index, x, y) -> new AutoHidingSlot(inv, index, x, y, 0, false))
                .grid(this.shopInventory, 0, 9, 3)
                .slotFactory(Slot::new)
                .moveTo(8, 85)
                .playerInventory(playerInventory);

        this.tradeEditBuffer = this.createProperty(ItemStack.class, ItemStack.EMPTY);
        this.tradeEditBuffer.observe(stack -> {
            if (this.owner.world.isClient && MinecraftClient.getInstance().currentScreen instanceof ShopScreen screen) {
                screen.afterDataUpdate();
            }
        });
//
//        Trade Buffer Slot
//        this.bufferInventory.addListener(this::onBufferChanged);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.shopInventory.canPlayerUse(player);
    }

    public void loadOffer(long index) {
        if (!this.owner.world.isClient) {
            if (index > this.offers.size() - 1) {
                NumismaticOverhaul.LOGGER.error("Player {} attempted to load invalid trade at index {}", owner.getName(), index);
                return;
            }

            this.tradeEditBuffer.set(this.offers.get((int) index).getSellStack());
        } else {
            NumismaticOverhaul.CHANNEL.clientHandle().send(new ShopScreenHandlerRequestC2SPacket(ShopScreenHandlerRequestC2SPacket.Action.LOAD_OFFER, index));
        }
    }

    public void createOffer(long price) {
        if (!this.owner.world.isClient) {
            final var stack = this.tradeEditBuffer.get();
            if (stack.isEmpty()) return;

            this.shop.addOrReplaceOffer(new ShopOffer(stack, price));
            this.updateClient();
        } else {
            NumismaticOverhaul.CHANNEL.clientHandle().send(new ShopScreenHandlerRequestC2SPacket(ShopScreenHandlerRequestC2SPacket.Action.CREATE_OFFER, price));
        }
    }

    public void extractCurrency() {
        if (!this.owner.world.isClient) {
            ModComponents.CURRENCY.get(owner).modify(shop.getStoredCurrency());
            this.shop.setStoredCurrency(0);
            this.updateClient();
        } else {
            NumismaticOverhaul.CHANNEL.clientHandle().send(new ShopScreenHandlerRequestC2SPacket(ShopScreenHandlerRequestC2SPacket.Action.EXTRACT_CURRENCY));
        }
    }

    public void deleteOffer() {
        if (!this.owner.world.isClient) {
            this.shop.deleteOffer(this.tradeEditBuffer.get());
            this.updateClient();
        } else {
            NumismaticOverhaul.CHANNEL.clientHandle().send(new ShopScreenHandlerRequestC2SPacket(ShopScreenHandlerRequestC2SPacket.Action.DELETE_OFFER));
        }
    }

    public void toggleTransfer() {
        if (!this.owner.world.isClient) {
            this.shop.toggleTransfer();
            this.updateClient();
        } else {
            NumismaticOverhaul.CHANNEL.clientHandle().send(new ShopScreenHandlerRequestC2SPacket(ShopScreenHandlerRequestC2SPacket.Action.TOGGLE_TRANSFER));
        }
    }

    public void handleBufferClick() {
        if (!this.owner.world.isClient) {
            this.tradeEditBuffer.set(this.getCursorStack().copy());
        } else {
            NumismaticOverhaul.CHANNEL.clientHandle().send(new ShopScreenHandlerRequestC2SPacket(ShopScreenHandlerRequestC2SPacket.Action.CLICK_BUFFER));
        }
    }

    private void updateClient() {
        NumismaticOverhaul.CHANNEL.serverHandle(owner).send(new UpdateShopScreenS2CPacket(shop));
    }

    public ItemStack getBufferStack() {
        return this.tradeEditBuffer.get();
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        return ScreenUtils.handleSlotTransfer(this, invSlot, this.shopInventory.size());
    }

    private static class AutoHidingSlot extends Slot {

        private final int targetTab;
        private final boolean hide;

        public AutoHidingSlot(Inventory inventory, int index, int x, int y, int targetTab, boolean hide) {
            super(inventory, index, x, y);
            this.targetTab = targetTab;
            this.hide = hide;
        }

        @Override
        @Environment(EnvType.CLIENT)
        public boolean isEnabled() {
            if (!(MinecraftClient.getInstance().currentScreen instanceof ShopScreen screen)) return true;
            //noinspection SimplifiableConditionalExpression
            return hide
                    ? screen.tab() != targetTab
                    : screen.tab() == targetTab;
        }
    }
}
