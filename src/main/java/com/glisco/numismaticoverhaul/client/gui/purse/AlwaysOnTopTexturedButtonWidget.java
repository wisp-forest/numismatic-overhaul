package com.glisco.numismaticoverhaul.client.gui.purse;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * Extension of a normal {@link TexturedButtonWidget} that does no depth testing and thus always draws on top
 */
public class AlwaysOnTopTexturedButtonWidget extends TexturedButtonWidget {

    //Replicate some fields from super because they are private for reason
    private final int u;
    private final int v;
    private final int hoveredVOffset;

    private final Identifier texture;

    public AlwaysOnTopTexturedButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, PressAction pressAction) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, pressAction);

        this.u = u;
        this.v = v;
        this.hoveredVOffset = hoveredVOffset;
        this.texture = texture;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderTexture(0, texture);
        int i = this.v;
        if (this.isHovered()) {
            i += this.hoveredVOffset;
        }

        RenderSystem.disableDepthTest();

        drawTexture(matrices, this.x, this.y, this.u, i, this.width, this.height);
        if (this.isHovered()) this.renderTooltip(matrices, mouseX, mouseY);
    }
}
