package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.currency.CurrencyStack;
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

    public static void renderTooltip(CurrencyStack stack, MatrixStack matrices, Screen screen, Text title, int x, int y) {

        List<Text> tooltip = new ArrayList<>();
        tooltip.add(title);

        y += 10;

        List<ItemStack> coins = stack.getAsItemStackList();
        MinecraftClient.getInstance().getItemRenderer().zOffset = 700.0f;

        for (int i = 0; i < coins.size(); i++) {
            tooltip.add(createPlaceholder(String.valueOf(coins.get(i).getCount())));

            ItemStack toRender = coins.get(i).copy();
            toRender.setCount(1);

            MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(toRender, x + 8, y - (2 - i) * 10);
        }

        if (tooltip.size() == 1) {
            tooltip.add(new TranslatableText("numismatic-overhaul.empty").formatted(Formatting.GRAY));
        }

        screen.renderTooltip(matrices, tooltip, x, y - 15);
    }

    private static Text createPlaceholder(String text) {
        String placeholder = "§7   " + text + " ";
        return Text.of(placeholder);
    }

}
