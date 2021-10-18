package com.glisco.numismaticoverhaul.client.gui.shop;

import com.glisco.numismaticoverhaul.client.gui.CurrencyTooltipRenderer;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;

public class TradeButtonWidget extends ButtonWidget {

    private final ItemStack renderItem;
    private final int price;
    private final Text PRICE_TITLE = new TranslatableText("gui.numismatic-overhaul.shop.price").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(CurrencyResolver.Currency.GOLD.getNameColor())));

    public TradeButtonWidget(int x, int y, int price, ItemStack item, int index) {
        super(x, y, 78, 20, new LiteralText(""), button -> {
            ((ShopScreen) MinecraftClient.getInstance().currentScreen).loadOffer(index);
        });
        this.price = price;
        this.renderItem = item;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);

        RenderSystem.setShaderTexture(0, ShopScreen.TEXTURE);
        RenderSystem.enableDepthTest();
        drawTexture(matrices, x + 22, y + 3, 0, 168, 7, 12);

        MinecraftClient.getInstance().getItemRenderer().zOffset = 0.0f;
        MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(renderItem, x + 4, y + 2);
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, String.valueOf(price), x + 30, y + 6, 0xffffff);
    }

    @Override
    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (mouseX > x && mouseX < x + 22) {
            screen.renderTooltip(matrices, screen.getTooltipFromItem(renderItem), mouseX, mouseY);
        } else if (mouseX > x + 26 && mouseX < x + 65) {
            CurrencyTooltipRenderer.renderTooltip(price, matrices, screen, PRICE_TITLE, mouseX, mouseY);
        }

    }
}
