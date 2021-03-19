package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.currency.CurrencyStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class MoneyBagItem extends Item {

    public MoneyBagItem() {
        super(new Settings().group(ItemGroup.MISC).maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        user.sendMessage(new LiteralText("Balance on " + (world.isClient ? "client" : "server") + ": " + ModComponents.CURRENCY.get(user).getValue() + new CurrencyStack(ModComponents.CURRENCY.get(user).getValue()).getAsItemStackList()), false);

        return TypedActionResult.success(user.getStackInHand(hand));
    }
}
