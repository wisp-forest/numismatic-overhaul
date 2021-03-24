package com.glisco.numismaticoverhaul.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    //Another :concern: mixin to allow creative cheating of items with stack sizes of 64
    @SuppressWarnings("SameReturnValue")
    @ModifyConstant(method = "onCreativeInventoryAction", constant = @Constant(intValue = 64))
    public int increaseStackLimit(int originalValue) {
        return 99;
    }

}
