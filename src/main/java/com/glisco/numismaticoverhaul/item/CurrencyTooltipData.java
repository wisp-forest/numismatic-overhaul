package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import net.minecraft.item.tooltip.TooltipData;

public final class CurrencyTooltipData implements TooltipData {

    private final long[] value;
    private final long[] original;

    // TODO - Working with raw long arrays sucks. Migrate this to use CurrencyComponent or similar
    public CurrencyTooltipData(long[] value, long[] original) {
        this.value = value;
        this.original = original;
    }

    public CurrencyTooltipData(long value, long original) {
        this.value = CurrencyResolver.splitValues(value);
        if (original == -1) {
            this.original = new long[]{-1};
        } else {
            this.original = CurrencyResolver.splitValues(original);
        }
    }

    public long[] original() {
        return original;
    }

    public long[] value() {
        return value;
    }
}
