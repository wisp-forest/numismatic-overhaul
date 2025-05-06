package com.glisco.numismaticoverhaul.currency;

import static com.glisco.numismaticoverhaul.currency.Currency.*;
import static com.glisco.numismaticoverhaul.currency.Currency.GOLD;

public class CurrencyResolver {

    /**
     * Splits the given raw value into the individual currencies, MSD first
     *
     * @param rawValue The value to split
     * @return The individual currency values in the format int[]{BRONZE, SILVER, GOLD}
     */
    public static long[] splitValues(long rawValue) {
        long[] output = new long[]{0, 0, 0};

        if (rawValue / GOLD_VALUE != 0) {
            output[2] = rawValue / GOLD_VALUE;
            rawValue -= GOLD_VALUE * output[2];
        }

        if (rawValue / SILVER_VALUE != 0) {
            output[1] = rawValue / SILVER_VALUE;
            rawValue -= SILVER_VALUE * output[1];
        }

        if (rawValue > 0) {
            output[0] = rawValue;
        }

        return output;
    }

    /**
     * Combines the given values in a raw currency value
     *
     * @param values The individual currency values in the format int[]{BRONZE, SILVER, GOLD}
     * @return The raw value, with respect to each value's worth
     */
    public static long combineValues(long[] values) {
        if (values.length != 3) throw new IllegalArgumentException("Input array has to have 3 elements");

        return BRONZE.getRawValue(values[0]) + SILVER.getRawValue(values[1]) + GOLD.getRawValue(values[2]);
    }

    public static long combineValues(long bronze, long silver, long gold) {
        return BRONZE.getRawValue(bronze) + SILVER.getRawValue(silver) + GOLD.getRawValue(gold);
    }

    public static boolean canBeCompacted(long[] values) {
        return values[0] < 100 && values[1] < 100 && values[2] < 100;
    }

}
