package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.client.gui.CurrencyTooltipRenderer;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.item.CurrencyItem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V", at = @At("HEAD"), cancellable = true)
    private void renderTooltip(MatrixStack matrices, ItemStack stack, int x, int y, CallbackInfo ci) {
//        var screen = (Screen) (Object) this;
//
//        if (!(stack.getItem() instanceof CurrencyItem currencyItem)) return;
//
//        if (CurrencyItem.hasOriginalValue(stack)) {
//            ci.cancel();
//            CurrencyTooltipRenderer.renderTooltip(currencyItem.getValue(stack), CurrencyItem.getOriginalValue(stack), matrices, screen, stack.getName(), x, y);
//        } else if (currencyItem == NumismaticOverhaul.MONEY_BAG) {
//            ci.cancel();
//            CurrencyTooltipRenderer.renderTooltip(currencyItem.getValue(stack), matrices, screen, new TranslatableText(stack.getTranslationKey()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(CurrencyResolver.Currency.SILVER.getNameColor()))), x, y);
//        }
    }

}
