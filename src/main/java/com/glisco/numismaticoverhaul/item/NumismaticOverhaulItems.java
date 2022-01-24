package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.currency.Currency;
import io.wispforest.owo.registration.reflect.ItemRegistryContainer;

public class NumismaticOverhaulItems implements ItemRegistryContainer {
    public static final CoinItem BRONZE_COIN = new CoinItem(Currency.BRONZE);
    public static final CoinItem SILVER_COIN = new CoinItem(Currency.SILVER);
    public static final CoinItem GOLD_COIN = new CoinItem(Currency.GOLD);
    public static final MoneyBagItem MONEY_BAG = new MoneyBagItem();
}
