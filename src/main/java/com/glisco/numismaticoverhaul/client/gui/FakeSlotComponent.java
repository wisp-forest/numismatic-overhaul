package com.glisco.numismaticoverhaul.client.gui;

import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

public class FakeSlotComponent extends ItemComponent {

    protected FakeSlotComponent(ItemStack stack) {
        super(stack);
    }

    @Override
    public boolean shouldDrawTooltip(double mouseX, double mouseY) {
        //noinspection DataFlowIssue
        var screenHandler = MinecraftClient.getInstance().player.currentScreenHandler;
        return (screenHandler == null || screenHandler.getCursorStack().isEmpty()) && super.shouldDrawTooltip(mouseX, mouseY);
    }

    public static void init() {
        // no-op
    }

    static {
        UIParsing.registerFactory("numismatic.fake-slot", element -> new FakeSlotComponent(ItemStack.EMPTY));
    }
}
