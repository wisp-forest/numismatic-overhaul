package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyComponent;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.network.RequestPurseActionC2SPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.List;

public class PurseWidget extends DrawableHelper implements Drawable, Element {

    public static final Identifier TEXTURE = new Identifier(NumismaticOverhaul.MOD_ID, "textures/gui/purse_widget.png");
    private final MinecraftClient client;
    private int x;
    private int y;

    private MutableInt goldAmount = new MutableInt(0);
    private MutableInt silverAmount = new MutableInt(0);
    private MutableInt bronzeAmount = new MutableInt(0);

    private List<ButtonWidget> buttons = new ArrayList<>();

    private final CurrencyComponent currencyStorage;

    private boolean active = false;

    public PurseWidget(int x, int y, MinecraftClient client, CurrencyComponent currencyStorage) {

        this.client = client;
        this.x = x;
        this.y = y;

        buttons.add(new SmallPurseAdjustButton(x + 18, y + 5, button -> modifyInBounds(goldAmount, true, CurrencyResolver.Currency.GOLD), true));
        buttons.add(new SmallPurseAdjustButton(x + 18, y + 11, button -> modifyInBounds(goldAmount, false, CurrencyResolver.Currency.GOLD), false));

        buttons.add(new SmallPurseAdjustButton(x + 18, y + 17, button -> modifyInBounds(silverAmount, true, CurrencyResolver.Currency.SILVER), true));
        buttons.add(new SmallPurseAdjustButton(x + 18, y + 23, button -> modifyInBounds(silverAmount, false, CurrencyResolver.Currency.SILVER), false));

        buttons.add(new SmallPurseAdjustButton(x + 18, y + 29, button -> modifyInBounds(bronzeAmount, true, CurrencyResolver.Currency.BRONZE), true));
        buttons.add(new SmallPurseAdjustButton(x + 18, y + 35, button -> modifyInBounds(bronzeAmount, false, CurrencyResolver.Currency.BRONZE), false));

        buttons.add(new AlwaysOnTopTexturedButtonWidget(x + 3, y + 41, 24, 8, 37, 0, 16, TEXTURE, button -> {

            if (Screen.hasShiftDown() && Screen.hasControlDown()) {
                client.getNetworkHandler().sendPacket(RequestPurseActionC2SPacket.create(RequestPurseActionC2SPacket.Action.EXTRACT_ALL));
            } else {
                if (getCurrentSelectedValue() > 0) {
                    client.getNetworkHandler().sendPacket(RequestPurseActionC2SPacket.create(RequestPurseActionC2SPacket.Action.EXTRACT, getCurrentSelectedValue()));
                    resetSelectedValue();
                }
            }
        }));

        this.currencyStorage = currencyStorage;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!active) return;

        RenderSystem.disableDepthTest();
        client.getTextureManager().bindTexture(TEXTURE);
        drawTexture(matrices, x, y, 0, 0, 37, 60);

        for (ButtonWidget button : buttons) {
            button.render(matrices, mouseX, mouseY, delta);
        }

        client.textRenderer.draw(matrices, new LiteralText("" + goldAmount), x + 5, y + 7, 16777215);
        client.textRenderer.draw(matrices, new LiteralText("" + silverAmount), x + 5, y + 19, 16777215);
        client.textRenderer.draw(matrices, new LiteralText("" + bronzeAmount), x + 5, y + 31, 16777215);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ButtonWidget buttonWidget : buttons) {
            if (buttonWidget.mouseClicked(mouseX, mouseY, button)) return true;
        }

        return isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + 37 && mouseY >= y && mouseY <= y + 57 && active;
    }

    public void toggleActive() {
        active = !active;
    }

    private void modifyInBounds(MutableInt value, boolean add, CurrencyResolver.Currency currency) {
        modifyInBounds(value, Screen.hasShiftDown() ? 10 : 1, add, currency);
    }

    private void modifyInBounds(MutableInt value, int modifyBy, boolean add, CurrencyResolver.Currency currency) {

        int stepSize = currency.getRawValue(1);
        int possibleSteps = (currencyStorage.getValue() - getCurrentSelectedValue()) / stepSize;

        int upperBound = Math.min(value.intValue() + possibleSteps, 99);

        if (add) value.add(modifyBy);
        else value.subtract(modifyBy);

        if (value.intValue() < 0) value.setValue(0);
        if (value.intValue() > upperBound) value.setValue(upperBound);
    }

    private int getCurrentSelectedValue() {
        return CurrencyResolver.getRawValue(new int[]{bronzeAmount.getValue(), silverAmount.getValue(), goldAmount.getValue()});
    }

    private void resetSelectedValue() {

        currencyStorage.silentModify(-getCurrentSelectedValue());

        int oldGoldAmount = goldAmount.intValue();
        int oldSilverAmount = silverAmount.intValue();
        int oldBronzeAmount = bronzeAmount.intValue();

        goldAmount.setValue(0);
        bronzeAmount.setValue(0);
        silverAmount.setValue(0);

        modifyInBounds(goldAmount, oldGoldAmount, true, CurrencyResolver.Currency.GOLD);
        modifyInBounds(silverAmount, oldSilverAmount, true, CurrencyResolver.Currency.SILVER);
        modifyInBounds(bronzeAmount, oldBronzeAmount, true, CurrencyResolver.Currency.BRONZE);
    }

    public static class SmallPurseAdjustButton extends AlwaysOnTopTexturedButtonWidget {
        public SmallPurseAdjustButton(int x, int y, PressAction pressAction, boolean add) {
            super(x, y, 9, 5, add ? 37 : 46, 24, 10, PurseWidget.TEXTURE, pressAction);
        }
    }
}
