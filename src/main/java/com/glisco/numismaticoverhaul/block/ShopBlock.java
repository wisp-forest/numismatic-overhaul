package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.currency.CurrencyStack;
import com.glisco.numismaticoverhaul.network.UpdateShopScreenS2CPacket;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
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

    public ShopBlock() {
        super(FabricBlockSettings.of(Material.STONE).breakByTool(FabricToolTags.PICKAXES).nonOpaque().hardness(5.0f));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new ShopBlockEntity();
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
                    ((ServerPlayerEntity) player).networkHandler.sendPacket(UpdateShopScreenS2CPacket.create(shop.getOffers(), shop.getStoredCurrency()));
                }
            } else {
                return openShopMerchant(player, shop);
            }
        }
        return ActionResult.SUCCESS;
    }

    private ActionResult openShopMerchant(PlayerEntity player, ShopBlockEntity shop) {
        if (shop.getMerchant().getCurrentCustomer() != null) return ActionResult.SUCCESS;

        ((ShopMerchant) shop.getMerchant()).updateTrades();
        shop.getMerchant().setCurrentCustomer(player);
        shop.getMerchant().sendOffers(player, new TranslatableText("gui.numismatic-overhaul.shop.merchant_title"), 0);

        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world.isClient) return;

        if (!(placer instanceof ServerPlayerEntity)) {
            world.breakBlock(pos, true);
            return;
        }

        ((ShopBlockEntity) world.getBlockEntity(pos)).setOwner(placer.getUuid());
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            ShopBlockEntity shop = (ShopBlockEntity) world.getBlockEntity(pos);

            CurrencyStack.splitAtMaxCount(new CurrencyStack(shop.getStoredCurrency()).getAsItemStackList()).forEach(stack -> ItemScatterer.spawn(shop.getWorld(), pos.getX(), pos.getY(), pos.getZ(), stack));
            ItemScatterer.spawn(shop.getWorld(), pos, shop);

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }
}
