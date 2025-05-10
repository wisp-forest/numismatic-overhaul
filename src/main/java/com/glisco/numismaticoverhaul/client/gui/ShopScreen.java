package com.glisco.numismaticoverhaul.client.gui;

import com.glisco.numismaticoverhaul.block.ShopOffer;
import com.glisco.numismaticoverhaul.block.ShopScreenHandler;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.network.UpdateShopScreenS2CPacket;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.*;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.UISounds;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import java.util.*;
import java.util.function.Consumer;

import static com.glisco.numismaticoverhaul.NumismaticOverhaul.id;
import static io.wispforest.owo.ui.container.Containers.*;

public class ShopScreen extends BaseOwoHandledScreen<FlowLayout, ShopScreenHandler> {

    public static final Identifier TEXTURE_PNG = id("textures/gui/shop_gui.png");
    public static final Identifier TRADES_TEXTURE = id("textures/gui/shop_gui_trades.png");

    private final List<ButtonWidget> tabButtons = new ArrayList<>();
    private final List<ShopOffer> offers = new ArrayList<>();

    private Runnable afterDataUpdate = () -> {
    };
    private Consumer<String> priceDisplay = s -> {
    };
    private int tab = 0;

    public ShopScreen(ShopScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.playerInventoryTitleY += 1;
        this.titleY = 5;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, (sizing, sizing2) -> {
            var root = verticalFlow(sizing, sizing2);
            root.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
            root.surface(Surface.VANILLA_TRANSLUCENT);
            return root;
        });
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        this.tabButtons.clear();
        // Main shop screen
        rootComponent
            .child(
                horizontalFlow(Sizing.content(), Sizing.content())
                    .children(List.of(
                            verticalFlow(Sizing.fixed(120), Sizing.content())
                                .children(List.of(
                                    makeTabButton(Items.CHEST, false, b -> this.selectTab(0)),
                                    makeTabButton(Items.EMERALD, true, b -> this.selectTab(1))
                                ))
                                .horizontalAlignment(HorizontalAlignment.RIGHT)
                                .padding(Insets.top(5))
                                .allowOverflow(true)
                                .id("left-column"),
                            stack(Sizing.content(), Sizing.content())
                                .child(createBackgroundTexture(TEXTURE_PNG)
                                    .id("background-texture"))
                                .id("background"),
                            verticalFlow(Sizing.fixed(120), Sizing.content())
                                .child(makeCurrencyWidget(button -> this.handler.extractCurrency()))
                                .child(stack(Sizing.content(), Sizing.content())
                                    .child(Components.item(Items.HOPPER.getDefaultStack())
                                        .margins(Insets.of(6))
                                    )
                                    .child(Components.label(Text.empty())
                                        .shadow(true)
                                        .positioning(Positioning.absolute(15, 15))
                                        .zIndex(150)
                                        .id("transfer-label")
                                    )
                                    .child(verticalFlow(Sizing.fixed(28), Sizing.fixed(28))
                                        .cursorStyle(CursorStyle.HAND)
                                        .positioning(Positioning.absolute(0, 0))
                                        .id("transfer-button")
                                    )
                                    .margins(Insets.top(3))
                                    .surface(Surface.PANEL)
                                )
                                .horizontalAlignment(HorizontalAlignment.LEFT)
                                .padding(Insets.left(2))
                                .id("right-column")
                        )
                    )
            );

        // Utility
        rootComponent.childById(FlowLayout.class, "transfer-button").mouseDown().subscribe((x, y, button) -> {
            if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
            this.handler.toggleTransfer();
            UISounds.playInteractionSound();
            return true;
        });
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        var buffer = this.component(ItemComponent.class, "trade-buffer");
        if (buffer != null && buffer.isInBoundingBox(mouseX, mouseY)) return false;

