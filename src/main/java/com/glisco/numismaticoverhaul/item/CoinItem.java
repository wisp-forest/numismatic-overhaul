package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CoinItem extends Item {

    public final CurrencyResolver.Currency currency;

    private final Style NAME_STYLE;

    public CoinItem(CurrencyResolver.Currency currency) {
        super(new Settings().group(ItemGroup.MISC).maxCount(99));
        this.currency = currency;
        this.NAME_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(currency.getNameColor()));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack clickedStack = user.getStackInHand(hand);
        int rawValue = ((CoinItem) clickedStack.getItem()).currency.getRawValue(clickedStack.getCount());

        if (!world.isClient) {
            ModComponents.CURRENCY.get(user).modify(rawValue);

            user.setStackInHand(hand, ItemStack.EMPTY);
        }

        return TypedActionResult.success(clickedStack);
    }

    @Override
    public Text getName() {
        return super.getName().copy().setStyle(NAME_STYLE);
    }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack).copy().setStyle(NAME_STYLE);
    }
}
