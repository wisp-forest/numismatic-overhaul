package com.glisco.numismaticoverhaul.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class PiggyBankBlock extends HorizontalFacingBlock {

    private static final VoxelShape NORTH_SHAPE = Stream.of(
            Block.createCuboidShape(7, 2, 4, 9, 4, 5),
            Block.createCuboidShape(5, 1, 5, 11, 6, 11),
            Block.createCuboidShape(5, 0, 5, 6, 1, 7),
            Block.createCuboidShape(5, 0, 9, 6, 1, 11),
            Block.createCuboidShape(10, 0, 9, 11, 1, 11),
            Block.createCuboidShape(10, 0, 5, 11, 1, 7)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape SOUTH_SHAPE = Stream.of(
            Block.createCuboidShape(7, 2, 11, 9, 4, 12),
            Block.createCuboidShape(5, 1, 5, 11, 6, 11),
            Block.createCuboidShape(10, 0, 9, 11, 1, 11),
            Block.createCuboidShape(10, 0, 5, 11, 1, 7),
            Block.createCuboidShape(5, 0, 5, 6, 1, 7),
            Block.createCuboidShape(5, 0, 9, 6, 1, 11)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape EAST_SHAPE = Stream.of(
            Block.createCuboidShape(11, 2, 7, 12, 4, 9),
            Block.createCuboidShape(5, 1, 5, 11, 6, 11),
            Block.createCuboidShape(9, 0, 5, 11, 1, 6),
            Block.createCuboidShape(5, 0, 5, 7, 1, 6),
            Block.createCuboidShape(5, 0, 10, 7, 1, 11),
            Block.createCuboidShape(9, 0, 10, 11, 1, 11)
    ).reduce(VoxelShapes::union).get();

    private static final VoxelShape WEST_SHAPE = Stream.of(
            Block.createCuboidShape(4, 2, 7, 5, 4, 9),
            Block.createCuboidShape(5, 1, 5, 11, 6, 11),
            Block.createCuboidShape(5, 0, 10, 7, 1, 11),
            Block.createCuboidShape(9, 0, 10, 11, 1, 11),
            Block.createCuboidShape(9, 0, 5, 11, 1, 6),
            Block.createCuboidShape(5, 0, 5, 7, 1, 6)
    ).reduce(VoxelShapes::union).get();

    public PiggyBankBlock() {
        super(FabricBlockSettings.copyOf(Blocks.TERRACOTTA));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }
}
