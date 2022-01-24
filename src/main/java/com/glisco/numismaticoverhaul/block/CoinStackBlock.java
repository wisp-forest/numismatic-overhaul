package com.glisco.numismaticoverhaul.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class CoinStackBlock extends BlockWithEntity {

    public CoinStackBlock() {
        super(FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK).nonOpaque());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        /*return new Entity(pos, state);*/
        return null;
    }

//    public static class Entity extends BlockEntity implements RenderAttachmentBlockEntity {
//
//        public Entity(BlockPos pos, BlockState state) {
//            super(NumismaticOverhaulBlocks.Entities.COIN_STACK, pos, state);
//        }
//
//        @Override
//        public @Nullable Object getRenderAttachmentData() {
//            return 5;
//        }
//    }
}
