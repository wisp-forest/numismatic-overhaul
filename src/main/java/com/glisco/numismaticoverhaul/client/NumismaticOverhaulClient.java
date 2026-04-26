package com.glisco.numismaticoverhaul.client;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.NumismaticOverhaulBlocks;
import com.glisco.numismaticoverhaul.client.gui.*;
import com.glisco.numismaticoverhaul.item.CurrencyTooltipData;
import com.glisco.numismaticoverhaul.item.NumismaticOverhaulItems;
import com.glisco.numismaticoverhaul.mixin.LayerInstanceAccessor;
import io.wispforest.owo.mixin.ui.layers.HandledScreenAccessor;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.layers.Layers;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import java.util.*;

@Environment(EnvType.CLIENT)
public class NumismaticOverhaulClient implements ClientModInitializer {

    public static final EntityModelLayer PIGGY_BANK = new EntityModelLayer(NumismaticOverhaul.id("piggy_bank"), "main");
    public static final SpriteIdentifier PIGGY_BANK_TEXTURE_ID = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, NumismaticOverhaul.id("block/piggy_bank/base"));
    public static final Map<DyeColor, SpriteIdentifier> COLORED_PIGGY_BANKS = new HashMap<>();

    @Override
    public void onInitializeClient() {
        generateColoredPiggies();
        HandledScreens.register(NumismaticOverhaul.SHOP_SCREEN_HANDLER_TYPE, ShopScreen::new);
        HandledScreens.register(NumismaticOverhaul.PAWN_SHOP_SCREEN_HANDLER_TYPE, PawnShopScreen::new);
        HandledScreens.register(NumismaticOverhaul.PIGGY_BANK_SCREEN_HANDLER_TYPE, PiggyBankScreen::new);

        FakeSlotComponent.init();

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
        BlockEntityRendererFactories.register(NumismaticOverhaulBlocks.Entities.PAWN_SHOP, PawnShopBlockEntityRender::new);

        EntityModelLayerRegistry.registerModelLayer(PIGGY_BANK, PiggyBankBlockEntityRenderer::createModelData);
        BlockEntityRendererFactories.register(NumismaticOverhaulBlocks.Entities.PIGGY_BANK, PiggyBankBlockEntityRenderer::new);

        Layers.add(
                PurseLayerContainer::new,
                new PurseLayerElement<>((instance, component) -> {
                    instance.aggressivePositioning = true;
                    ((LayerInstanceAccessor) instance).numismatic$getLayoutUpdaters().add(() -> {
                        if (instance.screen.isInventoryTabSelected()) {
                            component.positioning(Positioning.absolute(
                                    ((HandledScreenAccessor) instance.screen).owo$getRootX() + 38 + NumismaticOverhaul.CONFIG.purseOffsets.creativeX() ,
                                    ((HandledScreenAccessor) instance.screen).owo$getRootY() + 4 + NumismaticOverhaul.CONFIG.purseOffsets.creativeY()
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
                new PurseLayerElement<>((instance, component) -> {
                    instance.aggressivePositioning = true;
                    instance.alignComponentToHandledScreenCoordinates(
                            component,
                            160 + NumismaticOverhaul.CONFIG.purseOffsets.survivalX(),
                            5 + NumismaticOverhaul.CONFIG.purseOffsets.survivalY()
                    );
                }),
                InventoryScreen.class
        );

        Layers.add(
                PurseLayerContainer::new,
                new PurseLayerElement<>((instance, component) -> instance.alignComponentToHandledScreenCoordinates(
                        component,
                        260 + NumismaticOverhaul.CONFIG.purseOffsets.merchantX(),
                        5 + NumismaticOverhaul.CONFIG.purseOffsets.merchantY()
                )),
                MerchantScreen.class
        );
        Layers.add(
            PurseLayerContainer::new,
            new PurseLayerElement<>((instance, component) -> instance.alignComponentToHandledScreenCoordinates(
                component, NumismaticOverhaul.CONFIG.purseOffsets.pawnShopX(),
                70 + NumismaticOverhaul.CONFIG.purseOffsets.pawnShopY()
            )),
            PawnShopScreen.class
        );
    }

    private void generateColoredPiggies() {
        Arrays.stream(DyeColor.values()).forEach(dyeColor -> {
            var color = dyeColor.asString().toLowerCase(Locale.ROOT);
            COLORED_PIGGY_BANKS.put(dyeColor, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, NumismaticOverhaul.id("block/piggy_bank/" + color)));
        });
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
