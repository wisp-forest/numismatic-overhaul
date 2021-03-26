package com.glisco.numismaticoverhaul.currency;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents an amount of currency stored somewhere.
 * <p>
 * Provides convenience methods for converting to {@link ItemStack}
 */
public class CurrencyStack {

    private int value;

    public CurrencyStack(int value) {
        this.value = value;
    }

    public int getRawValue() {
        return value;
    }

    public void setRawValue(int value) {
        this.value = value;
    }

    /**
     * @return An array of 3 {@link ItemStack}, format ItemStack[]{BRONZE, SILVER, GOLD}, stack sizes can exceed 99
     */
    public ItemStack[] getAsItemStackArray() {
        ItemStack[] output = new ItemStack[]{null, null, null};

        int[] values = CurrencyResolver.splitValues(value);

        output[2] = new ItemStack(NumismaticOverhaul.GOLD_COIN, values[2]);
        output[1] = new ItemStack(NumismaticOverhaul.SILVER_COIN, values[1]);
        output[0] = new ItemStack(NumismaticOverhaul.BRONZE_COIN, values[0]);

        return output;
    }

    /**
     * Wrapper for {@link CurrencyStack#getAsItemStackArray()} that only includes non-zero {@link ItemStack}
     *
     * @return A list of {@link ItemStack}, stack sizes can exceed 99
     */
    public List<ItemStack> getAsItemStackList() {
        List<ItemStack> list = new ArrayList<>();

        Arrays.stream(getAsItemStackArray()).forEach(itemStack -> {
            if (itemStack.getCount() != 0) list.add(0, itemStack);
        });

        return list;
    }

    /**
     * @return The amount of currency types required to represent this stack's raw value
     */
    public int getRequiredCurrencyTypes() {
        return splitAtMaxCount(getAsItemStackList()).size();
    }

    public static CurrencyStack fromJson(JsonObject data) {
        int[] values = new int[]{0, 0, 0};
        values[0] = data.has("bronze") ? data.get("bronze").getAsInt() : 0;
        values[1] = data.has("silver") ? data.get("silver").getAsInt() : 0;
        values[2] = data.has("gold") ? data.get("gold").getAsInt() : 0;
        return new CurrencyStack(CurrencyResolver.combineValues(values));
    }

    /**
     * Splits the provided list into another list where no stacks are over their max size
     *
     * @param input A list of {@link ItemStack} that could contain some with illegal sizes
     * @return A list where no stacks have illegal sizes, most likely bigger than input
     */
    public static List<ItemStack> splitAtMaxCount(List<ItemStack> input) {
        List<ItemStack> output = new ArrayList<>();

        for (ItemStack stack : input) {
            if (stack.getCount() <= stack.getMaxCount()) {
                output.add(stack);
            } else {
                for (int i = 0; i < stack.getCount() / stack.getMaxCount(); i++) {
                    ItemStack copy = stack.copy();
                    copy.setCount(stack.getMaxCount());
                    output.add(copy);
                }

                ItemStack copy = stack.copy();
                copy.setCount(stack.getCount() % stack.getMaxCount());
                output.add(copy);
            }
        }

        return output;
    }
}
