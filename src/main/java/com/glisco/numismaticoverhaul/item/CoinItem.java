package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.currency.Currency;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.TradeOutputSlot;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Optional;

public class CoinItem extends Item implements CurrencyItem {

    public final Currency currency;
    public final Style NAME_STYLE;

    public CoinItem(Currency currency) {
        super(new Settings().group(ItemGroup.MISC).maxCount(99));
        this.currency = currency;
        this.NAME_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(currency.getNameColor()));
    }

    @Override
    public boolean onClicked(ItemStack clickedStack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (slot instanceof TradeOutputSlot) return false;
        if (clickType != ClickType.LEFT) return false;

        if ((otherStack.getItem() == this && otherStack.getCount() + clickedStack.getCount() <= otherStack.getMaxCount()) || !(otherStack.getItem() instanceof CurrencyItem currencyItem))
            return false;

        long[] values = currencyItem.getCombinedValue(otherStack);
        values[this.currency.ordinal()] += clickedStack.getCount();

        slot.setStack(MoneyBagItem.createCombined(values));

        cursorStackReference.set(ItemStack.EMPTY);
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack clickedStack = user.getStackInHand(hand);
        long rawValue = ((CoinItem) clickedStack.getItem()).currency.getRawValue(clickedStack.getCount());

        if (!world.isClient) {
            ModComponents.CURRENCY.get(user).modify(rawValue);

            user.setStackInHand(hand, ItemStack.EMPTY);
        }

        return TypedActionResult.success(clickedStack);
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return Optional.of(new CurrencyTooltipData(this.currency.getRawValue(stack.getCount()),
                CurrencyItem.hasOriginalValue(stack) ? CurrencyItem.getOriginalValue(stack) : -1));
    }

    @Override
    public Text getName() {
        return super.getName().copy().setStyle(NAME_STYLE);
    }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack).copy().setStyle(NAME_STYLE);
    }

    @Override
    public boolean wasAdjusted(ItemStack other) {
        return other.getItem() != this;
    }

    @Override
    public long getValue(ItemStack stack) {
        return this.currency.getRawValue(stack.getCount());
    }

    @Override
    public long[] getCombinedValue(ItemStack stack) {
        final long[] values = new long[3];
        values[this.currency.ordinal()] = stack.getCount();
        return values;
    }
}
