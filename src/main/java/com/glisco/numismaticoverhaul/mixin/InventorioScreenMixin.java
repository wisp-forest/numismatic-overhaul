package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.client.gui.purse.PurseButton;
import com.glisco.numismaticoverhaul.client.gui.purse.PurseWidget;
import com.glisco.numismaticoverhaul.network.RequestPurseActionC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@SuppressWarnings("UnresolvedMixinReference")
@Mixin(targets = "me.lizardofoz.inventorio.client.ui.InventorioScreen")
public abstract class InventorioScreenMixin extends AbstractInventoryScreen<ScreenHandler> {

    private InventorioScreenMixin(ScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    public PurseWidget purse;
    public PurseButton button;

    //The purse is injected via mixin instead of event because I need special callbacks in render(...) and mouseClicked(...) to handle
    //the non-button widget anyway, so I can just inject them here

    @Inject(method = "method_25426", at = @At(value = "FIELD", target = "me.lizardofoz.inventorio.client.ui.InventorioScreen.open:Z", opcode = Opcodes.PUTFIELD), remap = false)
    public void addButton(CallbackInfo ci) {
        purse = new PurseWidget(this.x + 134, this.y + 20, client, ModComponents.CURRENCY.get(client.player));

        button = new PurseButton(this.x + 163, this.y + 6, button -> {
            if (Screen.hasShiftDown()) {
                NumismaticOverhaul.CHANNEL.clientHandle().send(RequestPurseActionC2SPacket.storeAll());
            } else {
                purse.toggleActive();
            }
        }, client.player, this);

        this.addDrawableChild(button);
    }

    // it used to be a lambda mixin, now it's French bread
    @Inject(method = "findLeftEdge", at = @At("TAIL"), remap = false)
    private void updateWidgetPosition(RecipeBookWidget recipeBook, int width, int parentWidth, CallbackInfoReturnable<Integer> ci) {
        final int x = ci.getReturnValueI();
        purse = new PurseWidget(x + 134, y + 20, MinecraftClient.getInstance(), ModComponents.CURRENCY.get(MinecraftClient.getInstance().player));
        button.setPosition(x + 163, y + 6);
    }

    @Inject(method = "method_25394", at = @At("TAIL"), remap = false)
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        purse.render(context, mouseX, mouseY, delta);
    }

    @Inject(method = "method_25402", at = @At("HEAD"), cancellable = true, remap = false)
    public void onMouse(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (purse.mouseClicked(mouseX, mouseY, button)) cir.setReturnValue(true);
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
        if (purse.isMouseOver(x, y)) return;
        super.drawMouseoverTooltip(context, x, y);
    }
}
