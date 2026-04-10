package com.glisco.numismaticoverhaul.block.pawn;

import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.ops.ItemOps;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;

public record PawnShopOffer(ItemStack buy, long price) {

    public static final Endec<PawnShopOffer> ENDEC = StructEndecBuilder.of(
        CodecUtils.toEndec(ItemStack.VALIDATED_CODEC).fieldOf("buy", PawnShopOffer::getBuyStack),
        Endec.LONG.fieldOf("price", PawnShopOffer::price),
        PawnShopOffer::new
    );

    public PawnShopOffer {

        if (buy.isEmpty()) throw new IllegalArgumentException("Buy Stack must not be empty");
        if (price == 0) throw new IllegalArgumentException("Price must not be null");

    }

    public static void add(PawnShopBlockEntity pawnShop, ItemStack boughtStack) {
        var shopItems = pawnShop.getItems();
        // loop once to merge existing stacks together
        for (ItemStack stack : shopItems) {
            if (ItemOps.canStack(stack, boughtStack)) {
                stack.increment(boughtStack.getCount());
                return;
            }
        }
        // then again to fill an empty slot
        for (int i = 0; i < shopItems.size(); i++) {
            if (shopItems.get(i).isEmpty()) {
                pawnShop.setStack(i, boughtStack);
                return;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public TradeOffer toTradeOffer(PawnShopBlockEntity shop, boolean inexhaustible) {
        int maxUses = inexhaustible ? Integer.MAX_VALUE : (int) (shop.getStoredCurrency() / price);
        if (!shop.getItems().contains(ItemStack.EMPTY)) {
            maxUses = 0;
        }
        var money = CurrencyConverter.getRequiredCurrencyTypes(price) == 1 ? CurrencyConverter.getAsItemStackList(price).getFirst() : MoneyBagItem.fromRawValue(price);

        var buyItem = new TradedItem(buy.getItem(), buy.getCount());
        return new TradeOffer(buyItem, money, maxUses, 0, 0);
    }

    public ItemStack getBuyStack() {
        return buy.copy();
    }

    public static int count(DefaultedList<ItemStack> stacks, ItemStack testStack) {
        int count = 0;
        for (var stack : stacks) {
            if (!ItemStack.areItemsAndComponentsEqual(stack, testStack)) continue;
            count += stack.getCount();
        }
        return count;
    }

    @Override
    public String toString() {
        return this.buy + "@" + this.price + "coins";
    }
}
