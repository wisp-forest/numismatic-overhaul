package com.glisco.numismaticoverhaul.recipe;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.piggy.PiggyBankBlock;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class PiggyBankColoringRecipe extends SpecialCraftingRecipe {

    public PiggyBankColoringRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput recipeInputInventory, World world) {
        int i = 0;
        int j = 0;

        for (int k = 0; k < recipeInputInventory.getSize(); ++k) {
            ItemStack itemStack = recipeInputInventory.getStackInSlot(k);
            if (!itemStack.isEmpty()) {
                if (Block.getBlockFromItem(itemStack.getItem()) instanceof PiggyBankBlock) {
                    ++i;
                } else {
                    if (!(itemStack.getItem() instanceof DyeItem)) {
                        return false;
                    }

                    ++j;
                }

                if (j > 1 || i > 1) {
                    return false;
                }
            }
        }

        return i == 1 && j == 1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput recipeInputInventory, RegistryWrapper.WrapperLookup dynamicRegistryManager) {
        ItemStack itemStack = ItemStack.EMPTY;
        DyeItem dyeItem = (DyeItem) Items.WHITE_DYE;

        for (int i = 0; i < recipeInputInventory.getSize(); ++i) {
            ItemStack itemStack2 = recipeInputInventory.getStackInSlot(i);
            if (!itemStack2.isEmpty()) {
                Item item = itemStack2.getItem();
                if (Block.getBlockFromItem(item) instanceof PiggyBankBlock) {
                    itemStack = itemStack2;
                } else if (item instanceof DyeItem) {
                    dyeItem = (DyeItem) item;
                }
            }
        }

        ItemStack itemStack3 = PiggyBankBlock.getPiggy(dyeItem.getColor());
        if (!itemStack.getComponentChanges().isEmpty()) {
            itemStack3.applyComponentsFrom(itemStack.getComponents());
        }

        return itemStack3;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NumismaticOverhaul.PIGGY_BANK_COLORING_RECIPE;
    }

}
