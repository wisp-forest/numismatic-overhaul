package com.glisco.numismaticoverhaul.client.gui;

import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class CurrencyTooltipRenderer {

    public static void renderTooltip(long value, MatrixStack matrices, Screen screen, Text title, int x, int y) {

        List<Text> tooltip = new ArrayList<>();
        tooltip.add(title);

        y += 10;

        List<ItemStack> coins = CurrencyConverter.getAsItemStackList(value);

        MinecraftClient.getInstance().getItemRenderer().zOffset = 700.0f;

        for (int i = 0; i < coins.size(); i++) {
            renderStack(matrices, coins.get(i), tooltip, i, x, y, screen.getZOffset() + 1000);
        }

        if (tooltip.size() == 1) {
            tooltip.add(new TranslatableText("numismatic-overhaul.empty").formatted(Formatting.GRAY));
        }

        screen.renderTooltip(matrices, tooltip, x, y - 15);
    }

    private static void renderStack(MatrixStack matrices, ItemStack stack, List<Text> tooltip, int index, int x, int y, int z) {
        tooltip.add(createPlaceholder(String.valueOf(stack.getCount())));

        ItemStack toRender = stack.copy();
        toRender.setCount(1);

        int localX = x + 8;
        int localY = y - (2 - index) * 10;

        MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(toRender, localX, localY);
    }

    private static Text createPlaceholder(String text) {
        String placeholder = "§7   " + text + " ";
        return Text.of(placeholder);
    }

}
