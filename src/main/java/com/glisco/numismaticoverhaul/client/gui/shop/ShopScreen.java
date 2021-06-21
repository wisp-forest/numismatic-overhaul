package com.glisco.numismaticoverhaul.client.gui.shop;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.ShopOffer;
import com.glisco.numismaticoverhaul.block.ShopScreenHandler;
import com.glisco.numismaticoverhaul.network.ShopScreenHandlerRequestC2SPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ShopScreen extends HandledScreen<ShopScreenHandler> {

    public static final Identifier TEXTURE = new Identifier(NumismaticOverhaul.MOD_ID, "textures/gui/shop_gui.png");
    public static final Identifier TRADES_TEXTURE = new Identifier(NumismaticOverhaul.MOD_ID, "textures/gui/shop_gui_trades.png");

    private AddTradeWidget TRADE_WIDGET;
    private CurrencyStorageWidget CURRENCY_WIDGET;

    int selected_tab = 0;
    private int tabs = 1;

    private final List<ButtonWidget> tradeButtons = new ArrayList<>();
    private List<ShopOffer> offers = new ArrayList<>();

    public ShopScreen(ShopScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.playerInventoryTitleY += 3;
        this.backgroundHeight = 168;
    }

    @Override
    protected void init() {
        super.init();
        this.TRADE_WIDGET = new AddTradeWidget(x + 178, y, client, this);
        this.CURRENCY_WIDGET = new CurrencyStorageWidget(x + 178, y + 60, client);
        updateScreen(offers, 0);
        selectTab(selected_tab, false);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {

        RenderSystem.setShaderTexture(0, selected_tab == 0 ? TEXTURE : TRADES_TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);

        for (int i = 0; i < tabs; i++) {
            drawTab(matrices, i, selected_tab == i);
        }
    }

    private void drawTab(MatrixStack matrices, int index, boolean selected) {
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        drawTexture(matrices, x - (selected ? 29 : 27), y + 5 + index * 32, 113, selected ? 196 : 168, 32, 28);

        client.getItemRenderer().renderInGuiWithOverrides(new ItemStack(index == 0 ? Items.CHEST : Items.EMERALD), x - 20, y + 11 + index * 32);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);

        TRADE_WIDGET.render(matrices, mouseX, mouseY, delta);
        CURRENCY_WIDGET.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);

        if (selected_tab != 0) {

            int offset = (selected_tab - 1) * 6;

            for (int i = offset; i < 6 + offset; i++) {
                if (i > tradeButtons.size() - 1) break;
                tradeButtons.get(i).render(matrices, mouseX, mouseY, delta);
            }
        }

        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (mouseX >= x - 27 && mouseX <= x) {
            for (int i = 0; i < tabs; i++) {
                if (mouseY >= y + 14 + i * 32 && mouseY <= y + 42 + i * 32) {
                    selectTab(i, true);
                    return true;
                }
            }
        }

        if (selected_tab != 0) {
            int offset = (selected_tab - 1) * 6;

            for (int i = offset; i < 6 + offset; i++) {
                if (i > tradeButtons.size() - 1) break;
                tradeButtons.get(i).mouseClicked(mouseX, mouseY, button);
            }
        }

        return CURRENCY_WIDGET.mouseClicked(mouseX, mouseY, button) || TRADE_WIDGET.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return TRADE_WIDGET.isMouseOver(mouseX, mouseY) || super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.TRADE_WIDGET.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return this.TRADE_WIDGET.charTyped(chr, modifiers);
    }


    public void updateTradeWidget() {
        this.TRADE_WIDGET.updateButtonActiveState();
    }

    public void updateScreen(List<ShopOffer> offers, int storedCurrency) {

        tradeButtons.clear();
        this.offers = offers;

        this.tabs = (int) Math.ceil(1 + offers.size() / 6.0d);
        if (this.tabs == 1) tabs++;
        if (this.selected_tab > tabs - 1) this.selected_tab = tabs - 1;

        for (int i = 0; i < offers.size(); i++) {
            tradeButtons.add(new TradeButtonWidget(x + (i % 6 < 3 ? 8 : 90), y + 10 + (i % 3) * 20, offers.get(i).getPrice(), offers.get(i).getSellStack(), i));
        }

        trySelectTab();
        updateTradeWidget();

        CURRENCY_WIDGET.setCurrencyStorage(storedCurrency);
    }

    public void loadOffer(int index) {
        TRADE_WIDGET.setText(String.valueOf(offers.get(index).getPrice()));
        client.getNetworkHandler().sendPacket(ShopScreenHandlerRequestC2SPacket.createLOAD(index));
    }

    private void selectTab(int tab, boolean clickSound) {
        selected_tab = tab;
        TRADE_WIDGET.setActive(tab != 0);
        CURRENCY_WIDGET.setY(tab == 0 ? y : y + 60);
        this.titleY = tab == 0 ? 6 : 6000;
        if (clickSound) client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public int getSelectedTab() {
        return selected_tab;
    }

    public void trySelectTab() {

        ItemStack tradeItem = this.handler.getBufferStack();

        for (ShopOffer offer : offers) {
            if (!ItemStack.areEqual(offer.getSellStack(), tradeItem)) continue;
            this.selected_tab = 1 + offers.indexOf(offer) / 6;
            return;
        }
    }

    public int getRootX() {
        return (width - backgroundWidth) / 2;
    }

    public int getRootY() {
        return (height - backgroundHeight) / 2;
    }

    public ItemStack getBuffer() {
        return this.handler.getBufferStack();
    }

    public List<ShopOffer> getOffers() {
        return offers;
    }
}
