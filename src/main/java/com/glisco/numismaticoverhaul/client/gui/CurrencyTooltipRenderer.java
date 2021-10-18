package com.glisco.numismaticoverhaul.client.gui;

import com.glisco.numismaticoverhaul.currency.CurrencyStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurrencyTooltipRenderer {

    public static void renderTooltip(CurrencyStack stack, MatrixStack matrices, Screen screen, Text title, int x, int y) {
        renderTooltip(stack, null, matrices, screen, title, x, y);
    }

    public static void renderTooltip(CurrencyStack stack, @Nullable CurrencyStack before, MatrixStack matrices, Screen screen, Text title, int x, int y) {

        List<Text> tooltip = new ArrayList<>();
        tooltip.add(title);

        y += 10;

        List<ItemStack> coinsBefore = before != null ? before.getAsItemStackList() : Collections.emptyList();
        List<ItemStack> coins = stack.getAsItemStackList();

        MinecraftClient.getInstance().getItemRenderer().zOffset = 700.0f;

        for (int i = 0; i < coinsBefore.size(); i++) {
            renderStack(matrices, coinsBefore.get(i), tooltip, i, x, y, screen.getZOffset() + 1000, true);
        }

        if (!coinsBefore.isEmpty()) tooltip.add(Text.of(" "));

        for (int i = 0; i < coins.size(); i++) {
            renderStack(matrices, coins.get(i), tooltip, i + (coinsBefore.isEmpty() ? 0 : coinsBefore.size() + 1), x, y, screen.getZOffset() + 1000, false);
        }

        if (tooltip.size() == 1) {
            tooltip.add(new TranslatableText("numismatic-overhaul.empty").formatted(Formatting.GRAY));
        }

        screen.renderTooltip(matrices, tooltip, x, y - 15);
    }

    private static void renderStack(MatrixStack matrices, ItemStack stack, List<Text> tooltip, int index, int x, int y, int z, boolean strikeThrough) {
        tooltip.add(createPlaceholder(String.valueOf(stack.getCount())));

        ItemStack toRender = stack.copy();
        toRender.setCount(1);

        int localX = x + 8;
        int localY = y - (2 - index) * 10;

        MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(toRender, localX, localY);

        if (!strikeThrough) return;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, new Identifier("textures/gui/container/villager2.png"));

        DrawableHelper.drawTexture(matrices, localX + (stack.getCount() > 9 ? 17 : 14), localY + 8, z, 0, 176, 9, 2, 256, 512);
    }

    private static Text createPlaceholder(String text) {
        String placeholder = "§7   " + text + " ";
        return Text.of(placeholder);
    }

}
