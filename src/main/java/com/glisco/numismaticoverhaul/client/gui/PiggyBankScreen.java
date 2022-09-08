package com.glisco.numismaticoverhaul.client.gui;

import com.glisco.numismaticoverhaul.block.PiggyBankScreenHandler;
import io.wispforest.owo.ui.base.BaseUIModelHandledScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class PiggyBankScreen extends BaseUIModelHandledScreen<FlowLayout, PiggyBankScreenHandler> {

    private TextureComponent bronzeHint, silverHint, goldHint;

    public PiggyBankScreen(PiggyBankScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title, FlowLayout.class, BaseUIModelScreen.DataSource.file("../src/main/resources/assets/numismatic-overhaul/owo_ui/piggy_bank.xml"));
        this.backgroundHeight = 145;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
        this.titleX = (this.backgroundWidth - MinecraftClient.getInstance().textRenderer.getWidth(title)) / 2;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        this.bronzeHint = this.uiAdapter.rootComponent.childById(TextureComponent.class, "bronze-hint");
        this.silverHint = this.uiAdapter.rootComponent.childById(TextureComponent.class, "silver-hint");
        this.goldHint = this.uiAdapter.rootComponent.childById(TextureComponent.class, "gold-hint");
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        this.bronzeHint.sizing(this.handler.getSlot(0).hasStack() ? Sizing.fixed(0) : Sizing.fixed(16));
        this.silverHint.sizing(this.handler.getSlot(1).hasStack() ? Sizing.fixed(0) : Sizing.fixed(16));
        this.goldHint.sizing(this.handler.getSlot(2).hasStack() ? Sizing.fixed(0) : Sizing.fixed(16));
    }
}
