package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.item.NumismaticOverhaulItems;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class CoinStackModel implements UnbakedModel, BakedModel, FabricBakedModel {

    private static final SpriteIdentifier PARTICLE_SPRITE_ID =
            new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("minecraft", "block/gold_block"));
    private Sprite particleSprite;

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        this.particleSprite = textureGetter.apply(PARTICLE_SPRITE_ID);
        return this;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockRenderView, BlockState blockState, BlockPos blockPos, Supplier<Random> supplier, RenderContext renderContext) {
        final var emitter = renderContext.getEmitter();
        final var client = MinecraftClient.getInstance();
        final var stack = new ItemStack(NumismaticOverhaulItems.SILVER_COIN);

        var material = RendererAccess.INSTANCE.getRenderer().materialById(RenderMaterial.MATERIAL_STANDARD);
        var model = client.getItemRenderer().getModel(stack, client.world, null, 0);
        var quat = Vec3f.POSITIVE_X.getDegreesQuaternion(90);

        renderContext.pushTransform(quad -> {
            Vec3f v = null;

            for (int i = 0; i < 4; i++) {
                v = quad.copyPos(i, v);
                v.rotate(quat);
                v.add(0, 1, 0);
                v.scale(.25f);
                quad.pos(i, v);
            }

            return true;
        });

        for (var quad : model.getQuads(blockState, null, supplier.get())) {
            emitter.fromVanilla(quad, material, quad.getFace());
            emitter.emit();
        }

        renderContext.popTransform();
    }

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext renderContext) {

    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return this.particleSprite;
    }

    @Override
    public ModelTransformation getTransformation() {
        return null;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return null;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return List.of(PARTICLE_SPRITE_ID);
    }
}
