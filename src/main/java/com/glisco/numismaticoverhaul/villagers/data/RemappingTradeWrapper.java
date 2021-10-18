package com.glisco.numismaticoverhaul.villagers.data;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

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
        final var secondBuyRemapped = remap(tempOffer.getSecondBuyItem());
        final var sellRemapped = remap(tempOffer.getSellItem());

        return new TradeOffer(firstBuyRemapped, secondBuyRemapped, sellRemapped, tempOffer.getUses(), tempOffer.getMaxUses(), tempOffer.getMerchantExperience(), tempOffer.getPriceMultiplier(), tempOffer.getDemandBonus());
    }

    private static ItemStack remap(ItemStack stack) {
        if (stack.getItem() != Items.EMERALD) return stack;

        final int moneyWorth = stack.getCount() * 69;

        return CurrencyHelper.getClosest(moneyWorth);
    }
}
