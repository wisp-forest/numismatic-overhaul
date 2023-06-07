package com.glisco.numismaticoverhaul.client.gui;

import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class CurrencyTooltipRenderer {

    public static void renderTooltip(long value, DrawContext context, Text title, int x, int y) {

        List<Text> tooltip = new ArrayList<>();
        tooltip.add(title);

        y += 10;

        List<ItemStack> coins = CurrencyConverter.getAsItemStackList(value);

        // Push the item stack renderers forward, so they do not get obscured by the tooltip background
        context.getMatrices().translate(0, 0, 500);

        for (int i = 0; i < coins.size(); i++) {
            renderStack(context, coins.get(i), tooltip, i, x, y);
        }

        context.getMatrices().translate(0, 0, -500);

        if (tooltip.size() == 1) {
            tooltip.add(Text.translatable("numismatic-overhaul.empty").formatted(Formatting.GRAY));
        }


        context.drawTooltip(MinecraftClient.getInstance().textRenderer, tooltip, x, y - 15);
    }

    private static void renderStack(DrawContext context, ItemStack stack, List<Text> tooltip, int index, int x, int y) {
        tooltip.add(createPlaceholder(String.valueOf(stack.getCount())));

        ItemStack toRender = stack.copy();
        toRender.setCount(1);

        int localX = x + 8;
        int localY = y - (2 - index) * 10;

        context.drawItemWithoutEntity(toRender, localX, localY);
    }

    private static Text createPlaceholder(String text) {
        String placeholder = "§7   " + text + " ";
        return Text.of(placeholder);
    }

}
