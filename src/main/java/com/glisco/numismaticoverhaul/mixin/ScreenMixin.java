package com.glisco.numismaticoverhaul.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public class ScreenMixin {

    @Shadow
    @Nullable
    protected MinecraftClient client;

    @Inject(method = "renderOrderedTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    public void renderTooltip(MatrixStack matrices, List<? extends OrderedText> lines, int x, int y, CallbackInfo ci) {
        //RenderSystem.disableDepthTest();
        //MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(new ItemStack(NumismaticOverhaul.GOLD_COIN), x + 10, y + 10);
    }

}
