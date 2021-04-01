package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.currency.CurrencyComponent;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public class PurseButton extends TexturedButtonWidget {

    private final CurrencyComponent currencyStorage;
    private final Screen parent;
    private final Text TOOLTIP_TITLE;

    public PurseButton(int x, int y, PressAction pressAction, PlayerEntity player, Screen parent) {
        super(x, y, 11, 12, 62, 0, 12, PurseWidget.TEXTURE, pressAction);
        this.currencyStorage = ModComponents.CURRENCY.get(player);
        this.parent = parent;
        this.TOOLTIP_TITLE = new LiteralText("Purse").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(CurrencyResolver.Currency.GOLD.getNameColor())));
    }

    @Override
    public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
        CurrencyTooltipRenderer.renderTooltip(
                currencyStorage.getCurrencyStack(),
                matrices, parent,
                TOOLTIP_TITLE,
                x + 14, mouseY - 15);
    }


}
