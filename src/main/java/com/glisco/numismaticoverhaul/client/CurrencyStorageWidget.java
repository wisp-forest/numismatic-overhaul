package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.network.ShopScreenHandlerRequestC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class CurrencyStorageWidget extends DrawableHelper implements Drawable, Element {

    public static final Identifier TEXTURE = new Identifier(NumismaticOverhaul.MOD_ID, "textures/gui/shop_gui.png");

    private final MinecraftClient client;
    private final int x;
    private int y;
    private boolean active;

    private int currencyStorage;

    private final TexturedButtonWidget CONFIRM_BUTTON;

    public CurrencyStorageWidget(int x, int y, MinecraftClient client) {
        this.client = client;
        this.x = x;
        this.y = y;
        this.active = true;
        this.CONFIRM_BUTTON = new CurrencyRetrieveButton(x + 4, y + 41, button -> {
            client.getNetworkHandler().sendPacket(ShopScreenHandlerRequestC2SPacket.createEXTRACT());
        });
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!active) return;
        client.getTextureManager().bindTexture(TEXTURE);
        drawTexture(matrices, x, y, 146, 169, 34, 54);
        CONFIRM_BUTTON.render(matrices, mouseX, mouseY, delta);

        int[] values = CurrencyResolver.splitValues(currencyStorage);

        client.textRenderer.draw(matrices, new LiteralText("" + values[2]), x + 5, y + 7, 16777215);
        client.textRenderer.draw(matrices, new LiteralText("" + values[1]), x + 5, y + 19, 16777215);
        client.textRenderer.draw(matrices, new LiteralText("" + values[0]), x + 5, y + 31, 16777215);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return CONFIRM_BUTTON.mouseClicked(mouseX, mouseY, button);
    }

    public void setY(int y) {
        this.y = y;
        this.CONFIRM_BUTTON.setPos(this.CONFIRM_BUTTON.x, y + 41);
    }

    public void setCurrencyStorage(int currencyStorage) {
        this.currencyStorage = currencyStorage;
    }

    private static class CurrencyRetrieveButton extends TexturedButtonWidget {

        public CurrencyRetrieveButton(int x, int y, PressAction pressAction) {
            super(x, y, 26, 8, 146, 224, 16, CurrencyStorageWidget.TEXTURE, pressAction);
        }
    }
}
