package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.TradeOutputSlot;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import java.util.Optional;

import static com.glisco.numismaticoverhaul.NumismaticOverhaul.MONEY_BAG_COMPONENT;

public class MoneyBagItem extends Item implements CurrencyItem {

    public MoneyBagItem() {
        super(new Settings().maxCount(1).component(MONEY_BAG_COMPONENT, MoneyBagComponent.of(0)));
    }

    public static ItemStack create(ItemStack firstStack, ItemStack otherStack) {
        var stack = new ItemStack(NumismaticOverhaulItems.MONEY_BAG);
        if (firstStack.contains(MONEY_BAG_COMPONENT) && otherStack.contains(MONEY_BAG_COMPONENT)) {
            stack.set(MONEY_BAG_COMPONENT, MoneyBagComponent.combine(firstStack, otherStack));
        } else if (firstStack.getItem() instanceof CurrencyItem coins && otherStack.getItem() instanceof CurrencyItem coins2) {
            var values1 = coins.getCombinedValue(firstStack);
            var values2 = coins2.getCombinedValue(otherStack);
            stack.set(MONEY_BAG_COMPONENT, MoneyBagComponent.combine(values1, values2));
        }
        return stack;
    }

    public static ItemStack fromValues(long[] values) {
        var stack = new ItemStack(NumismaticOverhaulItems.MONEY_BAG);
        stack.set(MONEY_BAG_COMPONENT, MoneyBagComponent.of(values)
        );
        return stack;
    }

    public static ItemStack fromRawValue(long value) {
        var stack = new ItemStack(NumismaticOverhaulItems.MONEY_BAG);
        stack.set(MONEY_BAG_COMPONENT, MoneyBagComponent.of(value));
        return stack;
    }

    public long getValue(ItemStack stack) {
        return stack.getOrDefault(MONEY_BAG_COMPONENT, MoneyBagComponent.of(0)).value();
    }

    @Override
    public long[] getCombinedValue(ItemStack stack) {
        var bagComponent = stack.getOrDefault(MONEY_BAG_COMPONENT, MoneyBagComponent.of(0));
        return new long[]{bagComponent.bronze(), bagComponent.silver(), bagComponent.gold()};
    }

    @Override
    public boolean onClicked(ItemStack clickedStack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (slot instanceof TradeOutputSlot) return false;

        // Withdraw from money bag
        if (clickType == ClickType.RIGHT && clickedStack.getItem() == this && otherStack.isEmpty()) {
            var coins = getCombinedValue(clickedStack);
            final var stackRepresentation = CurrencyConverter.getAsValidStacks(coins);
            if (stackRepresentation.isEmpty()) return false;

            final var coinStack = stackRepresentation.getFirst();
            cursorStackReference.set(coinStack);

            final long[] values = getCombinedValue(clickedStack);
            values[((CoinItem) coinStack.getItem()).currency.ordinal()] -= coinStack.getCount();

            final long newValue = CurrencyResolver.combineValues(values);
            final boolean canBeCompacted = CurrencyResolver.canBeCompacted(values);

            if (newValue == 0) {
                slot.setStack(ItemStack.EMPTY);
            } else if (canBeCompacted && CurrencyConverter.getAsValidStacks(newValue).size() == 1) {
                slot.setStack(CurrencyConverter.getAsValidStacks(newValue).getFirst());
            } else {
                slot.setStack(fromValues(values));
            }

        } else if (clickType == ClickType.LEFT) {
            if (!(otherStack.getItem() instanceof CurrencyItem currencyItem)) return false;
            final var bag = MoneyBagItem.create(clickedStack, otherStack);
            if (bag.getOrDefault(MONEY_BAG_COMPONENT, MoneyBagComponent.of(0)).value() == 0) return false;
            if (!slot.canInsert(bag)) return false;

            slot.setStack(bag);
            return cursorStackReference.set(ItemStack.EMPTY);
        }

        return true;
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        var values = this.getCombinedValue(stack);
        return Optional.of(new CurrencyTooltipData(values, new long[]{-1}));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ModComponents.CURRENCY.get(user).modify(getValue(user.getStackInHand(hand)));
        user.setStackInHand(hand, ItemStack.EMPTY);
        return TypedActionResult.success(ItemStack.EMPTY);
    }

    @Override
    public boolean wasAdjusted(ItemStack other) {
        return true;
    }

    @Override
    public Text getName() {
        return super.getName().copy().setStyle(NumismaticOverhaulItems.SILVER_COIN.NAME_STYLE);
    }

}
