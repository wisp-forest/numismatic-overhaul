package com.glisco.numismaticoverhaul.currency;

import com.glisco.numismaticoverhaul.item.CoinItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class PlayerCurrencyHelper {

    /**
     * Checks how much money a player has as coin items in their inventory
     *
     * @param player The player to operate on (duh)
     * @param remove Whether to remove all coins from the player in the process
     * @return The amount of currency contained in the player's inventory
     */
    public static int getMoneyInInventory(PlayerEntity player, boolean remove) {

        int value = 0;

        for (int i = 0; i < player.inventory.size(); i++) {
            ItemStack stack = player.inventory.getStack(i);
            if (!(stack.getItem() instanceof CoinItem)) continue;

            value += ((CoinItem) stack.getItem()).currency.getRawValue(stack.getCount());

            if (remove) player.inventory.removeOne(stack);
        }

        return value;
    }

    public static void offerAsCoins(PlayerEntity player, CurrencyStack stack) {
        for (ItemStack itemStack : CurrencyStack.splitAtMaxCount(stack.getAsItemStackList())) {
            player.inventory.offerOrDrop(player.world, itemStack);
        }
    }

    public static boolean deduceFromInventory(PlayerEntity player, int value) {
        int presentInInventory = getMoneyInInventory(player, false);
        if (presentInInventory < value) return false;

        System.out.println("deduce");

        getMoneyInInventory(player, true);

        offerAsCoins(player, new CurrencyStack(presentInInventory - value));
        return true;
    }

}
