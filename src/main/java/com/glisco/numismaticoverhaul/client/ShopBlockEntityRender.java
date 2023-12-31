package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.block.ShopBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class ShopBlockEntityRender implements BlockEntityRenderer<ShopBlockEntity> {

    public ShopBlockEntityRender(BlockEntityRendererFactory.Context context) {
        super();
    }

    @Override
    public void render(ShopBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        var client = MinecraftClient.getInstance();
        var world = entity.getWorld();

        if (entity.getOffers().isEmpty() || world == null) return;

        ItemStack toRender = entity.getItemToRender();
        boolean isBlockItem = toRender.getItem() instanceof BlockItem;

        int lightAbove = WorldRenderer.getLightmapCoordinates(world, entity.getPos().up());

        matrices.push();
        matrices.translate(0.5, isBlockItem ? 0.85 : 0.95, 0.5);

        float scale = isBlockItem ? 0.95f : 0.85f;
        matrices.scale(scale, scale, scale);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) (System.currentTimeMillis() / 20d % 360d)));

        client.getItemRenderer().renderItem(toRender, ModelTransformationMode.GROUND, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, 0);

        matrices.pop();

    }
}
