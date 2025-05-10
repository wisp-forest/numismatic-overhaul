package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.village.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

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
        this.shop.busy = customer != null;
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
        if (!this.inexhaustible) {
            ShopOffer.remove(shop.getItems(), offer.getSellItem());

            this.updateTrades();
            if (this.getCustomer() instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.networkHandler.sendPacket(new SetTradeOffersS2CPacket(
                        serverPlayer.currentScreenHandler.syncId,
                        this.recipeList,
                        0, 0, false, false
                ));
            }
        }

        var items = new ArrayList<ItemStack>();
        items.add(offer.getOriginalFirstBuyItem());
        if (offer.getSecondBuyItem().isPresent()) {
            items.add(offer.getSecondBuyItem().get().itemStack());
        }
        shop.addCurrency(CurrencyHelper.getValue(items));
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

    public ShopBlockEntity shop() {
        return this.shop;
    }
}
