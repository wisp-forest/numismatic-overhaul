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
            return amount;
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
            return amount * 100;
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
            return amount * 10000;
        }

        @Override
        public Item asItem() {
            return NumismaticOverhaulItems.GOLD_COIN;
        }
    };

    public abstract int getNameColor();

    public abstract long getRawValue(long amount);
}
