package com.glisco.numismaticoverhaul.item;

import net.minecraft.client.item.TooltipData;

public record CurrencyTooltipData(int value, int original) implements TooltipData {}
