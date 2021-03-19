package com.glisco.numismaticoverhaul.currency;

public class CurrencyResolver {

    public static int[] getValues(int rawValue) {
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

    public static int getRawValue(int[] values) {
        if (values.length != 3) throw new IllegalArgumentException("Input array has to have 3 elements");

        return values[0] + values[1] * 100 + values[2] * 10000;
    }

    public enum Currency {
        BRONZE {
            @Override
            public int getNameColor() {
                return 0xae5b3c;
            }

            @Override
            public int getRawValue(int amount) {
                return amount;
            }
        }, SILVER {
            @Override
            public int getNameColor() {
                return 0x617174;
            }

            @Override
            public int getRawValue(int amount) {
                return amount * 100;
            }
        }, GOLD {
            @Override
            public int getNameColor() {
                return 0xbd9838;
            }

            @Override
            public int getRawValue(int amount) {
                return amount * 10000;
            }
        };

        public abstract int getNameColor();

        public abstract int getRawValue(int amount);
    }
}
