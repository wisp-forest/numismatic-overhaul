package com.glisco.numismaticoverhaul.block;

import io.wispforest.owo.registration.reflect.AutoRegistryContainer;
import io.wispforest.owo.registration.reflect.BlockRegistryContainer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;

public class NumismaticOverhaulBlocks implements BlockRegistryContainer {

    public static final Block SHOP = new ShopBlock();
//    public static final Block COIN_STACK = new CoinStackBlock();

    @Override
    public BlockItem createBlockItem(Block block, String identifier) {
        return new BlockItem(block, new Item.Settings().group(ItemGroup.MISC));
    }

    public static final class Entities implements AutoRegistryContainer<BlockEntityType<?>> {

        public static final BlockEntityType<ShopBlockEntity> SHOP = FabricBlockEntityTypeBuilder.create(ShopBlockEntity::new, NumismaticOverhaulBlocks.SHOP).build();
//        public static final BlockEntityType<CoinStackBlock.Entity> COIN_STACK = FabricBlockEntityTypeBuilder.create(CoinStackBlock.Entity::new, NumismaticOverhaulBlocks.SHOP).build();

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
