package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.client.CurrencyTooltipRenderer;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.currency.CurrencyStack;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends Screen {

    private MerchantScreenMixin(Text title) {
        super(title);
    }

    @Override
    protected void renderTooltip(MatrixStack matrices, ItemStack stack, int x, int y) {
        if (stack.getItem().equals(NumismaticOverhaul.MONEY_BAG)) {
            CurrencyTooltipRenderer.renderTooltip(new CurrencyStack(MoneyBagItem.getValue(stack)), matrices, this, new TranslatableText("item.numismatic-overhaul.money_bag").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(CurrencyResolver.Currency.SILVER.getNameColor()))), x, y);
            return;
        }

        super.renderTooltip(matrices, stack, x, y);
    }
}
