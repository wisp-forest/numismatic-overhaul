package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.villagers.data.VillagerTradesResourceListener;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.registry.DynamicRegistryManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerResourceManager.class)
public class ServerResourceManagerMixin {

    @Shadow
    @Final
    private ReloadableResourceManager resourceManager;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onReload(DynamicRegistryManager registryManager, CommandManager.RegistrationEnvironment commandEnvironment, int functionPermissionLevel, CallbackInfo ci) {
        resourceManager.registerReloader(new VillagerTradesResourceListener());
    }

}
