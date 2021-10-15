package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.client.gui.CurrencyTooltipRenderer;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends Screen {

    private MerchantScreenMixin(Text title) {
        super(title);
    }

    private ItemStack numismatic$firstBuyItem;

    @Inject(method = "renderFirstBuyItem", at = @At("HEAD"))
    private void captureFirstBuyItem(MatrixStack matrices, ItemStack adjustedFirstBuyItem, ItemStack originalFirstBuyItem, int x, int y, CallbackInfo ci) {
        this.numismatic$firstBuyItem = adjustedFirstBuyItem;
    }

    @ModifyVariable(method = "renderFirstBuyItem", at= @At("HEAD"), argsOnly = true, ordinal = 1)
    private ItemStack dontShowBagDiscount(ItemStack original) {
        if (numismatic$firstBuyItem.isOf(NumismaticOverhaul.MONEY_BAG)) {
            var copy = this.numismatic$firstBuyItem;
            this.numismatic$firstBuyItem = null;
            return copy;
        } else {
            this.numismatic$firstBuyItem = null;
            return original;
        }
    }

    @Override
    protected void renderTooltip(MatrixStack matrices, ItemStack stack, int x, int y) {
        if (stack.getItem().equals(NumismaticOverhaul.MONEY_BAG)) {
            if (MoneyBagItem.hasBefore(stack)) {
                CurrencyTooltipRenderer.renderTooltip(new CurrencyStack(MoneyBagItem.getValue(stack)), new CurrencyStack(MoneyBagItem.getBefore(stack)), matrices, this, new TranslatableText("item.numismatic-overhaul.money_bag").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(CurrencyResolver.Currency.SILVER.getNameColor()))), x, y);
            } else {
                CurrencyTooltipRenderer.renderTooltip(new CurrencyStack(MoneyBagItem.getValue(stack)), matrices, this, new TranslatableText("item.numismatic-overhaul.money_bag").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(CurrencyResolver.Currency.SILVER.getNameColor()))), x, y);
            }
            return;
        }

        super.renderTooltip(matrices, stack, x, y);
    }
}
