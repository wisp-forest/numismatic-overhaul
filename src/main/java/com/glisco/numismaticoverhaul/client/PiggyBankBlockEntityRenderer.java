package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.block.piggy.PiggyBankBlock;
import com.glisco.numismaticoverhaul.block.piggy.PiggyBankBlockEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import static com.glisco.numismaticoverhaul.client.NumismaticOverhaulClient.PIGGY_BANK;

public class PiggyBankBlockEntityRenderer implements BlockEntityRenderer<PiggyBankBlockEntity> {

    private final ModelPart model;

    public PiggyBankBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super();
        this.model = context.getLayerModelPart(PIGGY_BANK);
    }

    @Override
    public void render(PiggyBankBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        SpriteIdentifier spriteIdentifier;
        var dyeColor = entity.getColor();
        if (dyeColor == null) {
            spriteIdentifier = NumismaticOverhaulClient.PIGGY_BANK_TEXTURE_ID;
        } else {
            spriteIdentifier = NumismaticOverhaulClient.COLORED_PIGGY_BANKS.get(dyeColor);
        }
        matrices.push();

        switch (entity.getCachedState().get(PiggyBankBlock.FACING)) {
            case EAST -> {
                matrices.translate(1.0f, 0, 1.0);
                matrices.multiplyPositionMatrix(new Matrix4f().rotateY(MathHelper.HALF_PI * 3));
            }
            case SOUTH -> {
                matrices.multiplyPositionMatrix(new Matrix4f().rotateY(MathHelper.PI));
                matrices.translate(0f, 0, -1.0);
            }
            case WEST -> matrices.multiplyPositionMatrix(new Matrix4f().rotateY(MathHelper.HALF_PI));
            default -> matrices.translate(1.0f, 0, 0);
        }

        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(180));
        VertexConsumer vertexConsumer = spriteIdentifier.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutoutNoCull);
        this.model.render(matrices, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        matrices.pop();
    }

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
