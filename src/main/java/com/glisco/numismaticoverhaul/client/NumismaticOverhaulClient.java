package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.NumismaticOverhaulBlocks;
import com.glisco.numismaticoverhaul.client.gui.CurrencyTooltipComponent;
import com.glisco.numismaticoverhaul.client.gui.PiggyBankScreen;
import com.glisco.numismaticoverhaul.client.gui.ShopScreen;
import com.glisco.numismaticoverhaul.item.CurrencyTooltipData;
import com.glisco.numismaticoverhaul.item.NumismaticOverhaulItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class NumismaticOverhaulClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(NumismaticOverhaul.SHOP_SCREEN_HANDLER_TYPE, ShopScreen::new);
        HandledScreens.register(NumismaticOverhaul.PIGGY_BANK_SCREEN_HANDLER_TYPE, PiggyBankScreen::new);

        ModelPredicateProviderRegistry.register(NumismaticOverhaulItems.BRONZE_COIN, new Identifier("coins"), (stack, world, entity, seed) -> stack.getCount() / 100.0f);
        ModelPredicateProviderRegistry.register(NumismaticOverhaulItems.SILVER_COIN, new Identifier("coins"), (stack, world, entity, seed) -> stack.getCount() / 100.0f);
        ModelPredicateProviderRegistry.register(NumismaticOverhaulItems.GOLD_COIN, new Identifier("coins"), (stack, world, entity, seed) -> stack.getCount() / 100.0f);

        ModelPredicateProviderRegistry.register(NumismaticOverhaulItems.MONEY_BAG, new Identifier("size"), (stack, world, entity, seed) -> {
            long[] values = NumismaticOverhaulItems.MONEY_BAG.getCombinedValue(stack);
            if (values.length < 3) return 0;

            if (values[2] > 0) return 1;
            if (values[1] > 0) return .5f;

            return 0;
        });

        TooltipComponentCallback.EVENT.register(data -> {
            if (!(data instanceof CurrencyTooltipData currencyData)) return null;
            return new CurrencyTooltipComponent(currencyData);
        });

        BlockEntityRendererRegistry.register(NumismaticOverhaulBlocks.Entities.SHOP, ShopBlockEntityRender::new);
    }

}
