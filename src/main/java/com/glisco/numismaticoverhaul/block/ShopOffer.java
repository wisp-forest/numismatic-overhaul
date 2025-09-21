package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.item.MoneyBagComponent;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.ComponentPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;

public record ShopOffer(ItemStack sell, long price) {
    public static final Endec<ShopOffer> ENDEC = StructEndecBuilder.of(
        CodecUtils.toEndec(ItemStack.VALIDATED_CODEC).fieldOf("sell", ShopOffer::getSellStack),
        Endec.LONG.fieldOf("price", ShopOffer::getPrice),
        ShopOffer::new
    );


    public ShopOffer {
        if (sell.isEmpty()) throw new IllegalArgumentException("Sell Stack must not be empty");
        if (price == 0) throw new IllegalArgumentException("Price must not be null");
    }

    public TradeOffer toTradeOffer(ShopBlockEntity shop, boolean inexhaustible) {
        boolean isPocketChange = CurrencyConverter.getRequiredCurrencyTypes(price) == 1;
        var buyStack = isPocketChange ? CurrencyConverter.getAsItemStackList(price).getFirst() : MoneyBagItem.fromRawValue(price);
        int maxUses = inexhaustible ? Integer.MAX_VALUE : count(shop.getItems(), sell) / sell.getCount();
        var tradedItem = isPocketChange ? new TradedItem(buyStack.getItem(), buyStack.getCount()) :
            new TradedItem(
                Registries.ITEM.getEntry(buyStack.getItem()),
                1,
                ComponentPredicate.of(
                    ComponentMap.of(ComponentMap.EMPTY,
                        ComponentMap.builder().add(NumismaticOverhaul.MONEY_BAG_COMPONENT, MoneyBagComponent.of(price)).build()
                    )
                ),
                buyStack
            );

        return new TradeOffer(tradedItem, sell, maxUses, 0, 0);
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
            if (!ItemStack.areItemsAndComponentsEqual(stack, testStack)) continue;
            count += stack.getCount();
        }
        return count;
    }

    public static int remove(DefaultedList<ItemStack> stacks, ItemStack removeStack) {
        int toRemove = removeStack.getCount();
        for (var stack : stacks) {
            if (!ItemStack.areItemsAndComponentsEqual(stack, removeStack)) continue;

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
