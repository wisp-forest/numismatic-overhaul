package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.TradeOutputSlot;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Optional;

public class MoneyBagItem extends Item implements CurrencyItem {

    public MoneyBagItem() {
        super(new Settings().maxCount(1));
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack defaultStack = super.getDefaultStack();
        defaultStack.getOrCreateNbt().putLong("Value", 0);
        return defaultStack;
    }

    public static ItemStack create(long value) {
        ItemStack stack = new ItemStack(NumismaticOverhaulItems.MONEY_BAG);
        stack.getOrCreateNbt().putLong("Value", value);
        return stack;
    }

    public static ItemStack createCombined(long[] values) {
        ItemStack stack = new ItemStack(NumismaticOverhaulItems.MONEY_BAG);
        stack.getOrCreateNbt().putLongArray("Values", values);
        stack.getOrCreateNbt().putBoolean("Combined", true);
        return stack;
    }

    public long getValue(ItemStack stack) {
        if (stack.getItem() != NumismaticOverhaulItems.MONEY_BAG) return 0;

        if (!stack.getOrCreateNbt().contains("Combined", NbtElement.BYTE_TYPE)) {
            return stack.getOrCreateNbt().getLong("Value");
        } else {
            return CurrencyResolver.combineValues(CurrencyHelper.getFromNbt(stack.getOrCreateNbt(), "Values"));
        }
    }

    @Override
    public long[] getCombinedValue(ItemStack stack) {
        if (!stack.getOrCreateNbt().contains("Combined", NbtElement.BYTE_TYPE)) {
            return CurrencyResolver.splitValues(stack.getOrCreateNbt().getLong("Value"));
        } else {
            return CurrencyHelper.getFromNbt(stack.getOrCreateNbt(), "Values");
        }
    }

    public void setValue(ItemStack stack, long value) {
        stack.getOrCreateNbt().putLong("Value", value);
    }

    public void setCombinedValue(ItemStack stack, long[] values) {
        stack.getOrCreateNbt().putLongArray("Values", values);
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
                setCombinedValue(clickedStack, values);
            }

        } else if (clickType == ClickType.LEFT) {
            if (!(otherStack.getItem() instanceof CurrencyItem currencyItem)) return false;

            long[] clickedValues = getCombinedValue(clickedStack);
            long[] otherValues = currencyItem.getCombinedValue(otherStack);

            for (int i = 0; i < clickedValues.length; i++) clickedValues[i] += otherValues[i];

            slot.setStack(MoneyBagItem.createCombined(clickedValues));

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
        if (stack.getOrCreateNbt().getBoolean("Combined")) return;
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
