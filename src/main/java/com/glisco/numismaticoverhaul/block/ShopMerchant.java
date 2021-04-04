package com.glisco.numismaticoverhaul.block;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShopMerchant implements Merchant {

    ShopBlockEntity shop;
    private TradeOfferList recipeList = new TradeOfferList();
    private PlayerEntity customer;

    public ShopMerchant(ShopBlockEntity blockEntity) {
        this.shop = blockEntity;
    }

    public void updateTrades() {
        recipeList.clear();
        shop.getOffers().forEach(offer -> recipeList.add(offer.toTradeOffer(shop)));
    }

    @Override
    public void setCurrentCustomer(@Nullable PlayerEntity customer) {
        this.customer = customer;
    }

    @Nullable
    @Override
    public PlayerEntity getCurrentCustomer() {
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

        System.out.println("Selling: " + offer.getSellItem());

        shop.getItems().forEach(itemStack -> {
            ItemStack comparisonStack = itemStack.copy();
            comparisonStack.setCount(1);
            if (!ItemStack.areEqual(comparisonStack, offer.getSellItem())) return;
            itemStack.decrement(1);
        });
    }

    @Override
    public void onSellingItem(ItemStack stack) {

    }

    @Override
    public World getMerchantWorld() {
        return shop.getWorld();
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
}
