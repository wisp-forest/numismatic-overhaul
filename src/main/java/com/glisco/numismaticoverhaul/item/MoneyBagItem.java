package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.world.World;

import java.util.Optional;

public class MoneyBagItem extends Item implements CurrencyItem {

    public MoneyBagItem() {
        super(new Settings().group(ItemGroup.MISC).maxCount(1));
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack defaultStack = super.getDefaultStack();
        defaultStack.getOrCreateNbt().putInt("Value", 0);
        return defaultStack;
    }

    public static ItemStack create(int value) {
        ItemStack stack = new ItemStack(NumismaticOverhaul.MONEY_BAG);
        stack.getOrCreateNbt().putInt("Value", value);
        return stack;
    }

    public static ItemStack create(int value, boolean carriable) {
        ItemStack stack = create(value);
        stack.getOrCreateNbt().putBoolean("Carriable", carriable);
        return stack;
    }

    public int getValue(ItemStack stack) {
        return stack.getOrCreateNbt().getInt("Value");
    }

    public void setValue(ItemStack stack, int value) {
        stack.getOrCreateNbt().putInt("Value", value);
    }

    @Override
    public boolean onClicked(ItemStack clickedStack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT && clickedStack.getItem() == this && otherStack.isEmpty()) {
            final var coinStack = CurrencyConverter.getAsValidStacks(getValue(clickedStack)).get(0);
            cursorStackReference.set(coinStack);

            final var newValue = getValue(clickedStack) - ((CoinItem) coinStack.getItem()).currency.getRawValue(coinStack.getCount());

            if (CurrencyConverter.getAsValidStacks(newValue).size() == 1) {
                slot.setStack(CurrencyConverter.getAsValidStacks(newValue).get(0));
            } else {
                setValue(clickedStack, newValue);
            }

        } else if (clickType == ClickType.LEFT) {
            if (!(otherStack.getItem() instanceof CurrencyItem currencyItem)) return false;

            int value = currencyItem.getValue(otherStack) + getValue(clickedStack);
            slot.setStack(MoneyBagItem.create(value, true));

            cursorStackReference.set(ItemStack.EMPTY);
        }

        return true;
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return Optional.of(new CurrencyTooltipData(this.getValue(stack), CurrencyItem.hasOriginalValue(stack) ? CurrencyItem.getOriginalValue(stack) : -1));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (stack.getOrCreateNbt().getBoolean("Carriable")) return;
        if (!(entity instanceof PlayerEntity player)) return;

        player.getInventory().removeOne(stack);

        for (ItemStack toOffer : CurrencyConverter.getAsValidStacks(getValue(stack))) {
            player.getInventory().offerOrDrop(toOffer);
        }
    }

    @Override
    public boolean wasAdjusted(ItemStack other) {
        return true;
    }

    @Override
    public Text getName() {
        return super.getName().copy().setStyle(NumismaticOverhaul.SILVER_COIN.NAME_STYLE);
    }

}
