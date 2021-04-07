package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.client.ShopScreen;
import com.glisco.numismaticoverhaul.network.SetShopOffersS2CPacket;
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
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class ShopScreenHandler extends ScreenHandler {

    private final PlayerEntity owner;

    private final Inventory shopInventory;
    private final Inventory BUFFER_INVENTORY = new SimpleInventory(1) {
        @Override
        public void markDirty() {
            onBufferChanged();
        }
    };

    private final List<ShopOffer> offers;

    @Environment(EnvType.SERVER)
    private ShopBlockEntity shop = null;

    public ShopScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(27));
    }

    public ShopScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(NumismaticOverhaul.SHOP_SCREEN_HANDLER_TYPE, syncId);
        this.shopInventory = inventory;
        this.owner = playerInventory.player;

        if (!playerInventory.player.world.isClient()) {
            this.shop = (ShopBlockEntity) inventory;
            this.offers = shop.getOffers();
        } else {
            this.offers = new ArrayList<>();
        }

        int m;
        int l;

        //Shop Inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new AutoHidingSlot(shopInventory, l + m * 9, 8 + l * 18, 18 + m * 18, 0, false));
            }
        }

        //The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 86 + m * 18));
            }
        }
        //The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 144));
        }

        //Trade Buffer Slot
        this.addSlot(new AutoHidingSlot(BUFFER_INVENTORY, 0, 186, 36, 0, true) {
            @Override
            public boolean canInsert(ItemStack stack) {
                ItemStack shadow = stack.copy();
                shadow.setCount(1);
                this.setStack(shadow);
                return false;
            }

            @Override
            public boolean canTakeItems(PlayerEntity playerEntity) {
                this.setStack(ItemStack.EMPTY);
                return false;
            }
        });

    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.shopInventory.canPlayerUse(player);
    }

    public void onBufferChanged() {
        if (this.owner.world.isClient) {
            ((ShopScreen) MinecraftClient.getInstance().currentScreen).updateTradeWidget();
        }
    }

    public void loadOffer(int index) {

        if (index > offers.size() - 1) {
            NumismaticOverhaul.LOGGER.error("Player {} attempted to load invalid trade at index {}", owner.getName(), index);
            return;
        }

        ShopOffer offer = offers.get(index);
        BUFFER_INVENTORY.setStack(0, offer.getSellStack());
    }

    public void createOffer(int price) {
        shop.addOrReplaceOffer(new ShopOffer(BUFFER_INVENTORY.getStack(0), price));
        ((ServerPlayerEntity) owner).networkHandler.sendPacket(SetShopOffersS2CPacket.create(shop.getOffers()));
    }

    public ItemStack getBufferStack() {
        return BUFFER_INVENTORY.getStack(0);
    }

    public void deleteOffer() {
        shop.deleteOffer(BUFFER_INVENTORY.getStack(0));
        ((ServerPlayerEntity) owner).networkHandler.sendPacket(SetShopOffersS2CPacket.create(shop.getOffers()));
    }

    // Shift + Player Inv Slot
    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.shopInventory.size()) {
                if (!this.insertItem(originalStack, this.shopInventory.size(), this.slots.size() - 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.shopInventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
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
        public boolean doDrawHoveringEffect() {
            if (!(MinecraftClient.getInstance().currentScreen instanceof ShopScreen)) return true;
            if (hide) {
                return ((ShopScreen) MinecraftClient.getInstance().currentScreen).getSelectedTab() != targetTab;
            } else {
                return ((ShopScreen) MinecraftClient.getInstance().currentScreen).getSelectedTab() == targetTab;
            }
        }
    }
}
