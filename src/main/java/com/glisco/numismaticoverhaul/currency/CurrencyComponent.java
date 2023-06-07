package com.glisco.numismaticoverhaul.currency;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.NumismaticOverhaulConfigModel;
import com.glisco.numismaticoverhaul.item.CoinItem;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.wispforest.owo.config.ConfigSynchronizer;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
        if (!provider.getWorld().isClient) {
            ModComponents.CURRENCY.sync(this.provider);
        }
    }

    /**
     * Modifies this component, displays a message with the change
     *
     * @param value The value to modify by
     */
    public void modify(long value) {
        // This code can be triggered on both Client and Server, for example via Money Bags
        setValue(this.value + value);

        long tempValue = value < 0 ? -value : value;

        List<ItemStack> transactionStacks = CurrencyConverter.getAsItemStackList(tempValue);
        if (transactionStacks.isEmpty()) return;


        // Only do text handling on the server, this is to prevent duplicate text if the config is set to display in chat
        if (provider.getWorld().isClient()) return;

        // Always try to respect the clients option on where they want the message
        NumismaticOverhaulConfigModel.MoneyMessageLocation moneyMessageLocation;

        moneyMessageLocation = (NumismaticOverhaulConfigModel.MoneyMessageLocation) ConfigSynchronizer.getClientOptions(
                (ServerPlayerEntity) provider,
                NumismaticOverhaul.CONFIG).get(NumismaticOverhaul.CONFIG.keys.moneyMessageLocation);

        if (moneyMessageLocation == NumismaticOverhaulConfigModel.MoneyMessageLocation.DISABLED) return;

        // Text handling examples:
        // Actionbar = "+ [12 Silver, 4 Bronze]"
        // Chat = "numismatic > + [12 Silver, 4 Bronze]"
        var message = moneyMessageLocation == NumismaticOverhaulConfigModel.MoneyMessageLocation.CHAT
                ?
                TextOps.withColor("numismatic §> ", Currency.GOLD.getNameColor(), Color.ofFormatting(Formatting.GRAY).argb())
                :
                Text.empty();

        message.append(value < 0 ? Text.literal("§c- ") : Text.literal("§a+ "));
        message.append(Text.literal("§7["));
        for (ItemStack stack : transactionStacks) {
            message.append(Text.literal("§b" + stack.getCount() + " "));
            message.append(TextOps.translateWithColor(
                    "currency.numismatic-overhaul." + ((CoinItem) stack.getItem()).currency.name().toLowerCase(),
                    ((CoinItem) stack.getItem()).currency.getNameColor()
            ));

            if (transactionStacks.indexOf(stack) != transactionStacks.size() - 1) {
                message.append(Text.literal(", "));
            }
        }
        message.append(Text.literal("§7]"));

        provider.sendMessage(message, moneyMessageLocation == NumismaticOverhaulConfigModel.MoneyMessageLocation.ACTIONBAR);

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
