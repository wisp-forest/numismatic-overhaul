package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.currency.CurrencyStack;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.village.TradeOffer;

import java.util.List;

public class ShopOffer {

    private final ItemStack sell;
    private final int price;

    public ShopOffer(ItemStack sell, int price) {

        if (sell.isEmpty()) throw new IllegalArgumentException("Sell Stack must not be empty");
        if (price == 0) throw new IllegalArgumentException("Price must not be null");

        this.sell = sell;
        this.price = price;
    }

    public TradeOffer toTradeOffer(ShopBlockEntity shop) {

        CurrencyStack priceStack = new CurrencyStack(price);
        ItemStack buy = priceStack.getRequiredCurrencyTypes() == 1 ? priceStack.getAsItemStackList().get(0) : MoneyBagItem.create(price);

        int maxUses = shop.getItems().stream().filter(stack -> {
            ItemStack comparisonStack = stack.copy();
            comparisonStack.setCount(1);
            return ItemStack.areEqual(comparisonStack, sell);
        }).mapToInt(ItemStack::getCount).sum();

        return new TradeOffer(buy, sell, maxUses, 0, 0);
    }

    public int getPrice() {
        return price;
    }

    public ItemStack getSellStack() {
        return sell.copy();
    }

    public static CompoundTag toTag(CompoundTag tag, List<ShopOffer> offers) {

        ListTag offerList = new ListTag();

        for (ShopOffer offer : offers) {
            CompoundTag offerTag = new CompoundTag();
            offerTag.putInt("Price", offer.getPrice());

            CompoundTag item = new CompoundTag();
            offer.getSellStack().toTag(item);

            offerTag.put("Item", item);

            offerList.add(offerTag);
        }

        tag.put("Offers", offerList);

        return tag;
    }

    public static void fromTag(CompoundTag tag, List<ShopOffer> offers) {

        offers.clear();

        ListTag offerList = tag.getList("Offers", 10);

        for (Tag offerTag : offerList) {

            CompoundTag offer = (CompoundTag) offerTag;

            int price = offer.getInt("Price");

            ItemStack sell = ItemStack.fromTag(offer.getCompound("Item"));

            offers.add(new ShopOffer(sell, price));
        }
    }

    @Override
    public String toString() {
        return this.sell + "@" + this.price + "coins";
    }
}
