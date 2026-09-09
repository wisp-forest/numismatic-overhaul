package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.block.piggy.PiggyBankBlockEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;


public class PiggyBankBlockEntityRenderer implements BlockEntityRenderer<PiggyBankBlockEntity> {


    public PiggyBankBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super();
    }

    @Override
    public void render(PiggyBankBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
    }

    /**
     * Entity rendering for the piggy bank
     * When using this, make sure to rotate the piggy bank correctly.
     * I found this to be useful:
     *         switch (entity.getCachedState().get(PiggyBankBlock.FACING)) {
     *             case EAST -> {
     *                 matrices.translate(1.0f, 0, 1.0);
     *                 matrices.multiplyPositionMatrix(new Matrix4f().rotateY(MathHelper.HALF_PI * 3));
     *             }
     *             case SOUTH -> {
     *                 matrices.multiplyPositionMatrix(new Matrix4f().rotateY(MathHelper.PI));
     *                 matrices.translate(0f, 0, -1.0);
     *             }
     *             case WEST -> matrices.multiplyPositionMatrix(new Matrix4f().rotateY(MathHelper.HALF_PI));
     *             default -> matrices.translate(1.0f, 0, 0);
     *         }
     */
    public static TexturedModelData createModelData() {
        var data = new ModelData();
        var root = data.getRoot();

        root.addChild("bb_main", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -6.0F, -3.0F, 6.0F, 5.0F, 6.0F, Dilation.NONE)
            .uv(1, 11).cuboid(-1.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F, Dilation.NONE)
            .uv(0, 0).cuboid(-3.0F, -1.0F, -3.0F, 1.0F, 1.0F, 2.0F, Dilation.NONE)
            .uv(0, 0).cuboid(-3.0F, -1.0F, 1.0F, 1.0F, 1.0F, 2.0F, Dilation.NONE)
            .uv(0, 0).cuboid(2.0F, -1.0F, 1.0F, 1.0F, 1.0F, 2.0F, Dilation.NONE)
            .uv(0, 0).cuboid(2.0F, -1.0F, -3.0F, 1.0F, 1.0F, 2.0F, Dilation.NONE),
            ModelTransform.pivot(8.0F, 0, 8.0F));

        return TexturedModelData.of(data, 32, 32);
    }
}
