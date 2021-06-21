package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MoneyBagItem extends Item {

    public MoneyBagItem() {
        super(new Settings().group(ItemGroup.MISC).maxCount(1));
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack defaultStack = super.getDefaultStack();
        defaultStack.getOrCreateTag().putInt("Value", 0);
        return defaultStack;
    }

    public static ItemStack create(int value) {
        ItemStack stack = new ItemStack(NumismaticOverhaul.MONEY_BAG);
        stack.getOrCreateTag().putInt("Value", value);
        return stack;
    }

    public static int getValue(ItemStack stack) {
        return stack.getOrCreateTag().getInt("Value");
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.clear();

        CurrencyStack currencyStack = new CurrencyStack(getValue(stack));

        if (currencyStack.getRawValue() == 0) {
            tooltip.add(new TranslatableText("numismatic-overhaul.empty").formatted(Formatting.GRAY));
            return;
        }

        for (ItemStack coin : currencyStack.getAsItemStackList()) {
            tooltip.add(new LiteralText(coin.getCount() + " " + coin.getName().getString().split(" ")[0]).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(((CoinItem) coin.getItem()).currency.getNameColor()))));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof PlayerEntity)) return;

        PlayerEntity player = (PlayerEntity) entity;
        player.getInventory().removeOne(stack);

        CurrencyStack currencyStack = new CurrencyStack(getValue(stack));
        for (ItemStack toOffer : CurrencyStack.splitAtMaxCount(currencyStack.getAsItemStackList())) {
            player.getInventory().offerOrDrop(toOffer);
        }
    }
}
