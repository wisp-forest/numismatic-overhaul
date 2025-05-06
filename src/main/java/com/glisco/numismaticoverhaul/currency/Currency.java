package com.glisco.numismaticoverhaul.currency;

import com.glisco.numismaticoverhaul.item.NumismaticOverhaulItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;

public enum Currency implements ItemConvertible {
    BRONZE {
        @Override
        public int getNameColor() {
            return 0xae5b3c;
        }

        @Override
        public long getRawValue(long amount) {
            return amount * BRONZE_VALUE;
        }

        @Override
        public Item asItem() {
            return NumismaticOverhaulItems.BRONZE_COIN;
        }
    }, SILVER {
        @Override
        public int getNameColor() {
            return 0x617174;
        }

        @Override
        public long getRawValue(long amount) {
            return amount * SILVER_VALUE;
        }

        @Override
        public Item asItem() {
            return NumismaticOverhaulItems.SILVER_COIN;
        }
    }, GOLD {
        @Override
        public int getNameColor() {
            return 0xbd9838;
        }

        @Override
        public long getRawValue(long amount) {
            return amount * GOLD_VALUE;
        }

        @Override
        public Item asItem() {
            return NumismaticOverhaulItems.GOLD_COIN;
        }
    };

    public abstract int getNameColor();

    public abstract long getRawValue(long amount);

    public static final int GOLD_VALUE = 10000;
    public static final int SILVER_VALUE = 100;
    public static final int BRONZE_VALUE = 1;
}
