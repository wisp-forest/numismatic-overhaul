package com.glisco.numismaticoverhaul.item;

import net.minecraft.item.ItemStack;

public interface CurrencyItem {

    static void setOriginalValue(ItemStack stack, int value) {
        stack.getOrCreateNbt().putInt("OriginalValue", value);
    }

    static int getOriginalValue(ItemStack stack) {
        return stack.getOrCreateNbt().getInt("OriginalValue");
    }

    static boolean hasOriginalValue(ItemStack stack) {
        return stack.getOrCreateNbt().contains("OriginalValue");
    }

    boolean wasAdjusted(ItemStack other);

    int getValue(ItemStack stack);

    int[] getCombinedValue(ItemStack stack);

}
