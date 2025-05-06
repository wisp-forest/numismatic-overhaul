package com.glisco.numismaticoverhaul.item;

import net.minecraft.item.ItemStack;

public interface CurrencyItem {

    boolean wasAdjusted(ItemStack other);

    long getValue(ItemStack stack);

    long[] getCombinedValue(ItemStack stack);

}
