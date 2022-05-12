package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.client.gui.purse.PurseButton;
import com.glisco.numismaticoverhaul.client.gui.purse.PurseWidget;
import com.glisco.numismaticoverhaul.network.RequestPurseActionC2SPacket;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    PurseWidget purse;
    PurseButton button;

    //The purse is injected via mixin instead of event because I need special callbacks in render(...) and mouseClicked(...) to handle
    //the non-button widget anyway, so I can just inject them here

    @Inject(method = "init", at = @At("TAIL"))
    public void addButton(CallbackInfo ci) {
        purse = new PurseWidget(this.x + 129, this.y + 20, client, ModComponents.CURRENCY.get(client.player));

        button = new PurseButton(this.x + 158, this.y + 6, button -> {
            if (Screen.hasShiftDown()) {
                NumismaticOverhaul.CHANNEL.clientHandle().send(RequestPurseActionC2SPacket.storeAll());
            } else {
                purse.toggleActive();
            }
        }, client.player, this);

        this.addDrawableChild(button);
    }

    //Incredibly beautiful lambda mixin
    @Inject(method = "method_19891", at = @At("TAIL"))
    private void updateWidgetPosition(ButtonWidget button, CallbackInfo ci) {
        this.button.setPos(this.x + 158, this.y + 6);
        this.purse = new PurseWidget(this.x + 129, this.y + 20, client, ModComponents.CURRENCY.get(client.player));
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        purse.render(matrices, mouseX, mouseY, delta);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void onMouse(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (purse.mouseClicked(mouseX, mouseY, button)) cir.setReturnValue(true);
    }

    @Override
    protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
        if (purse.isMouseOver(x, y)) return;
        super.drawMouseoverTooltip(matrices, x, y);
    }

}
