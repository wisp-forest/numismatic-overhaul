package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.NumismaticOverhaulBlocks;
import com.glisco.numismaticoverhaul.client.gui.CurrencyTooltipComponent;
import com.glisco.numismaticoverhaul.client.gui.PiggyBankScreen;
import com.glisco.numismaticoverhaul.client.gui.PurseLayerElement;
import com.glisco.numismaticoverhaul.client.gui.ShopScreen;
import com.glisco.numismaticoverhaul.item.CurrencyTooltipData;
import com.glisco.numismaticoverhaul.item.NumismaticOverhaulItems;
import com.glisco.numismaticoverhaul.mixin.LayerInstanceAccessor;
import io.wispforest.owo.mixin.ui.layers.HandledScreenAccessor;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.layers.Layers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.util.Identifier;

import java.util.List;

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

        BlockEntityRendererFactories.register(NumismaticOverhaulBlocks.Entities.SHOP, ShopBlockEntityRender::new);

        Layers.add(
                PurseLayerContainer::new,
                new PurseLayerElement<>((instance, component, xOffset, yOffset) -> {
                    instance.aggressivePositioning = true;
                    ((LayerInstanceAccessor) instance).numismatic$getLayoutUpdaters().add(() -> {
                        if (instance.screen.isInventoryTabSelected()) {
                            component.positioning(Positioning.absolute(
                                    ((HandledScreenAccessor) instance.screen).owo$getRootX() + NumismaticOverhaul.CONFIG.pursePositionCreativeX(),
                                    ((HandledScreenAccessor) instance.screen).owo$getRootY() + NumismaticOverhaul.CONFIG.pursePositionCreativeY()
                            ));
                        } else {
                            component.positioning(Positioning.absolute(-50, -50));
                        }
                    });
                }),
                CreativeInventoryScreen.class
        );

        Layers.add(
                PurseLayerContainer::new,
                new PurseLayerElement<>((instance, component, xOffset, yOffset) -> {
                    instance.aggressivePositioning = true;
                    ((LayerInstanceAccessor) instance).numismatic$getLayoutUpdaters().add(() -> {
                        var recipeBookButton = instance.queryWidget($ -> $ instanceof TexturedButtonWidget);
                        if (recipeBookButton == null) return;

                        component.positioning(
                                Positioning.absolute(recipeBookButton.getX() + xOffset + NumismaticOverhaul.CONFIG.pursePositionX(),
                                        recipeBookButton.getY() + yOffset + NumismaticOverhaul.CONFIG.pursePositionY())
                        );
                    });
                }),
                InventoryScreen.class
        );

        Layers.add(
                PurseLayerContainer::new,
                new PurseLayerElement<>((instance, component, xOffset, yOffset) -> instance.alignComponentToHandledScreenCoordinates(component, 232 + xOffset, 33 + yOffset)),
                MerchantScreen.class
        );
    }

    private static class PurseLayerContainer extends StackLayout {

        protected PurseLayerContainer(Sizing horizontalSizing, Sizing verticalSizing) {
            super(horizontalSizing, verticalSizing);
        }

        @Override
        protected void drawChildren(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta, List<Component> children) {
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 300);
            super.drawChildren(context, mouseX, mouseY, partialTicks, delta, children);
            context.getMatrices().pop();
        }
    }
}
