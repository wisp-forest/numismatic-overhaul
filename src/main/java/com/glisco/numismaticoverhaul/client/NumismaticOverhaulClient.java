package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.client.gui.CurrencyTooltipComponent;
import com.glisco.numismaticoverhaul.client.gui.shop.ShopScreen;
import com.glisco.numismaticoverhaul.item.CurrencyTooltipData;
import com.glisco.numismaticoverhaul.network.UpdateShopScreenS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class NumismaticOverhaulClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(NumismaticOverhaul.SHOP_SCREEN_HANDLER_TYPE, ShopScreen::new);

        FabricModelPredicateProviderRegistry.register(NumismaticOverhaul.BRONZE_COIN, new Identifier("coins"), (stack, world, entity, seed) -> stack.getCount() / 100.0f);
        FabricModelPredicateProviderRegistry.register(NumismaticOverhaul.SILVER_COIN, new Identifier("coins"), (stack, world, entity, seed) -> stack.getCount() / 100.0f);
        FabricModelPredicateProviderRegistry.register(NumismaticOverhaul.GOLD_COIN, new Identifier("coins"), (stack, world, entity, seed) -> stack.getCount() / 100.0f);

        FabricModelPredicateProviderRegistry.register(NumismaticOverhaul.MONEY_BAG, new Identifier("size"), (stack, world, entity, seed) -> {
            int value = NumismaticOverhaul.MONEY_BAG.getValue(stack);
            if (value > 9999) return 1;
            if (value > 99) return .5f;
            return 0;
        });

        TooltipComponentCallback.EVENT.register(data -> {
            if (!(data instanceof CurrencyTooltipData currencyData)) return null;
            return new CurrencyTooltipComponent(currencyData);
        });

        ClientPlayNetworking.registerGlobalReceiver(UpdateShopScreenS2CPacket.ID, UpdateShopScreenS2CPacket::onPacket);

        BlockEntityRendererRegistry.INSTANCE.register(NumismaticOverhaul.SHOP_BLOCK_ENTITY, ShopBlockEntityRender::new);
    }

}
