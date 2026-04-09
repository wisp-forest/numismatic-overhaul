package com.glisco.numismaticoverhaul.block.pawn;

import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.glisco.numismaticoverhaul.villagers.data.NumismaticTradeOfferExtensions;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.TradeOffer;

import java.util.List;

public class PawnShopOffer {

    private final ItemStack buy;
    private final long price;

    public PawnShopOffer(ItemStack buy, long price) {

        if (buy.isEmpty()) throw new IllegalArgumentException("Buy Stack must not be empty");
        if (price == 0) throw new IllegalArgumentException("Price must not be null");

        this.buy = buy;
        this.price = price;
    }

    public static void add(PawnShopBlockEntity pawnShop, ItemStack boughtStack) {
        var shopItems = pawnShop.getItems();
        // loop once to merge existing stacks together
        for (ItemStack stack : shopItems) {
            if (stack.getItem() == boughtStack.getItem() && ItemStack.canCombine(stack, boughtStack)) {
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
        // TODO - max uses must be calculated both from available space in the shop block, as well as stored currency in the shop
        int maxUses = inexhaustible ? Integer.MAX_VALUE : (int) (shop.getStoredCurrency() / price);
        if (!shop.getItems().contains(ItemStack.EMPTY)) {
            maxUses = 0;
        }
        var money = CurrencyConverter.getRequiredCurrencyTypes(price) == 1 ? CurrencyConverter.getAsItemStackList(price).get(0) : MoneyBagItem.create(price);

        final var tradeOffer = new TradeOffer(buy, money, maxUses, 0, 0);
        ((NumismaticTradeOfferExtensions) tradeOffer).numismatic$setReputation(-69420);
        return tradeOffer;
    }

    public long getPrice() {
        return price;
    }

    public ItemStack getBuyStack() {
        return buy.copy();
    }

    public static NbtCompound writeAll(NbtCompound tag, List<PawnShopOffer> offers) {

        NbtList offerList = new NbtList();

        for (PawnShopOffer offer : offers) {
            offerList.add(offer.toNbt());
        }

        tag.put("Offers", offerList);

        return tag;
    }

    public static void readAll(NbtCompound tag, List<PawnShopOffer> offers) {
        offers.clear();

        NbtList offerList = tag.getList("Offers", NbtElement.COMPOUND_TYPE);

        for (NbtElement offerTag : offerList) {
            offers.add(fromNbt((NbtCompound) offerTag));
        }
    }

    public NbtCompound toNbt() {
        var nbt = new NbtCompound();
        nbt.putLong("Price", this.price);

        var itemNbt = new NbtCompound();
        this.buy.writeNbt(itemNbt);

        nbt.put("Item", itemNbt);
        return nbt;
    }

    public static PawnShopOffer fromNbt(NbtCompound nbt) {
        var item = ItemStack.fromNbt(nbt.getCompound("Item"));
        return new PawnShopOffer(item, nbt.getLong("Price"));
    }

    public static int count(DefaultedList<ItemStack> stacks, ItemStack testStack) {
        int count = 0;
        for (var stack : stacks) {
            if (!ItemStack.canCombine(stack, testStack)) continue;
            count += stack.getCount();
        }
        return count;
    }

    @Override
    public String toString() {
        return this.buy + "@" + this.price + "coins";
    }
}
