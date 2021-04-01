package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
        shop.getItems().forEach(itemStack -> {
            if (itemStack.getItem() == Items.AIR) return;

            ItemStack toSell = itemStack.copy();
            toSell.setCount(1);
            recipeList.add(new TradeOffer(new ItemStack(NumismaticOverhaul.BRONZE_COIN), toSell, itemStack.getCount(), 0, 0));
        });
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
            if (itemStack.getItem() != offer.getSellItem().getItem()) return;

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
