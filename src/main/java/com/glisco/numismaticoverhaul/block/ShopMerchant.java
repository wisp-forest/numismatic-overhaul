package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ShopMerchant implements Merchant {

    private final ShopBlockEntity shop;
    private final boolean inexhaustible;
    private TradeOfferList recipeList = new TradeOfferList();
    private PlayerEntity customer;

    public ShopMerchant(ShopBlockEntity blockEntity, boolean inexhaustible) {
        this.shop = blockEntity;
        this.inexhaustible = inexhaustible;
    }

    public void updateTrades() {
        recipeList.clear();
        shop.getOffers().forEach(offer -> recipeList.add(offer.toTradeOffer(shop, this.inexhaustible)));
    }

    @Override
    public void setCustomer(@Nullable PlayerEntity customer) {
        this.customer = customer;
    }

    @Nullable
    @Override
    public PlayerEntity getCustomer() {
        return customer;
    }

    @Override
    public TradeOfferList getOffers() {
        return recipeList;
    }

    @Override
    public void setOffersFromServer(@Nullable TradeOfferList offers) {
        this.recipeList = offers;
    }

    @Override
    public void trade(TradeOffer offer) {
        offer.use();
        if (!this.inexhaustible) ShopOffer.remove(shop.getItems(), offer.getSellItem());
        shop.addCurrency(CurrencyHelper.getValue(Arrays.asList(offer.getOriginalFirstBuyItem(), offer.getSecondBuyItem())));
    }

    @Override
    public void onSellingItem(ItemStack stack) {

    }

    @Override
    public int getExperience() {
        return 0;
    }

    @Override
    public void setExperienceFromServer(int experience) {

    }

    @Override
    public boolean isLeveledMerchant() {
        return false;
    }

    @Override
    public SoundEvent getYesSound() {
        return SoundEvents.ENTITY_VILLAGER_YES;
    }

    @Override
    public boolean isClient() {
        return false;
    }
}
