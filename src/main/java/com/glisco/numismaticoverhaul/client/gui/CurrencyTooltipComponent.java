package com.glisco.numismaticoverhaul.client.gui;

import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.item.CurrencyTooltipData;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ops.ItemOps;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CurrencyTooltipComponent implements TooltipComponent {

    private final CurrencyTooltipData data;
    private final List<Text> text;

    private int widthCache = -1;

    public CurrencyTooltipComponent(CurrencyTooltipData data) {
        this.data = data;
        this.text = new ArrayList<>();

        if (data.original()[0] != -1) {
            CurrencyConverter.getAsItemStackList(data.original()).forEach(stack -> text.add(createPlaceholder(stack.getCount())));
            text.add(Text.of(" "));
        }

        CurrencyConverter.getAsItemStackList(data.value()).forEach(stack -> text.add(createPlaceholder(stack.getCount())));
    }

    @Override
    public int getHeight() {
        return 10 * text.size();
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        if (widthCache == -1) {
            widthCache = textRenderer.getWidth(text.stream()
                    .max(Comparator.comparingInt(textRenderer::getWidth)).orElse(Text.of("")));
        }
        return widthCache;
    }

    @Override
    public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix4f, VertexConsumerProvider.Immediate immediate) {
        for (int i = 0; i < text.size(); i++) {
            textRenderer.draw(text.get(i), x, y + i * 10, -1, true, matrix4f, immediate, false, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        }
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
        List<ItemStack> originalCoins = data.original()[0] != -1 ? CurrencyConverter.getAsItemStackList(data.original()) : new ArrayList<>();
        List<ItemStack> coins = CurrencyConverter.getAsItemStackList(data.value());

        RenderSystem.setShaderTexture(0, new Identifier("textures/gui/container/villager2.png"));
        for (int i = 0; i < originalCoins.size(); i++) {
            DrawableHelper.drawTexture(matrices, x + (originalCoins.get(i).getCount() > 9 ? 14 : 11), y + 3, z, 0, 176, 9, 2, 512, 256);
            itemRenderer.renderGuiItemIcon(ItemOps.singleCopy(originalCoins.get(i)), x - 4, y - 5 + i * 10);
        }

        for (int i = 0; i < coins.size(); i++) {
            itemRenderer.renderGuiItemIcon(ItemOps.singleCopy(coins.get(i)), x - 4, y - 5 + i * 10 + (originalCoins.size() == 0 ? 0 : 10 + originalCoins.size() * 10));
        }
    }

    private static Text createPlaceholder(int count) {
        String placeholder = "§7   " + count + " ";
        return Text.literal(placeholder).formatted(Formatting.GRAY);
    }

}
