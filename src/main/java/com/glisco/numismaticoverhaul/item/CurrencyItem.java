package com.glisco.numismaticoverhaul.item;

import io.wispforest.owo.nbt.NbtKey;
import net.minecraft.item.ItemStack;

public interface CurrencyItem {

    NbtKey<Long> ORIGINAL_VALUE = new NbtKey<>("OriginalValue", NbtKey.Type.LONG);

    static void setOriginalValue(ItemStack stack, long value) {
        stack.put(ORIGINAL_VALUE, value);
    }

    static long getOriginalValue(ItemStack stack) {
        return stack.get(ORIGINAL_VALUE);
    }

    static boolean hasOriginalValue(ItemStack stack) {
        return stack.has(ORIGINAL_VALUE);
    }

    boolean wasAdjusted(ItemStack other);

    long getValue(ItemStack stack);

    long[] getCombinedValue(ItemStack stack);

}
