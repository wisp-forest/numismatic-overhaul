package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.ShopOffer;
import com.glisco.numismaticoverhaul.block.ShopScreenHandler;
import com.glisco.numismaticoverhaul.network.ModifyShopOfferC2SPacket;
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

import java.util.ArrayList;
import java.util.List;

public class ShopScreen extends HandledScreen<ShopScreenHandler> {

    public static final Identifier TEXTURE = new Identifier(NumismaticOverhaul.MOD_ID, "textures/gui/shop_gui.png");
    public static final Identifier TRADES_TEXTURE = new Identifier(NumismaticOverhaul.MOD_ID, "textures/gui/shop_gui_trades.png");

    private AddTradeWidget TRADE_WIDGET;
    int selected_tab = 0;

    private final List<ButtonWidget> tradeButtons = new ArrayList<>();
    private List<ShopOffer> offers = new ArrayList<>();

    public ShopScreen(ShopScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.playerInventoryTitleY += 3;
        this.backgroundHeight = 168;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {

        client.getTextureManager().bindTexture(selected_tab == 0 ? TEXTURE : TRADES_TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);

        drawTab(matrices, y + 14, selected_tab == 0);
        drawTab(matrices, y + 46, selected_tab == 1);

        client.getItemRenderer().renderInGuiWithOverrides(new ItemStack(Items.CHEST), x - 20, y + 19);
        client.getItemRenderer().renderInGuiWithOverrides(new ItemStack(Items.EMERALD), x - 20, y + 52);
    }

    private void drawTab(MatrixStack matrices, int y, boolean selected) {
        client.getTextureManager().bindTexture(TEXTURE);
        int x = (width - backgroundWidth) / 2;
        drawTexture(matrices, x - (selected ? 29 : 27), y, 113, selected ? 196 : 168, 32, 28);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);

        TRADE_WIDGET.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);

        if (selected_tab == 1) {
            for (ButtonWidget button : tradeButtons) {
                button.render(matrices, mouseX, mouseY, delta);
            }
        }

        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    public void updateShopOffers(List<ShopOffer> offers) {

        tradeButtons.clear();
        this.offers = offers;

        for (int i = 0; i < offers.size(); i++) {
            tradeButtons.add(new TradeButtonWidget(x + (i < 3 ? 8 : 90), y + 10 + (i % 3) * 20, offers.get(i).getPrice(), offers.get(i).getSellStack(), i));
        }
    }

    public void loadOffer(int index) {
        TRADE_WIDGET.setText(String.valueOf(offers.get(index).getPrice()));
        client.getNetworkHandler().sendPacket(ModifyShopOfferC2SPacket.createLOAD(index));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (mouseX >= x - 27 && mouseX <= x) {
            if (mouseY >= y + 14 && mouseY <= y + 42) {
                selectTab(0);
                return true;
            } else if (mouseY >= y + 46 && mouseY <= y + 74) {
                selectTab(1);
                return true;
            }
        }

        if (selected_tab == 1) {
            for (ButtonWidget buttonWidget : tradeButtons) {
                if (buttonWidget.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }

        return TRADE_WIDGET.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    private void selectTab(int tab) {
        selected_tab = tab;
        TRADE_WIDGET.setActive(tab == 1);
        this.titleY = tab == 0 ? 6 : 6000;
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public int getSelectedTab() {
        return selected_tab;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return TRADE_WIDGET.isMouseOver(mouseX, mouseY) || super.isMouseOver(mouseX, mouseY);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.client.player.closeHandledScreen();
        }

        return this.TRADE_WIDGET.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return this.TRADE_WIDGET.charTyped(chr, modifiers);
    }

    //TODO ScreenHandlerListener to update button active state
    public boolean isBufferEmpty() {
        return this.handler.slots.get(this.handler.slots.size() - 1).hasStack();
    }


    @Override
    protected void init() {
        super.init();
        this.TRADE_WIDGET = new AddTradeWidget(x + 178, y + 21, client, this);
    }
}
