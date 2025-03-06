package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.glisco.numismaticoverhaul.villagers.data.NumismaticTradeOfferExtensions;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.ComponentPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;

public class ShopOffer {
    public static final Endec<ShopOffer> ENDEC = StructEndecBuilder.of(
        MinecraftEndecs.ITEM_STACK.fieldOf("sell", ShopOffer::getSellStack),
        Endec.LONG.fieldOf("price", ShopOffer::getPrice),
        ShopOffer::new
    );

    private final ItemStack sell;
    private final long price;

    public ShopOffer(ItemStack sell, long price) {

        if (sell.isEmpty()) throw new IllegalArgumentException("Sell Stack must not be empty");
        if (price == 0) throw new IllegalArgumentException("Price must not be null");

        this.sell = sell;
        this.price = price;
    }

    @SuppressWarnings("ConstantConditions")
    public TradeOffer toTradeOffer(ShopBlockEntity shop, boolean inexhaustible) {
        boolean isPocketChange = CurrencyConverter.getRequiredCurrencyTypes(price) == 1;
        var buyStack = isPocketChange ? CurrencyConverter.getAsItemStackList(price).get(0) : MoneyBagItem.create(price);
        int maxUses = inexhaustible ? Integer.MAX_VALUE : count(shop.getItems(), sell) / sell.getCount();
        var tradedItem = isPocketChange ? new TradedItem(buyStack.getItem(), (int) price) : new TradedItem(Registries.ITEM.getEntry(buyStack.getItem()), 1, ComponentPredicate.EMPTY, buyStack);

        final var tradeOffer = new TradeOffer(tradedItem, sell, maxUses, 0, 0);
        ((NumismaticTradeOfferExtensions) tradeOffer).numismatic$setReputation(-69420);
        return tradeOffer;
    }

    public long getPrice() {
        return price;
    }

    public ItemStack getSellStack() {
        return sell.copy();
    }

    public static int count(DefaultedList<ItemStack> stacks, ItemStack testStack) {
        int count = 0;
        for (var stack : stacks) {
            if (!ItemStack.areEqual(stack, testStack)) continue;
            count += stack.getCount();
        }
        return count;
    }

    public static int remove(DefaultedList<ItemStack> stacks, ItemStack removeStack) {
        int toRemove = removeStack.getCount();
        for (var stack : stacks) {
            if (!ItemStack.areEqual(stack, removeStack)) continue;

            int removed = stack.getCount();
            stack.decrement(toRemove);

            toRemove -= removed;
            if (toRemove < 1) break;
        }
        return removeStack.getCount() - toRemove;
    }

    @Override
    public String toString() {
        return this.sell + "@" + this.price + "coins";
    }
}
