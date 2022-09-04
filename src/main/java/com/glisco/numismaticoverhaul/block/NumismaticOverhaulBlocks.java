package com.glisco.numismaticoverhaul.block;

import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import io.wispforest.owo.registration.reflect.BlockRegistryContainer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NumismaticOverhaulBlocks implements BlockRegistryContainer {

    public static final Block SHOP = new ShopBlock(false);
    public static final Block INEXHAUSTIBLE_SHOP = new ShopBlock(true);
    public static final Block PIGGY_BANK = new PiggyBankBlock();

    @Override
    public BlockItem createBlockItem(Block block, String identifier) {
        if (block == INEXHAUSTIBLE_SHOP) {
            return new BlockItem(block, new Item.Settings().group(ItemGroup.MISC).rarity(Rarity.EPIC)) {
                @Override
                public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
                    tooltip.add(Text.translatable(stack.getTranslationKey() + ".tooltip").formatted(Formatting.GRAY));
                }
            };
        } else if (block == PIGGY_BANK) {
            return new BlockItem(block, new FabricItemSettings().group(ItemGroup.MISC).equipmentSlot(stack -> EquipmentSlot.HEAD));
        }

        return new BlockItem(block, new Item.Settings().group(ItemGroup.MISC));
    }

    public static final class Entities implements AutoRegistryContainer<BlockEntityType<?>> {

        public static final BlockEntityType<ShopBlockEntity> SHOP =
                FabricBlockEntityTypeBuilder.create(ShopBlockEntity::new, NumismaticOverhaulBlocks.SHOP, NumismaticOverhaulBlocks.INEXHAUSTIBLE_SHOP).build();

        @Override
        public Registry<BlockEntityType<?>> getRegistry() {
            return Registry.BLOCK_ENTITY_TYPE;
        }

        @Override
        public Class<BlockEntityType<?>> getTargetFieldType() {
            //noinspection unchecked
            return (Class<BlockEntityType<?>>) (Object) BlockEntityType.class;
        }
    }
}
