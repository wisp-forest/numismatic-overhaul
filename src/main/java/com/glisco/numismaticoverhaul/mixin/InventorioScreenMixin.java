package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.client.gui.purse.PurseButton;
import com.glisco.numismaticoverhaul.client.gui.purse.PurseWidget;
import com.glisco.numismaticoverhaul.network.RequestPurseActionC2SPacket;
import me.lizardofoz.inventorio.client.ui.InventorioScreen;
import me.lizardofoz.inventorio.player.InventorioScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "me.lizardofoz.inventorio.client.ui.InventorioScreen")
public abstract class InventorioScreenMixin extends AbstractInventoryScreen<InventorioScreenHandler> {

         public InventorioScreenMixin(InventorioScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
            super(screenHandler, playerInventory, text);
        }

        PurseWidget purse;
        PurseButton button;

        //The purse is injected via mixin instead of event because I need special callbacks in render(...) and mouseClicked(...) to handle
        //the non-button widget anyways, so I can just inject them here

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

        // Incredibly cursed lambda mixin
        @Inject(target = @Desc(value = "init$lambda-0", args={InventorioScreen.class, ButtonWidget.class}), at = @At("TAIL"))
        private static void updateWidgetPosition(@Coerce Screen screen, ButtonWidget button, CallbackInfo ci) {

            var clazz= screen.getClass();

            try {
                var fieldX = HandledScreen.class.getDeclaredField("x");
                var fieldY = HandledScreen.class.getDeclaredField("y");
                fieldX.setAccessible(true);
                fieldY.setAccessible(true);

                int x = fieldX.getInt(screen);
                int y = fieldY.getInt(screen);

                var fieldButton = clazz.getDeclaredField("button");
                var fieldPurse = clazz.getDeclaredField("purse");
                fieldButton.setAccessible(true);
                fieldPurse.setAccessible(true);

                PurseButton cursedButton = (PurseButton) fieldButton.get(screen);
                fieldPurse.set(screen, (new PurseWidget(x + 129, y + 20,
                        MinecraftClient.getInstance(), ModComponents.CURRENCY.get(MinecraftClient.getInstance().player))));

                cursedButton.setPos(x + 158, y + 6);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }



        }

        @Inject(method = "render", at = @At("TAIL"))
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
            purse.render(matrices, mouseX, mouseY, delta);
//            button.render(matrices, mouseX, mouseY, delta);
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
