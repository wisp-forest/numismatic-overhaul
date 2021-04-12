package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.villagers.VillagerTradesHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Inject(method = "onPlayerConnected", at = @At("TAIL"))
    public void playerConnect(ServerPlayerEntity player, CallbackInfo ci) {
        VillagerTradesHandler.broadcastErrors(Collections.singletonList(player));
    }

}
