package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class NumismaticModelProvider implements ModelResourceProvider {

    public static final Identifier COIN_STACK_MODEL_ID = NumismaticOverhaul.id("block/coin_stack");

    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier identifier, ModelProviderContext modelProviderContext) {
        if (!COIN_STACK_MODEL_ID.equals(identifier)) return null;
        return new CoinStackModel();
    }
}
