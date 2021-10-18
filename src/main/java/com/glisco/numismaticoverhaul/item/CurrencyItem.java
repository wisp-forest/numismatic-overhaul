package com.glisco.numismaticoverhaul.item;

import net.minecraft.item.ItemStack;

public interface CurrencyItem {

    static void setOriginalValue(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt("OriginalValue", value);
    }

    static int getOriginalValue(ItemStack stack) {
        return stack.getOrCreateTag().getInt("OriginalValue");
    }

    static boolean hasOriginalValue(ItemStack stack) {
        return stack.getOrCreateTag().contains("OriginalValue");
    }

    boolean wasAdjusted(ItemStack other);

    int getValue(ItemStack stack);

}
