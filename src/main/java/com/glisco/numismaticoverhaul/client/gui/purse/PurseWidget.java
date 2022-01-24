package com.glisco.numismaticoverhaul.client.gui.purse;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.Currency;
import com.glisco.numismaticoverhaul.currency.CurrencyComponent;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.network.RequestPurseActionC2SPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.List;

public class PurseWidget extends DrawableHelper implements Drawable, Element, Selectable {

    public static final Identifier TEXTURE = new Identifier(NumismaticOverhaul.MOD_ID, "textures/gui/purse_widget.png");
    private final MinecraftClient client;
    private final int x;
    private final int y;

    private boolean active = false;
    private final List<ButtonWidget> buttons = new ArrayList<>();

    private final MutableInt goldAmount = new MutableInt(0);
    private final MutableInt silverAmount = new MutableInt(0);
    private final MutableInt bronzeAmount = new MutableInt(0);
    private final CurrencyComponent currencyStorage;

    public PurseWidget(int x, int y, MinecraftClient client, CurrencyComponent currencyStorage) {
        this.client = client;
        this.x = x;
        this.y = y;

        buttons.add(new SmallPurseAdjustButton(x + 18, y + 10, button -> modifyInBounds(goldAmount, true, Currency.GOLD), true));
        buttons.add(new SmallPurseAdjustButton(x + 18, y + 16, button -> modifyInBounds(goldAmount, false, Currency.GOLD), false));

        buttons.add(new SmallPurseAdjustButton(x + 18, y + 22, button -> modifyInBounds(silverAmount, true, Currency.SILVER), true));
        buttons.add(new SmallPurseAdjustButton(x + 18, y + 28, button -> modifyInBounds(silverAmount, false, Currency.SILVER), false));

        buttons.add(new SmallPurseAdjustButton(x + 18, y + 34, button -> modifyInBounds(bronzeAmount, true, Currency.BRONZE), true));
        buttons.add(new SmallPurseAdjustButton(x + 18, y + 40, button -> modifyInBounds(bronzeAmount, false, Currency.BRONZE), false));

        buttons.add(new AlwaysOnTopTexturedButtonWidget(x + 3, y + 46, 24, 8, 37, 0, 16, TEXTURE, button -> {
            if (Screen.hasShiftDown() && Screen.hasControlDown()) {
                NumismaticOverhaul.CHANNEL.clientHandle().send(RequestPurseActionC2SPacket.extractAll());
            } else if (selectedValue() > 0) {
                NumismaticOverhaul.CHANNEL.clientHandle().send(RequestPurseActionC2SPacket.extract(selectedValue()));
                resetSelectedValue();
            }
        }));

        this.currencyStorage = currencyStorage;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!active) return;

        //Draw over items in the crafting interface
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderTexture(0, TEXTURE);
        drawTexture(matrices, x, y, 0, 0, 37, 60);

        for (ButtonWidget button : buttons) {
            button.render(matrices, mouseX, mouseY, delta);
        }

        client.textRenderer.draw(matrices, new LiteralText("" + goldAmount), x + 5, y + 12, 16777215);
        client.textRenderer.draw(matrices, new LiteralText("" + silverAmount), x + 5, y + 24, 16777215);
        client.textRenderer.draw(matrices, new LiteralText("" + bronzeAmount), x + 5, y + 36, 16777215);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active) return false;

        for (ButtonWidget buttonWidget : buttons) {
            if (buttonWidget.mouseClicked(mouseX, mouseY, button)) return true;
        }

        return isMouseOver(mouseX, mouseY);
    }

    //Required to not draw tooltips for items in the crafting interface
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + 37 && mouseY >= y && mouseY <= y + 57 && active;
    }

    public void toggleActive() {
        active = !active;
    }

    /**
     * Modifies a value by either 1 or 10 depending on whether or not SHIFT is held
     * <br>
     * Shortcut for {@link PurseWidget#modifyInBounds(MutableInt, int, boolean, Currency)}
     */
    private void modifyInBounds(MutableInt value, boolean add, Currency currency) {
        modifyInBounds(value, Screen.hasShiftDown() ? 10 : 1, add, currency);
    }


    /**
     * Modifies a value with respect to the total amount of money the player has.
     * If modifying the value even by one would surpass the player's worth when added
     * to the two other selected values, nothing will happen
     *
     * @param value    The value to modify
     * @param modifyBy The amount to modify by
     * @param add      Whether to add or subtract
     * @param currency The currency this selector is for
     */
    private void modifyInBounds(MutableInt value, int modifyBy, boolean add, Currency currency) {

        //Get the step size of this selector
        int stepSize = currency.getRawValue(1);

        //Calculate possible steps using the difference between the player's worth and the currently selected values added together
        int possibleSteps = (currencyStorage.getValue() - selectedValue()) / stepSize;

        //Upper bound is either 99 or the the current value of this selector plus the possible steps
        int upperBound = Math.min(value.intValue() + possibleSteps, 99);

        if (add) value.add(modifyBy);
        else value.subtract(modifyBy);

        if (value.intValue() < 0) value.setValue(0);
        if (value.intValue() > upperBound) value.setValue(upperBound);
    }

    /**
     * Resolves the selected values into a raw currency value
     *
     * @return The raw value of all selectors added with respect to their different worths
     */
    private int selectedValue() {
        return CurrencyResolver.combineValues(new int[]{bronzeAmount.getValue(), silverAmount.getValue(), goldAmount.getValue()});
    }

    /**
     * This adjusts the extract values to the maximum you can do by simply setting
     * them to zero and then letting them run into bounds
     */
    private void resetSelectedValue() {

        //Silently modify client cache because this runs before the sync packet is received
        currencyStorage.silentModify(-selectedValue());

        int oldGoldAmount = goldAmount.intValue();
        int oldSilverAmount = silverAmount.intValue();
        int oldBronzeAmount = bronzeAmount.intValue();

        goldAmount.setValue(0);
        bronzeAmount.setValue(0);
        silverAmount.setValue(0);

        modifyInBounds(goldAmount, oldGoldAmount, true, Currency.GOLD);
        modifyInBounds(silverAmount, oldSilverAmount, true, Currency.SILVER);
        modifyInBounds(bronzeAmount, oldBronzeAmount, true, Currency.BRONZE);
    }

    @Override
    public SelectionType getType() {
        return SelectionType.FOCUSED;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    /**
     * Convenience class so I don't have init 6 buttons with the same config
     */
    public static class SmallPurseAdjustButton extends AlwaysOnTopTexturedButtonWidget {
        public SmallPurseAdjustButton(int x, int y, PressAction pressAction, boolean add) {
            super(x, y, 9, 5, add ? 37 : 46, 24, 10, PurseWidget.TEXTURE, pressAction);
        }
    }
}
