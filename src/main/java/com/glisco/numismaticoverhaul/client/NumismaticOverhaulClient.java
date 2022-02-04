package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.NumismaticOverhaulBlocks;
import com.glisco.numismaticoverhaul.client.entity.TaxCollectorEntityRenderer;
import com.glisco.numismaticoverhaul.client.gui.CurrencyTooltipComponent;
import com.glisco.numismaticoverhaul.client.gui.shop.ShopScreen;
import com.glisco.numismaticoverhaul.item.CurrencyTooltipData;
import com.glisco.numismaticoverhaul.item.NumismaticOverhaulItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class NumismaticOverhaulClient implements ClientModInitializer {

    public static final EntityModelLayer TAX_COLLECTOR_LAYER = new EntityModelLayer(NumismaticOverhaul.id("tax_collector"), "main");

    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(NumismaticOverhaul.SHOP_SCREEN_HANDLER_TYPE, ShopScreen::new);

        EntityModelLayerRegistry.registerModelLayer(TAX_COLLECTOR_LAYER, () ->
                TexturedModelData.of(VillagerResemblingModel.getModelData(), 64, 64));
        EntityRendererRegistry.register(NumismaticOverhaul.TAX_COLLECTOR, TaxCollectorEntityRenderer::new);

//        ModelLoadingRegistry.INSTANCE.registerResourceProvider(resourceManager -> new NumismaticModelProvider());

        FabricModelPredicateProviderRegistry.register(NumismaticOverhaulItems.BRONZE_COIN, new Identifier("coins"), (stack, world, entity, seed) -> stack.getCount() / 100.0f);
        FabricModelPredicateProviderRegistry.register(NumismaticOverhaulItems.SILVER_COIN, new Identifier("coins"), (stack, world, entity, seed) -> stack.getCount() / 100.0f);
        FabricModelPredicateProviderRegistry.register(NumismaticOverhaulItems.GOLD_COIN, new Identifier("coins"), (stack, world, entity, seed) -> stack.getCount() / 100.0f);

        FabricModelPredicateProviderRegistry.register(NumismaticOverhaulItems.MONEY_BAG, new Identifier("size"), (stack, world, entity, seed) -> {
            int[] values = NumismaticOverhaulItems.MONEY_BAG.getCombinedValue(stack);
            if (values[2] > 0) return 1;
            if (values[1] > 0) return .5f;

            return 0;
        });

        TooltipComponentCallback.EVENT.register(data -> {
            if (!(data instanceof CurrencyTooltipData currencyData)) return null;
            return new CurrencyTooltipComponent(currencyData);
        });

        BlockEntityRendererRegistry.register(NumismaticOverhaulBlocks.Entities.SHOP, ShopBlockEntityRender::new);
//        BlockRenderLayerMap.INSTANCE.putBlock(NumismaticOverhaulBlocks.COIN_STACK, RenderLayer.getCutout());
    }

}
