package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.network.UpdateShopScreenS2CPacket;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShopBlock extends BlockWithEntity {

    private static final VoxelShape MAIN_PILLAR = Block.createCuboidShape(1, 0, 1, 14, 8, 14);

    private static final VoxelShape PLATE = Block.createCuboidShape(0, 8, 0, 16, 12, 16);

    private static final VoxelShape PILLAR_1 = Block.createCuboidShape(13, 0, 0, 16, 8, 3);
    private static final VoxelShape PILLAR_2 = Block.createCuboidShape(0, 0, 0, 3, 8, 3);
    private static final VoxelShape PILLAR_3 = Block.createCuboidShape(0, 0, 13, 3, 8, 16);
    private static final VoxelShape PILLAR_4 = Block.createCuboidShape(13, 0, 13, 16, 8, 16);

    private static final VoxelShape SHAPE = VoxelShapes.union(MAIN_PILLAR, PLATE, PILLAR_1, PILLAR_2, PILLAR_3, PILLAR_4);

    private final boolean inexhaustible;

    public ShopBlock(boolean inexhaustible) {
        super(FabricBlockSettings.create().nonOpaque().hardness(5.0f));
        this.inexhaustible = inexhaustible;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {

            ShopBlockEntity shop = (ShopBlockEntity) world.getBlockEntity(pos);

            if (shop.getOwner().equals(player.getUuid())) {
                if (player.isSneaking()) {
                    return openShopMerchant(player, shop);
                } else {
                    player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
                    NumismaticOverhaul.CHANNEL.serverHandle(player).send(new UpdateShopScreenS2CPacket(shop));
                }
            } else {
                return openShopMerchant(player, shop);
            }
        }
        return ActionResult.SUCCESS;
    }

    private ActionResult openShopMerchant(PlayerEntity player, ShopBlockEntity shop) {
        if (shop.getMerchant().getCustomer() != null) return ActionResult.SUCCESS;

        ((ShopMerchant) shop.getMerchant()).updateTrades();
        shop.getMerchant().setCustomer(player);
        shop.getMerchant().sendOffers(player, Text.translatable("gui.numismatic-overhaul.shop.merchant_title"), 0);

        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world.isClient) return;

        if (!(placer instanceof ServerPlayerEntity)) {
            world.breakBlock(pos, true);
            return;
        }

        if (itemStack.hasCustomName() && world.getBlockEntity(pos) instanceof ShopBlockEntity shop) {
            shop.setCustomName(itemStack.getName());
        }

        ((ShopBlockEntity) world.getBlockEntity(pos)).setOwner(placer.getUuid());
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof ShopBlockEntity shop) {
                CurrencyConverter.getAsValidStacks(shop.getStoredCurrency())
                        .forEach(stack -> ItemScatterer.spawn(shop.getWorld(), pos.getX(), pos.getY(), pos.getZ(), stack));

                ItemScatterer.spawn(world, pos, shop);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    public boolean inexhaustible() {
        return this.inexhaustible;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ShopBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, NumismaticOverhaulBlocks.Entities.SHOP, ShopBlockEntity::tick);
    }
}
