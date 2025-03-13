package com.glisco.numismaticoverhaul.item;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public interface CurrencyItem {

    KeyedEndec<Long> ORIGINAL_VALUE = new KeyedEndec<>("OriginalValue", Endec.LONG, 0L);

    static void setOriginalValue(ItemStack stack, long value) {
        NbtCompound nbt = new NbtCompound();
        nbt.put(ORIGINAL_VALUE, value);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    static long getOriginalValue(ItemStack stack) {
        NbtCompound nbt = new NbtCompound();
        nbt.put(ORIGINAL_VALUE, ORIGINAL_VALUE.defaultValue());
        nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt)).copyNbt();
        return nbt.get(ORIGINAL_VALUE);
    }

    static boolean hasOriginalValue(ItemStack stack) {
        NbtCompound nbt = new NbtCompound();
        nbt.put(ORIGINAL_VALUE, ORIGINAL_VALUE.defaultValue());
        nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt)).copyNbt();
        return nbt.has(ORIGINAL_VALUE);
    }

    boolean wasAdjusted(ItemStack other);

    long getValue(ItemStack stack);

    long[] getCombinedValue(ItemStack stack);

}
