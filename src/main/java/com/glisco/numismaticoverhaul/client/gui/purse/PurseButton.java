package com.glisco.numismaticoverhaul.client.gui.purse;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.client.gui.CurrencyTooltipRenderer;
import com.glisco.numismaticoverhaul.currency.Currency;
import com.glisco.numismaticoverhaul.currency.CurrencyComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;

public class PurseButton extends TexturedButtonWidget {

    private final CurrencyComponent currencyStorage;
    private final Screen parent;
    private final Text TOOLTIP_TITLE;

    public PurseButton(int x, int y, PressAction pressAction, PlayerEntity player, Screen parent) {
        super(x, y, 11, 13, 62, 0, 13, PurseWidget.TEXTURE, pressAction);
        this.currencyStorage = ModComponents.CURRENCY.get(player);
        this.parent = parent;
        this.TOOLTIP_TITLE = new TranslatableText("gui.numismatic-overhaul.purse_title").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Currency.GOLD.getNameColor())));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MinecraftClient.getInstance().player.isSpectator()) return false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        CurrencyTooltipRenderer.renderTooltip(
                currencyStorage.getValue(),
                matrices, parent,
                TOOLTIP_TITLE,
                x + 14, y + 5);
    }
}
