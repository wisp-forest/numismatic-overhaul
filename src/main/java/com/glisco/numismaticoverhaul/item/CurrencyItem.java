package com.glisco.numismaticoverhaul.item;

import net.minecraft.item.ItemStack;

public interface CurrencyItem {

    static void setOriginalValue(ItemStack stack, long value) {
        stack.getOrCreateNbt().putLong("OriginalValue", value);
    }

    static long getOriginalValue(ItemStack stack) {
        return stack.getOrCreateNbt().getLong("OriginalValue");
    }

    static boolean hasOriginalValue(ItemStack stack) {
        return stack.getOrCreateNbt().contains("OriginalValue");
    }

    boolean wasAdjusted(ItemStack other);

    long getValue(ItemStack stack);

    long[] getCombinedValue(ItemStack stack);

}
