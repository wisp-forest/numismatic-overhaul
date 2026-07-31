package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.pawn.PawnShopBlock;
import com.glisco.numismaticoverhaul.block.pawn.PawnShopBlockEntity;
import com.glisco.numismaticoverhaul.block.piggy.PiggyBankBlock;
import com.glisco.numismaticoverhaul.block.piggy.PiggyBankBlockEntity;
import com.glisco.numismaticoverhaul.block.shop.ShopBlock;
import com.glisco.numismaticoverhaul.block.shop.ShopBlockEntity;
import com.glisco.numismaticoverhaul.item.CurrencyTooltipData;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import io.wispforest.owo.registration.reflect.BlockEntityRegistryContainer;
import io.wispforest.owo.registration.reflect.BlockRegistryContainer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class NumismaticOverhaulBlocks implements BlockRegistryContainer {

    public static final Block SHOP = new ShopBlock(false);
    public static final Block PAWN_SHOP = new PawnShopBlock(false);
    public static final Block INEXHAUSTIBLE_SHOP = new ShopBlock(true);
    public static final Block INEXHAUSTIBLE_PAWN_SHOP = new PawnShopBlock(true);
    public static final Block PIGGY_BANK = new PiggyBankBlock(null);
    public static final Block WHITE_PIGGY_BANK = new PiggyBankBlock(DyeColor.WHITE);
    public static final Block ORANGE_PIGGY_BANK = new PiggyBankBlock(DyeColor.ORANGE);
    public static final Block MAGENTA_PIGGY_BANK = new PiggyBankBlock(DyeColor.MAGENTA);
    public static final Block LIGHT_BLUE_PIGGY_BANK = new PiggyBankBlock(DyeColor.LIGHT_BLUE);
    public static final Block YELLOW_PIGGY_BANK = new PiggyBankBlock(DyeColor.YELLOW);
    public static final Block LIME_PIGGY_BANK = new PiggyBankBlock(DyeColor.LIME);
    public static final Block PINK_PIGGY_BANK = new PiggyBankBlock(DyeColor.PINK);
    public static final Block GRAY_PIGGY_BANK = new PiggyBankBlock(DyeColor.GRAY);
    public static final Block LIGHT_GRAY_PIGGY_BANK = new PiggyBankBlock(DyeColor.LIGHT_GRAY);
    public static final Block CYAN_PIGGY_BANK = new PiggyBankBlock(DyeColor.CYAN);
    public static final Block PURPLE_PIGGY_BANK = new PiggyBankBlock(DyeColor.PURPLE);
    public static final Block BLUE_PIGGY_BANK = new PiggyBankBlock(DyeColor.BLUE);
    public static final Block BROWN_PIGGY_BANK = new PiggyBankBlock(DyeColor.BROWN);
    public static final Block GREEN_PIGGY_BANK = new PiggyBankBlock(DyeColor.GREEN);
    public static final Block RED_PIGGY_BANK = new PiggyBankBlock(DyeColor.RED);
    public static final Block BLACK_PIGGY_BANK = new PiggyBankBlock(DyeColor.BLACK);

    @Override
    public BlockItem createBlockItem(Block block, String identifier) {
        if (block == INEXHAUSTIBLE_SHOP || block == INEXHAUSTIBLE_PAWN_SHOP) {
            return new BlockItem(block, new OwoItemSettings().group(NumismaticOverhaul.NUMISMATIC_GROUP).rarity(Rarity.EPIC)) {
                @Override
                public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
                    tooltip.add(Text.translatable(stack.getTranslationKey() + ".tooltip").formatted(Formatting.GRAY));
                }
            };
        } else if (block instanceof PiggyBankBlock) {
            return new BlockItem(block, new OwoItemSettings()
                .group(NumismaticOverhaul.NUMISMATIC_GROUP)
                .maxCount(1)
                .equipmentSlot(stack -> EquipmentSlot.HEAD)
            ) {
                @Override
                public Optional<TooltipData> getTooltipData(ItemStack stack) {
                    if (stack.hasNbt() && stack.getNbt().contains("BlockEntityTag")) {
                        var items = DefaultedList.ofSize(3, ItemStack.EMPTY);
                        Inventories.readNbt(stack.getSubNbt("BlockEntityTag"), items);

                        var values = new long[]{items.get(0).getCount(), items.get(1).getCount(), items.get(2).getCount()};
                        return Optional.of(new CurrencyTooltipData(values, new long[]{-1}));
                    } else {
                        return Optional.empty();
                    }
                }
            };
        }
        return new BlockItem(block, new OwoItemSettings().group(NumismaticOverhaul.NUMISMATIC_GROUP)) {
            @Override
            public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
                tooltip.add(Text.translatable(stack.getTranslationKey() + ".tooltip").formatted(Formatting.GRAY));
            }
        };
    }

    public static final class Entities implements BlockEntityRegistryContainer {

        public static final BlockEntityType<ShopBlockEntity> SHOP =
                FabricBlockEntityTypeBuilder.create(ShopBlockEntity::new, NumismaticOverhaulBlocks.SHOP, NumismaticOverhaulBlocks.INEXHAUSTIBLE_SHOP).build();

        public static final BlockEntityType<PawnShopBlockEntity> PAWN_SHOP =
            FabricBlockEntityTypeBuilder.create(PawnShopBlockEntity::new, NumismaticOverhaulBlocks.PAWN_SHOP, NumismaticOverhaulBlocks.INEXHAUSTIBLE_PAWN_SHOP).build();

        public static final BlockEntityType<PiggyBankBlockEntity> PIGGY_BANK =
                FabricBlockEntityTypeBuilder.create(PiggyBankBlockEntity::new,
                    NumismaticOverhaulBlocks.PIGGY_BANK,
                    NumismaticOverhaulBlocks.WHITE_PIGGY_BANK,
                    NumismaticOverhaulBlocks.ORANGE_PIGGY_BANK,
                    NumismaticOverhaulBlocks.MAGENTA_PIGGY_BANK,
                    NumismaticOverhaulBlocks.LIGHT_BLUE_PIGGY_BANK,
                    NumismaticOverhaulBlocks.YELLOW_PIGGY_BANK,
                    NumismaticOverhaulBlocks.LIME_PIGGY_BANK,
                    NumismaticOverhaulBlocks.PINK_PIGGY_BANK,
                    NumismaticOverhaulBlocks.GRAY_PIGGY_BANK,
                    NumismaticOverhaulBlocks.LIGHT_GRAY_PIGGY_BANK,
                    NumismaticOverhaulBlocks.CYAN_PIGGY_BANK,
                    NumismaticOverhaulBlocks.PURPLE_PIGGY_BANK,
                    NumismaticOverhaulBlocks.BLUE_PIGGY_BANK,
                    NumismaticOverhaulBlocks.BROWN_PIGGY_BANK,
                    NumismaticOverhaulBlocks.GREEN_PIGGY_BANK,
                    NumismaticOverhaulBlocks.RED_PIGGY_BANK,
                    NumismaticOverhaulBlocks.BLACK_PIGGY_BANK
                ).build();
    }
}
