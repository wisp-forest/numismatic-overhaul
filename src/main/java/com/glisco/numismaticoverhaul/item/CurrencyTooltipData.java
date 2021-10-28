package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import net.minecraft.client.item.TooltipData;

public final class CurrencyTooltipData implements TooltipData {

    private final int[] value;
    private final int[] original;

    public CurrencyTooltipData(int[] value, int[] original) {
        this.value = value;
        this.original = original;
    }

    public CurrencyTooltipData(int value, int original) {
        this.value = CurrencyResolver.splitValues(value);
        if (original == -1) {
            this.original = new int[]{-1};
        } else {
            this.original = CurrencyResolver.splitValues(original);
        }
    }

    public int[] original() {
        return original;
    }

    public int[] value() {
        return value;
    }
}
