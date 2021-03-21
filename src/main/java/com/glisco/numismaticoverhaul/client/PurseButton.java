package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.currency.CurrencyComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

public class PurseButton extends TexturedButtonWidget {

    private final CurrencyComponent currencyStorage;

    public PurseButton(int x, int y, PressAction pressAction, PlayerEntity player) {
        super(x, y, 11, 12, 62, 0, 12, PurseWidget.TEXTURE, pressAction);
        this.currencyStorage = ModComponents.CURRENCY.get(player);
    }

    @Override
    public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {

        //Try as hard as possible to have this on top, however because of drawing order not that much can be done
        RenderSystem.disableDepthTest();

        //Draw this fixed to the side of the inventory
        drawTexture(matrices, x + 20, mouseY - 25, 73, 0, 44, 39);

        MinecraftClient.getInstance().textRenderer.draw(matrices, String.valueOf(currencyStorage.getCurrencyStack().getAsItemStackArray()[2].getCount()), x + 35, mouseY - 20, 11184810);
        MinecraftClient.getInstance().textRenderer.draw(matrices, String.valueOf(currencyStorage.getCurrencyStack().getAsItemStackArray()[1].getCount()), x + 35, mouseY - 9, 11184810);
        MinecraftClient.getInstance().textRenderer.draw(matrices, String.valueOf(currencyStorage.getCurrencyStack().getAsItemStackArray()[0].getCount()), x + 35, mouseY + 2, 11184810);

    }

    /*@Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);
    }*/
}
