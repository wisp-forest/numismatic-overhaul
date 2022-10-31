package com.glisco.numismaticoverhaul.client.gui;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.ShopOffer;
import com.glisco.numismaticoverhaul.block.ShopScreenHandler;
import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import com.glisco.numismaticoverhaul.network.UpdateShopScreenS2CPacket;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.base.BaseUIModelHandledScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ShopScreen extends BaseUIModelHandledScreen<FlowLayout, ShopScreenHandler> {

    public static final Identifier TEXTURE = NumismaticOverhaul.id("textures/gui/shop_gui.png");
    public static final Identifier TRADES_TEXTURE = NumismaticOverhaul.id("textures/gui/shop_gui_trades.png");

    private final List<ButtonWidget> tabButtons = new ArrayList<>();
    private final List<ShopOffer> offers = new ArrayList<>();

    private Runnable afterDataUpdate = () -> {};
    private Consumer<String> priceDisplay = s -> {};
    private int tab = 0;

    public ShopScreen(ShopScreenHandler handler, PlayerInventory inventory, Text title) {
//        super(handler, inventory, title, FlowLayout.class, BaseUIModelScreen.DataSource.file("../src/main/resources/assets/numismatic-overhaul/owo_ui/shop.xml"));
        super(handler, inventory, title, FlowLayout.class, BaseUIModelScreen.DataSource.asset(NumismaticOverhaul.id("shop")));
        this.playerInventoryTitleY += 1;
        this.titleY = 5;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        this.tabButtons.clear();

        var leftColumn = rootComponent.childById(FlowLayout.class, "left-column");
        leftColumn.child(makeTabButton(Items.CHEST, false, button -> selectTab(0)));
        leftColumn.child(makeTabButton(Items.EMERALD, true, button -> this.selectTab(1)));

        rootComponent.childById(ButtonWidget.class, "extract-button").onPress(button -> this.handler.extractCurrency());

        rootComponent.childById(FlowLayout.class, "transfer-button").mouseDown().subscribe((x, y, button) -> {
            if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

            this.handler.toggleTransfer();
            UISounds.playInteractionSound();
            return true;
        });
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
        this.populateTrades(this.tab);

        if (this.tab == 1 && this.offers.size() > prevOffers) {
            var offersScroll = this.component(ScrollContainer.class, "offer-container");
            var leftColumn = offersScroll.childById(FlowLayout.class, "first-trades-column");

            offersScroll.scrollTo(leftColumn.children().get(leftColumn.children().size() - 1));
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

    public void afterDataUpdate() {
        this.afterDataUpdate.run();
    }

    private void selectTab(int index) {
        if (this.tab == index) return;

        if (index == 0) {
            this.swapBackgroundTexture(TEXTURE);
            this.titleY = 5;

            this.component(FlowLayout.class, "right-column").removeChild(this.component(FlowLayout.class, "trade-edit-widget"));
            this.afterDataUpdate = () -> {};
            this.priceDisplay = s -> {};
        } else {
            this.swapBackgroundTexture(TRADES_TEXTURE);
            this.titleY = 69420;

            final var editWidget = this.model.expandTemplate(FlowLayout.class, "trade-edit-widget", Map.of());
            var submitButton = editWidget.childById(ButtonComponent.class, "submit-button");
            var deleteButton = editWidget.childById(ButtonComponent.class, "delete-button");

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
                boolean hasOffer = this.hasOfferFor(this.handler.getBufferStack());

                submitButton.active = !priceText.isBlank()
                        && Integer.parseInt(priceText) > 0
                        && !this.handler.getBufferStack().isEmpty()
                        && (this.offers.size() < 24 || hasOffer);
                deleteButton.active = hasOffer;
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
        return this.offers.stream().anyMatch(offer -> ItemStack.areEqual(stack, offer.getSellStack()));
    }

    private void populateTrades(int tab) {
        var firstColumn = this.component(FlowLayout.class, "first-trades-column");
        var secondColumn = this.component(FlowLayout.class, "second-trades-column");

        firstColumn.clearChildren();
        secondColumn.clearChildren();

        if (tab == 0) return;

        for (int i = 0; i < this.offers.size(); i++) {
            final int offerIndex = i;
            var offer = this.offers.get(offerIndex);

            var component = this.model.expandTemplate(FlowLayout.class, "trade-button", Map.of("price", String.valueOf(offer.getPrice())));
            component.childById(ItemComponent.class, "item-display").stack(offer.getSellStack());
            component.childById(ButtonWidget.class, "trade-button").onPress(button -> {
                this.handler.loadOffer(offerIndex);
                this.priceDisplay.accept(String.valueOf(offer.getPrice()));
            });

            (i % 2 == 0 ? firstColumn : secondColumn).child(component);
        }
    }

    private void swapBackgroundTexture(Identifier newTexture) {
        final var background = this.component(FlowLayout.class, "background");
        background.removeChild(background.children().get(0));
        background.child(0, this.model.expandTemplate(TextureComponent.class, "background-texture", Map.of("texture", newTexture.toString())));
    }

    private FlowLayout makeTabButton(Item icon, boolean active, ButtonWidget.PressAction onPress) {
        var buttonContainer = this.model.expandTemplate(FlowLayout.class, "tab-button", Map.of("icon-item", Registry.ITEM.getId(icon).toString()));

        final var button = buttonContainer.childById(ButtonWidget.class, "tab-button");
        this.tabButtons.add(button);

        button.active = active;
        button.onPress(onPress);

        return buttonContainer;
    }

    private <C extends Component> C component(Class<C> componentClass, String id) {
        return this.uiAdapter.rootComponent.childById(componentClass, id);
    }

    public int tab() {
        return this.tab;
    }
}