        return super.isClickOutsideBounds(mouseX, mouseY, left, top, button);
    }

    public void update(UpdateShopScreenS2CPacket data) {
        if (this.uiAdapter == null) return;

        long[] storedCurrency = CurrencyResolver.splitValues(data.storedCurrency());
        this.component(LabelComponent.class, "bronze-count").text(Text.literal(String.valueOf(storedCurrency[0])));
        this.component(LabelComponent.class, "silver-count").text(Text.literal(String.valueOf(storedCurrency[1])));
        this.component(LabelComponent.class, "gold-count").text(Text.literal(String.valueOf(storedCurrency[2])));

        int prevOffers = this.offers.size();
        this.offers.clear();
        this.offers.addAll(data.offers());

        if (this.tab == 1) this.populateTrades(this.tab);

        if (this.tab == 1 && this.offers.size() > prevOffers) {
            var offersScroll = this.component(ScrollContainer.class, "offer-container");
            var leftColumn = offersScroll.childById(FlowLayout.class, "first-trades-column");

            offersScroll.scrollTo(leftColumn.children().getLast());
        }

        this.component(FlowLayout.class, "transfer-button").tooltip(
            data.transferEnabled()
                ? Text.translatable("gui.numismatic-overhaul.shop.transfer_tooltip.enabled")
                : Text.translatable("gui.numismatic-overhaul.shop.transfer_tooltip.disabled")
        );
        this.component(LabelComponent.class, "transfer-label").text(
            data.transferEnabled()
                ? TextOps.withColor("✔", 0x28FFBF)
                : TextOps.withColor("✘", 0xEB1D36)
        );

        this.afterDataUpdate();
    }

    private Text computeTransferTooltip(boolean isTransferEnabled) {
        return isTransferEnabled ? Text.translatable("gui.numismatic-overhaul.shop.transfer_tooltip.enabled") : Text.translatable("gui.numismatic-overhaul.shop.transfer_tooltip.disabled");
    }

    public void afterDataUpdate() {
        this.afterDataUpdate.run();
    }

    private void selectTab(int index) {
        if (this.tab == index) return;

        if (index == 0) {
            this.swapBackgroundTexture(TEXTURE_PNG);
            this.titleY = 5;

            this.component(FlowLayout.class, "right-column").removeChild(this.component(FlowLayout.class, "trade-edit-widget"));
            this.afterDataUpdate = () -> {
            };
            this.priceDisplay = s -> {
            };
        } else {
            this.swapBackgroundTexture(TRADES_TEXTURE);
            this.titleY = 69420;

            this.uiAdapter.rootComponent.childById(StackLayout.class, "background")
                .child(
                    verticalScroll(Sizing.fixed(160), Sizing.fixed(60),
                        horizontalFlow(Sizing.content(), Sizing.content())
                            .child(verticalFlow(Sizing.content(), Sizing.content())
                                .children(List.of())
                                .id("first-trades-column"))
                            .child(verticalFlow(Sizing.content(), Sizing.content())
                                .children(List.of())
                                .id("second-trades-column")
                                .margins(Insets.left(4))
                            )
                    )
                        .positioning(Positioning.absolute(8, 10))
                        .id("offer-container")
                );

            final var editWidget = makeTradeEditWidget(this.handler.getBufferStack());
            var submitButton = editWidget.childById(ButtonComponent.class, "submit-button");
            var deleteButton = editWidget.childById(ButtonComponent.class, "delete-button");

            var tradeBuffer = editWidget.childById(ItemComponent.class, "trade-buffer");
            tradeBuffer.showOverlay(true);
            tradeBuffer.mouseDown().subscribe((mouseX, mouseY, button) -> {
                this.handler.handleBufferClick();
                return true;
            });

            var priceField = editWidget.childById(TextFieldWidget.class, "price-field");
            priceField.setMaxLength(7);
            priceField.setTextPredicate(s -> s.matches("\\d*"));
            priceField.setChangedListener(s -> {
                this.afterDataUpdate();

                var price = CurrencyResolver.splitValues(s.isBlank() ? 0 : Integer.parseInt(s));
                this.component(LabelComponent.class, "offer-bronze-count").text(Text.literal(String.valueOf(price[0])));
                this.component(LabelComponent.class, "offer-silver-count").text(Text.literal(String.valueOf(price[1])));
                this.component(LabelComponent.class, "offer-gold-count").text(Text.literal(String.valueOf(price[2])));
            });

            submitButton.onPress((ButtonComponent button) -> this.handler.createOffer(Integer.parseInt(priceField.getText())));
            deleteButton.onPress((ButtonComponent button) -> this.handler.deleteOffer());

            this.priceDisplay = priceField::setText;
            this.afterDataUpdate = () -> {
                var priceText = priceField.getText();
                var bufferStack = this.handler.getBufferStack();
                boolean hasOffer = this.hasOfferFor(bufferStack);

                submitButton.active = !priceText.isBlank()
                    && Integer.parseInt(priceText) > 0
                    && !bufferStack.isEmpty()
                    && (this.offers.size() < 24 || hasOffer);
                deleteButton.active = hasOffer;

                tradeBuffer.stack(bufferStack);
                if (!bufferStack.isEmpty()) {
                    var tooltip = new ArrayList<TooltipComponent>();
                    var client = MinecraftClient.getInstance();
                    bufferStack.getTooltip(Item.TooltipContext.create(client.world), client.player, client.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC)
                        .stream()
                        .map(Text::asOrderedText)
                        .map(TooltipComponent::of)
                        .forEach(tooltip::add);
                    bufferStack.getTooltipData().ifPresent(data -> {
                        var fabricComponent = TooltipComponentCallback.EVENT.invoker().getComponent(data);
                        tooltip.add(1, Objects.requireNonNullElseGet(fabricComponent, () -> TooltipComponent.of(data)));
                    });
                    tradeBuffer.tooltip(tooltip);
                } else {
                    tradeBuffer.tooltip((List<TooltipComponent>) null);
                }
            };

            this.component(FlowLayout.class, "right-column").child(0, editWidget);
        }

        this.populateTrades(index);
        for (int i = 0; i < this.tabButtons.size(); i++) {
            this.tabButtons.get(i).active = i != index;
        }

        this.tab = index;
    }

    private boolean hasOfferFor(ItemStack stack) {
        return this.offers.stream().anyMatch(offer -> ItemStack.areItemsEqual(stack, offer.getSellStack()));
    }

    private void populateTrades(int tab) {
        var firstColumn = this.component(FlowLayout.class, "first-trades-column");
        var secondColumn = this.component(FlowLayout.class, "second-trades-column");

        firstColumn.clearChildren();
        secondColumn.clearChildren();

        if (tab == 0) return;

        for (int i = 0; i < this.offers.size(); i++) {
            var offer = this.offers.get(i);

            var tradeComponent = makeTradeButton(offer.getSellStack(), offer.getPrice(), i);
            (i % 2 == 0 ? firstColumn : secondColumn).child(tradeComponent);
        }
    }

    private void swapBackgroundTexture(Identifier newTexture) {
        this.uiAdapter.rootComponent.childById(TextureComponent.class, "background-texture").remove();
        this.uiAdapter.rootComponent.childById(StackLayout.class, "background").child(createBackgroundTexture(newTexture));
    }

    private TextureComponent createBackgroundTexture(Identifier id) {
        var bg = Components.texture(id, 0, 0, 176, 168);
        bg.id("background-texture");
        return bg;
    }

    private StackLayout makeTabButton(Item icon, boolean active, Consumer<ButtonComponent> onPress) {
        var buttonLayout = stack(Sizing.content(), Sizing.content());
        buttonLayout
            .child(
                Components.item(icon.getDefaultStack()).positioning(Positioning.absolute(9, 6)))
            .child(Components.button(Text.empty(), onPress)
                .active(active)
                .renderer(ButtonComponent.Renderer.texture(TEXTURE_PNG, 113, 168, 256, 256))
                .sizing(Sizing.fixed(32), Sizing.fixed(28))
                .margins(Insets.right(-3))
                .id("tab-button")
            )
            .margins(Insets.bottom(4))
            .allowOverflow(true);

        var button = buttonLayout.childById(ButtonComponent.class, "tab-button");
        this.tabButtons.add(button);

        return buttonLayout;
    }

    private StackLayout makeCurrencyWidget(Consumer<ButtonComponent> onPress) {
        var currencyComponent = stack(Sizing.content(), Sizing.content());
        currencyComponent
            .child(Components.texture(TEXTURE_PNG, 146, 169, 34, 54))
            .child(Components.label(Text.literal("0")).positioning(Positioning.absolute(5, 7)).id("gold-count"))
            .child(Components.label(Text.literal("0")).positioning(Positioning.absolute(5, 19)).id("silver-count"))
            .child(Components.label(Text.literal("0")).positioning(Positioning.absolute(5, 31)).id("bronze-count"))
            .child(Components.button(Text.empty(), onPress)
                .renderer(ButtonComponent.Renderer.texture(TEXTURE_PNG, 146, 224, 256, 256))
                .sizing(Sizing.fixed(26), Sizing.fixed(8))
                .positioning(Positioning.absolute(4, 41))
            );
        return currencyComponent;
    }

    private FlowLayout makeTradeButton(ItemStack tradeItem, long price, int offerIndex) {
        var tradeButton = horizontalFlow(Sizing.content(), Sizing.content());
        tradeButton
            .child(Components.button(Text.empty(), button -> {
                        this.handler.loadOffer(offerIndex);
                        this.priceDisplay.accept(String.valueOf(price));
                    })
                    .sizing(Sizing.fixed(78), Sizing.content())
                    .id("trade-button")
            )
            .child(horizontalFlow(Sizing.content(), Sizing.fixed(20))
                .children(List.of(
                    Components.item(tradeItem)
                        .showOverlay(true)
                        .cursorStyle(CursorStyle.HAND)
                        .id("item-display")
                    ,
                    Components.texture(TEXTURE_PNG, 1, 172, 5, 7).margins(Insets.left(3)),
                    Components.label(Text.literal(String.valueOf(price)))
                        .shadow(true)
                        .cursorStyle(CursorStyle.HAND)
                        .margins(Insets.left(2))
                        .id("price-label")
                ))
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.left(4))
                .positioning(Positioning.absolute(0, 0))
            );
        return tradeButton;
    }

    private FlowLayout makeTradeEditWidget(ItemStack tradeStack) {
        var editorWidget = horizontalFlow(Sizing.content(), Sizing.content());
        var priceFieldComponent = Components.textBox(Sizing.fixed(47));
        priceFieldComponent
            .verticalSizing(Sizing.fixed(11))
            .positioning(Positioning.absolute(35, 18))
            .id("price-field");
        priceFieldComponent.setDrawsBackground(false);
        editorWidget.children(List.of(
            Components.texture(TEXTURE_PNG, 15, 169, 98, 54),
            new FakeSlotComponent(tradeStack)
                .showOverlay(true)
                .positioning(Positioning.absolute(8, 15))
                .id("trade-buffer"),
            Components.button(Text.empty(), buttonComponent -> {
                })
                .active(false)
                .renderer(ButtonComponent.Renderer.texture(TEXTURE_PNG, 15, 223, 256, 256))
                .sizing(Sizing.fixed(41), Sizing.fixed(11))
                .positioning(Positioning.absolute(7, 36))
                .id("submit-button"),
            Components.button(Text.empty(), buttonComponent -> {
                })
                .renderer(ButtonComponent.Renderer.texture(TEXTURE_PNG, 56, 223, 256, 256))
                .active(false)
                .sizing(Sizing.fixed(41), Sizing.fixed(11))
                .positioning(Positioning.absolute(50, 36))
                .id("delete-button"),
            priceFieldComponent,
            horizontalFlow(Sizing.content(), Sizing.content())
                .children(List.of(
                    Components.label(Text.literal("0"))
                        .color(Color.ofRgb(0x898989))
                        .horizontalSizing(Sizing.fixed(12))
                        .id("offer-bronze-count"),
                    Components.label(Text.literal("0"))
                        .color(Color.ofRgb(0x898989))
                        .margins(Insets.left(8))
                        .horizontalSizing(Sizing.fixed(12))
                        .id("offer-silver-count"),
                    Components.label(Text.literal("0"))
                        .color(Color.ofRgb(0x898989))
                        .margins(Insets.left(8))
                        .horizontalSizing(Sizing.fixed(18))
                        .id("offer-gold-count")
                ))
                .positioning(Positioning.absolute(36, 5))
        ));
        editorWidget.margins(Insets.bottom(3));
        editorWidget.id("trade-edit-widget");
        return editorWidget;
    }

    public int tab() {
        return this.tab;
    }

    public static class FakeSlotComponent extends ItemComponent {

        protected FakeSlotComponent(ItemStack stack) {
            super(stack);
        }

        @Override
        public boolean shouldDrawTooltip(double mouseX, double mouseY) {
            var player = MinecraftClient.getInstance().player;
            if (player == null) return false;
            return (player.currentScreenHandler == null || player.currentScreenHandler.getCursorStack().isEmpty()) && super.shouldDrawTooltip(mouseX, mouseY);
        }
    }

    static {
        UIParsing.registerFactory(id("fake-slot"), element -> new FakeSlotComponent(ItemStack.EMPTY));
    }
}
