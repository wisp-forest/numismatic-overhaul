package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.network.SetShopOffersS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class NumismaticOverhaulClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(NumismaticOverhaul.SHOP_SCREEN_HANDLER_TYPE, ShopScreen::new);

        FabricModelPredicateProviderRegistry.register(NumismaticOverhaul.BRONZE_COIN, new Identifier("coins"), (stack, world, entity) -> stack.getCount() / 100.0f);
        FabricModelPredicateProviderRegistry.register(NumismaticOverhaul.SILVER_COIN, new Identifier("coins"), (stack, world, entity) -> stack.getCount() / 100.0f);
        FabricModelPredicateProviderRegistry.register(NumismaticOverhaul.GOLD_COIN, new Identifier("coins"), (stack, world, entity) -> stack.getCount() / 100.0f);

        ClientPlayNetworking.registerGlobalReceiver(SetShopOffersS2CPacket.ID, SetShopOffersS2CPacket::onPacket);
    }

}
