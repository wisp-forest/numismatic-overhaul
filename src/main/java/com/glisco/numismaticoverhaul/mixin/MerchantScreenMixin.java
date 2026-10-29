package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.item.CurrencyItem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends Screen {

    private MerchantScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private ItemStack numismatic$adjustedFirstBuyItem;
    @Unique
    private ItemStack numismatic$originalFirstBuyItem;

    @Inject(method = "renderFirstBuyItem", at = @At("HEAD"))
    private void captureFirstBuyItem(DrawContext context, ItemStack adjustedFirstBuyItem, ItemStack originalFirstBuyItem, int x, int y, CallbackInfo ci) {
        this.numismatic$originalFirstBuyItem = originalFirstBuyItem;
        this.numismatic$adjustedFirstBuyItem = adjustedFirstBuyItem;
    }

    @ModifyVariable(method = "renderFirstBuyItem", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private ItemStack dontShowBagDiscount(ItemStack original) {
        if (this.numismatic$adjustedFirstBuyItem == null) {
            return original;
        }

        try {
            var adjustedItem = this.numismatic$adjustedFirstBuyItem.getItem();
            if (adjustedItem instanceof CurrencyItem adjustable
                    && this.numismatic$originalFirstBuyItem != null
                    && adjustable.wasAdjusted(this.numismatic$originalFirstBuyItem)) {
                return this.numismatic$adjustedFirstBuyItem;
            } else {
                return original;
            }
        } finally {
            this.numismatic$adjustedFirstBuyItem = null;
            this.numismatic$originalFirstBuyItem = null;
        }
    }
}
