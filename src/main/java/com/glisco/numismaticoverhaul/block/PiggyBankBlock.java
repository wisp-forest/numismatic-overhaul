package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.mojang.serialization.MapCodec;
import io.wispforest.owo.ops.WorldOps;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class PiggyBankBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    public static final MapCodec<PiggyBankBlock> CODEC = createCodec(ignored -> new PiggyBankBlock());

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
        super(FabricBlockSettings.create().strength(1.25F, 4.2F));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
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

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        if (!world.isClient) {
            if (world.getBlockEntity(pos) instanceof NamedScreenHandlerFactory factory) {
                player.openHandledScreen(factory);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (entity instanceof FallingBlockEntity fallingBlock && fallingBlock.getBlockState().isIn(NumismaticOverhaul.VERY_HEAVY_BLOCKS) && !world.isClient) {
            if (world.getBlockEntity(pos) instanceof PiggyBankBlockEntity piggyBank) {
                ItemScatterer.spawn(world, pos.offset(world.getBlockState(pos).get(FACING).getOpposite()), piggyBank.inventory());
            }

            world.removeBlock(pos, false);

            WorldOps.playSound(world, pos, NumismaticOverhaul.PIGGY_BANK_BREAK, SoundCategory.BLOCKS);
            NumismaticOverhaul.PIGGY_BANK_BROKEN.spawn(world, Vec3d.of(pos), Math.round(fallDistance));
        }

        super.onLandedUpon(world, state, pos, entity, fallDistance);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world.getBlockEntity(pos) instanceof PiggyBankBlockEntity piggyBank && player.isCreative() && !world.isClient && !piggyBank.inventory().stream().allMatch(ItemStack::isEmpty)) {

            var stack = new ItemStack(NumismaticOverhaulBlocks.PIGGY_BANK);
            piggyBank.setStackNbt(stack);

            ItemEntity var = new ItemEntity(world, pos.getX() + .5d, pos.getY() + .5d, pos.getZ() + .5d, stack);
            var.setToDefaultPickupDelay();
            world.spawnEntity(var);
        }

        return super.onBreak(world, pos, state, player);
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        if (builder.getOptional(LootContextParameters.BLOCK_ENTITY) instanceof PiggyBankBlockEntity piggyBank) {
            var tool = builder.getOptional(LootContextParameters.TOOL);
            if (tool != null && tool.hasCustomName() && Objects.equals(tool.getName().getString(), "Hammer")) {

                WorldOps.playSound(piggyBank.getWorld(), piggyBank.getPos(), NumismaticOverhaul.PIGGY_BANK_BREAK, SoundCategory.BLOCKS);
                NumismaticOverhaul.PIGGY_BANK_BROKEN.spawn(piggyBank.getWorld(), Vec3d.of(piggyBank.getPos()), 5);

                var drops = new ArrayList<>(super.getDroppedStacks(state, builder));
                piggyBank.inventory().stream().filter(stack -> !stack.isEmpty()).forEach(drops::add);
                return drops;
            } else {
                builder.addDynamicDrop(new Identifier("contents"), (consumer) -> {
                    piggyBank.inventory().forEach(consumer);
                });
            }
        }

        return super.getDroppedStacks(state, builder);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PiggyBankBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
}
