package com.glisco.numismaticoverhaul.client.gui.shop;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.network.ShopScreenHandlerRequestC2SPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ToggleTransferWidget extends ButtonWidget {

    public boolean transferEnabled = false;

    public ToggleTransferWidget(int x, int y) {
        super(x, y, 28, 28, Text.of(""), button -> {
            NumismaticOverhaul.CHANNEL.clientHandle().send(new ShopScreenHandlerRequestC2SPacket(ShopScreenHandlerRequestC2SPacket.Action.TOGGLE_TRANSFER));
        });
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!this.active) return;

        RenderSystem.setShaderTexture(0, ShopScreen.TEXTURE);

        drawTexture(matrices, x, y,
                this.transferEnabled ? 176 : 204,
                this.hovered ? 28 : 0,
                28, 28);
    }
}
