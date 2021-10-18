package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.client.gui.CurrencyTooltipRenderer;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.item.CurrencyItem;
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

    private ItemStack numismatic$adjustedFirstBuyItem;
    private ItemStack numismatic$originalFirstBuyItem;

    @Inject(method = "renderFirstBuyItem", at = @At("HEAD"))
    private void captureFirstBuyItem(MatrixStack matrices, ItemStack adjustedFirstBuyItem, ItemStack originalFirstBuyItem, int x, int y, CallbackInfo ci) {
        this.numismatic$originalFirstBuyItem = originalFirstBuyItem;
        this.numismatic$adjustedFirstBuyItem = adjustedFirstBuyItem;
    }

    @ModifyVariable(method = "renderFirstBuyItem", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private ItemStack dontShowBagDiscount(ItemStack original) {
        var adjustedItem = numismatic$adjustedFirstBuyItem.getItem();

        if (adjustedItem instanceof CurrencyItem adjustable && adjustable.wasAdjusted(numismatic$originalFirstBuyItem)) {
            var copy = this.numismatic$adjustedFirstBuyItem;
            this.numismatic$adjustedFirstBuyItem = null;
            return copy;
        } else {
            this.numismatic$adjustedFirstBuyItem = null;
            return original;
        }
    }

    @Override
    protected void renderTooltip(MatrixStack matrices, ItemStack stack, int x, int y) {
        if (!(stack.getItem() instanceof CurrencyItem currencyItem)) {
            super.renderTooltip(matrices, stack, x, y);
            return;
        }

        if (CurrencyItem.hasOriginalValue(stack)) {
            CurrencyTooltipRenderer.renderTooltip(currencyItem.getValue(stack), CurrencyItem.getOriginalValue(stack), matrices, this, new TranslatableText(stack.getTranslationKey()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(CurrencyResolver.Currency.SILVER.getNameColor()))), x, y);
        } else if (currencyItem == NumismaticOverhaul.MONEY_BAG) {
            CurrencyTooltipRenderer.renderTooltip(currencyItem.getValue(stack), matrices, this, new TranslatableText(stack.getTranslationKey()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(CurrencyResolver.Currency.SILVER.getNameColor()))), x, y);
        } else {
            super.renderTooltip(matrices, stack, x, y);
        }
    }
}
