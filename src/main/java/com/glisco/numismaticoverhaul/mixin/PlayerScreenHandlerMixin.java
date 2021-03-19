package com.glisco.numismaticoverhaul.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin extends AbstractRecipeScreenHandler<CraftingInventory> {

    @Shadow
    @Final
    private PlayerEntity owner;

    public PlayerScreenHandlerMixin(ScreenHandlerType<?> screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    //TODO handle this via custom packet
    /*@Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id != 0) {
            for (int i = 0; i < owner.inventory.size(); i++) {
                ItemStack stack = owner.inventory.getStack(i);
                if (!(stack.getItem() instanceof CurrencyItem)) continue;

                CurrencyItem currency = (CurrencyItem) stack.getItem();
                ModComponents.CURRENCY.get(owner).pushTransaction(currency.currency.getRawValue(stack.getCount()));

                owner.inventory.removeOne(stack);
            }

            ModComponents.CURRENCY.get(owner).commitTransactions();
        }

        return true;
    }*/

}
