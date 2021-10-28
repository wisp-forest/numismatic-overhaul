package com.glisco.numismaticoverhaul.currency;

public class CurrencyResolver {

    /**
     * Splits the given raw value into the individual currencies, MSD first
     *
     * @param rawValue The value to split
     * @return The individual currency values in the format int[]{BRONZE, SILVER, GOLD}
     */
    public static int[] splitValues(int rawValue) {
        int[] output = new int[]{0, 0, 0};

        if (rawValue / 10000 != 0) {
            output[2] = rawValue / 10000;
            rawValue -= 10000 * output[2];
        }

        if (rawValue / 100 != 0) {
            output[1] = rawValue / 100;
            rawValue -= 100 * output[1];
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
    public static int combineValues(int[] values) {
        if (values.length != 3) throw new IllegalArgumentException("Input array has to have 3 elements");

        return values[0] + values[1] * 100 + values[2] * 10000;
    }

}
