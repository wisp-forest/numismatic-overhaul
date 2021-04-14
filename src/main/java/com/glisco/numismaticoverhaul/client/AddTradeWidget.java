package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.network.ShopScreenHandlerRequestC2SPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AddTradeWidget extends DrawableHelper implements Drawable, Element {

    public static final Identifier TEXTURE = new Identifier(NumismaticOverhaul.MOD_ID, "textures/gui/shop_gui.png");
    private final MinecraftClient client;
    private final int x;
    private final int y;
    private final ShopScreen parent;

    private boolean active = false;
    private final List<ButtonWidget> buttons = new ArrayList<>();

    private TextFieldWidget PRICE_FIELD;

    public AddTradeWidget(int x, int y, MinecraftClient client, ShopScreen parent) {

        this.client = client;
        this.x = x;
        this.y = y;
        this.parent = parent;

        buttons.add(new TradeWidgetButton(x + 7, y + 36, button -> {
            client.getNetworkHandler().sendPacket(ShopScreenHandlerRequestC2SPacket.createCREATE(Integer.parseInt(PRICE_FIELD.getText())));
        }, true));

        buttons.add(new TradeWidgetButton(x + 50, y + 36, button -> {
            client.getNetworkHandler().sendPacket(ShopScreenHandlerRequestC2SPacket.createDELETE());
        }, false));

        client.keyboard.setRepeatEvents(true);
        PRICE_FIELD = new TextFieldWidget(client.textRenderer, x + 36, y + 19, 64, 14, new LiteralText("")) {
            @Override
            public void write(String string) {
                if (!string.matches("[0-9]+")) return;
                super.write(string);
            }
        };

        PRICE_FIELD.setMaxLength(7);
        PRICE_FIELD.setDrawsBackground(false);
        PRICE_FIELD.active = true;
        PRICE_FIELD.setChangedListener(this::onTextChanged);
    }

    private void onTextChanged(String newText) {
        updateButtonActiveState();
    }

    public void updateButtonActiveState() {

        buttons.get(0).active = isEnteredTextValid() && (parent.getOffers().size() < 24 || doesTradeExistFor(parent.getBuffer()));

        buttons.get(1).active = doesTradeExistFor(parent.getBuffer());
    }

    private boolean doesTradeExistFor(ItemStack stack) {
        return parent.getOffers().stream().anyMatch(shopOffer -> ItemStack.areEqual(stack, shopOffer.getSellStack()));
    }

    private boolean isEnteredTextValid() {
        if (StringUtils.isBlank(PRICE_FIELD.getText())) {
            return false;
        }
        return Integer.parseInt(PRICE_FIELD.getText()) != 0;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!active) return;

        client.getTextureManager().bindTexture(TEXTURE);
        drawTexture(matrices, x, y, 15, 169, 98, 54);

        for (ButtonWidget button : buttons) {
            button.render(matrices, mouseX, mouseY, delta);
        }

        drawTexture(matrices, x + 28, y + 16, 0, 168, 7, 12);

        int[] values = CurrencyResolver.splitValues(isEnteredTextValid() ? Integer.parseInt(PRICE_FIELD.getText()) : 0);

        renderText(matrices, String.valueOf(values[0]), x + 36, y + 6, 7f, 0x8b8b8b);
        renderText(matrices, String.valueOf(values[1]), x + 56, y + 6, 7f, 0x8b8b8b);
        renderText(matrices, String.valueOf(values[2]), x + 76, y + 6, 7f, 0x8b8b8b);

        PRICE_FIELD.render(matrices, mouseX, mouseY, delta);
        RenderSystem.color4f(255.0f, 255.0f, 255.0f, 255.0f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        for (ButtonWidget buttonWidget : buttons) {
            if (buttonWidget.mouseClicked(mouseX, mouseY, button)) return true;
        }

        if (PRICE_FIELD.mouseClicked(mouseX, mouseY, button)) return true;

        return active && isMouseOver(mouseX, mouseY);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return active && this.PRICE_FIELD.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return active && this.PRICE_FIELD.charTyped(chr, modifiers);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + 98 && mouseY >= y && mouseY <= y + 45 && active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setText(String text) {
        PRICE_FIELD.setText(text);
        buttons.forEach(buttonWidget -> buttonWidget.active = true);
    }

    private void renderText(MatrixStack matrices, String text, int x, int y, float size, int color) {
        matrices.push();
        float scaling = ((size * 1.2f) / (float) client.textRenderer.fontHeight);
        matrices.scale(scaling, scaling, 1);
        client.textRenderer.draw(matrices, text, x / scaling, y / scaling, color);
        matrices.pop();
    }

    private static class TradeWidgetButton extends TexturedButtonWidget {

        private final int hoveredVOffset;
        private final int u;
        private final int v;

        private final Identifier texture = AddTradeWidget.TEXTURE;

        public TradeWidgetButton(int x, int y, PressAction pressAction, boolean confirm) {
            super(x, y, 41, 11, confirm ? 15 : 56, 234, 11, AddTradeWidget.TEXTURE, pressAction);

            hoveredVOffset = height;
            this.u = confirm ? 15 : 56;
            this.v = 234;

            active = false;
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            minecraftClient.getTextureManager().bindTexture(this.texture);
            int i = this.v;

            if (this.active) {
                if (this.isHovered()) {
                    i += this.hoveredVOffset;
                }
            } else {
                i -= hoveredVOffset;
            }


            RenderSystem.enableDepthTest();
            drawTexture(matrices, this.x, this.y, (float) this.u, (float) i, this.width, this.height, 256, 256);
            if (this.isHovered()) {
                this.renderToolTip(matrices, mouseX, mouseY);
            }
        }
    }
}
