package com.glisco.numismaticoverhaul.villagers.data;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.*;
import org.jetbrains.annotations.Nullable;

public class RemappingTradeWrapper implements TradeOffers.Factory {

    private final TradeOffers.Factory delegate;

    private RemappingTradeWrapper(TradeOffers.Factory delegate) {
        this.delegate = delegate;
    }

    public static RemappingTradeWrapper wrap(TradeOffers.Factory delegate) {
        return new RemappingTradeWrapper(delegate);
    }

    @Nullable
    @Override
    public TradeOffer create(Entity entity, Random random) {
        final var tempOffer = delegate.create(entity, random);

        if (tempOffer == null) return null;

        final var firstBuyRemapped = remap(tempOffer.getOriginalFirstBuyItem());
        final var secondBuyRemapped = tempOffer.getSecondBuyItem().map(RemappingTradeWrapper::remap);
        final var sellRemapped = remap(tempOffer.getSellItem());

        return new TradeOffer(firstBuyRemapped, secondBuyRemapped, sellRemapped.itemStack(), tempOffer.getUses(), tempOffer.getMaxUses(), tempOffer.getMerchantExperience(), tempOffer.getPriceMultiplier(), tempOffer.getDemandBonus());
    }

    private static TradedItem remap(TradedItem tradedItem) {
        if (!tradedItem.itemStack().isOf(Items.EMERALD)) {
            return tradedItem;
        }

        return CurrencyHelper.getClosestTradeItem(convertEmeraldsToCoins(tradedItem.count()));
    }

    private static TradedItem remap(ItemStack stack) {
        if (stack.getItem() != Items.EMERALD) {
            return new TradedItem(stack.getItem(), stack.getCount());
        }

        final int moneyWorth = stack.getCount() * 125;

        return CurrencyHelper.getClosestTradeItem(convertEmeraldsToCoins(moneyWorth));
    }

    private static long convertEmeraldsToCoins(int stackCount) {
        return (long) stackCount * 125;
    }
}
