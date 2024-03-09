package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.currency.*;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.TradeOutputSlot;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import java.util.Optional;

public class MoneyBagItem extends Item implements CurrencyItem {

    public static final KeyedEndec<Boolean> COMBINED = new KeyedEndec<>("Combined", Endec.BOOLEAN, false);

    public MoneyBagItem() {
        super(new Settings().maxCount(1));
    }

    @Override
    public ItemStack getDefaultStack() {
        var defaultStack = super.getDefaultStack();
        defaultStack.put(CurrencyHelper.VALUE, 0L);
        return defaultStack;
    }

    public static ItemStack create(long value) {
        var stack = new ItemStack(NumismaticOverhaulItems.MONEY_BAG);
        stack.put(CurrencyHelper.VALUE, value);
        return stack;
    }

    public static ItemStack createCombined(long[] values) {
        var stack = new ItemStack(NumismaticOverhaulItems.MONEY_BAG);
        stack.put(CurrencyHelper.VALUES, values);
        stack.put(COMBINED, true);
        return stack;
    }

    public long getValue(ItemStack stack) {
        if (stack.getItem() != NumismaticOverhaulItems.MONEY_BAG) return 0;

        if (!stack.has(COMBINED)) {
            return stack.get(CurrencyHelper.VALUE);
        } else {
            return CurrencyResolver.combineValues(CurrencyHelper.getValues(stack));
        }
    }

    @Override
    public long[] getCombinedValue(ItemStack stack) {
        if (!stack.has(COMBINED)) {
            return CurrencyResolver.splitValues(stack.get(CurrencyHelper.VALUE));
        } else {
            return CurrencyHelper.getValues(stack);
        }
    }

    @Override
    public boolean onClicked(ItemStack clickedStack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (slot instanceof TradeOutputSlot) return false;

        if (clickType == ClickType.RIGHT && clickedStack.getItem() == this && otherStack.isEmpty()) {
            final var stackRepresentation = CurrencyConverter.getAsValidStacks(getCombinedValue(clickedStack));
            if (stackRepresentation.isEmpty()) return false;

            final var coinStack = stackRepresentation.get(0);
            cursorStackReference.set(coinStack);

            final long[] values = getCombinedValue(clickedStack);
            values[((CoinItem) coinStack.getItem()).currency.ordinal()] -= coinStack.getCount();

            final long newValue = CurrencyResolver.combineValues(values);
            final boolean canBeCompacted = values[0] < 100 && values[1] < 100 && values[2] < 100;

            if (newValue == 0) {
                slot.setStack(ItemStack.EMPTY);
            } else if (canBeCompacted && CurrencyConverter.getAsValidStacks(newValue).size() == 1) {
                slot.setStack(CurrencyConverter.getAsValidStacks(newValue).get(0));
            } else {
                clickedStack.put(CurrencyHelper.VALUES, values);
            }

        } else if (clickType == ClickType.LEFT) {
            if (!(otherStack.getItem() instanceof CurrencyItem currencyItem)) return false;

            long[] clickedValues = getCombinedValue(clickedStack);
            long[] otherValues = currencyItem.getCombinedValue(otherStack);

            for (int i = 0; i < clickedValues.length; i++) clickedValues[i] += otherValues[i];

            final var stack = MoneyBagItem.createCombined(clickedValues);
            if (!slot.canInsert(stack)) return false;

            slot.setStack(stack);
            cursorStackReference.set(ItemStack.EMPTY);
        }

        return true;
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return Optional.of(new CurrencyTooltipData(this.getCombinedValue(stack),
                CurrencyItem.hasOriginalValue(stack) ? CurrencyResolver.splitValues(CurrencyItem.getOriginalValue(stack)) : new long[]{-1}));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (stack.get(COMBINED)) return;
        if (!(entity instanceof PlayerEntity player)) return;

        player.getInventory().removeOne(stack);

        for (ItemStack toOffer : CurrencyConverter.getAsValidStacks(getValue(stack))) {
            player.getInventory().offerOrDrop(toOffer);
        }
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
