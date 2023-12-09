package com.glisco.numismaticoverhaul.client.gui;

import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.item.CurrencyTooltipData;
import io.wispforest.owo.ops.ItemOps;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

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
            CurrencyConverter.getAsItemStackList(data.original()).forEach(stack -> text.add(Text.literal(String.valueOf(stack.getCount())).formatted(Formatting.GRAY)));
            text.add(Text.of(" "));
        }

        CurrencyConverter.getAsItemStackList(data.value()).forEach(stack -> text.add(Text.literal(String.valueOf(stack.getCount())).formatted(Formatting.GRAY)));
    }

    @Override
    public int getHeight() {
        return 10 * text.size();
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        if (widthCache == -1) {
            widthCache = textRenderer.getWidth(text.stream()
                    .max(Comparator.comparingInt(textRenderer::getWidth)).orElse(Text.of(""))) + 10;
        }
        return widthCache;
    }

    @Override
    public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix4f, VertexConsumerProvider.Immediate immediate) {
        for (int i = 0; i < text.size(); i++) {
            textRenderer.draw(text.get(i), x + 10, y + i * 10, -1, true, matrix4f, immediate, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        }
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        List<ItemStack> originalCoins = data.original()[0] != -1 ? CurrencyConverter.getAsItemStackList(data.original()) : new ArrayList<>();
        List<ItemStack> coins = CurrencyConverter.getAsItemStackList(data.value());

        context.push().translate(0, 0, 50);

        for (int i = 0; i < originalCoins.size(); i++) {
            context.drawGuiTexture(new Identifier("container/villager/discount_strikethrough"), x + (originalCoins.get(i).getCount() > 9 ? 14 : 11), y + 3, 9, 2);
            context.drawItemWithoutEntity(ItemOps.singleCopy(originalCoins.get(i)), x - 4, y - 5 + i * 10);
        }

        for (int i = 0; i < coins.size(); i++) {
            context.drawItemWithoutEntity(ItemOps.singleCopy(coins.get(i)), x - 4, y - 5 + i * 10 + (originalCoins.size() == 0 ? 0 : 10 + originalCoins.size() * 10));
        }

        context.pop();
    }

}
