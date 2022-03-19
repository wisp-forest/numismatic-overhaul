package com.glisco.numismaticoverhaul.currency;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.item.CoinItem;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.List;

public class CurrencyComponent implements Component, AutoSyncedComponent {

    private long value;
    private final PlayerEntity provider;

    private final List<Long> transactions;

    public CurrencyComponent(PlayerEntity provider) {
        this.provider = provider;
        this.transactions = new ArrayList<Long>();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        value = tag.getLong("Value");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putLong("Value", value);
    }

    public long getValue() {
        return value;
    }

    /**
     * This is only to be used in specific edge cases
     * <br>
     * Use {@link CurrencyComponent#modify(long)} or the transaction system wherever possible
     */
    @Deprecated
    public void setValue(long value) {
        this.value = value;

        //Update Client
        if (!provider.world.isClient) {
            ModComponents.CURRENCY.sync(this.provider);
        }
    }

    /**
     * Modifies this component, displays a message in the action bar
     *
     * @param value The value to modify by
     */
    public void modify(long value) {
        setValue(this.value + value);

        long tempValue = value < 0 ? -value : value;

        List<ItemStack> transactionStacks = CurrencyConverter.getAsItemStackList(tempValue);
        if (transactionStacks.isEmpty()) return;

        MutableText message = value < 0 ? new LiteralText("§c- ") : new LiteralText("§a+ ");
        message.append(new LiteralText("§7["));
        for (ItemStack stack : transactionStacks) {
            message.append(new LiteralText("§b" + stack.getCount() + " "));
            message.append(new TranslatableText("currency.numismatic-overhaul." + ((CoinItem) stack.getItem()).currency.name().toLowerCase()));
            if (transactionStacks.indexOf(stack) != transactionStacks.size() - 1) message.append(new LiteralText(", "));
        }
        message.append(new LiteralText("§7]"));

        provider.sendMessage(message, true);
    }

    /**
     * Same as {@link CurrencyComponent#modify(long)}, but doesn't show a message in the action bar
     *
     * @param value The value to modify by
     */
    public void silentModify(long value) {
        setValue(this.value + value);
    }

    /**
     * Enqueues a transaction onto the stack
     *
     * @param value The value this component should be modified by
     */
    public void pushTransaction(long value) {
        this.transactions.add(value);
    }

    /**
     * Pops the most recent transaction off the stack
     *
     * @return The transaction that was popped
     */
    public Long popTransaction() {
        return this.transactions.remove(this.transactions.size() - 1);
    }

    /**
     * Commits the transactions on the current stack into the actual component value and pops the entire stack
     * <br>
     * Displays one accumulated action bar message
     */
    public void commitTransactions() {
        this.modify(this.transactions.stream().mapToLong(Long::longValue).sum());
        this.transactions.clear();
    }
}
