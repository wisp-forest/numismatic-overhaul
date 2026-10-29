package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "vanishCursedItems", at = @At("TAIL"))
    public void onServerDeath(CallbackInfo ci) {
        var player = (PlayerEntity) (Object) this;

        final var world = player.getWorld();
        if (world.isClient) return;

        final var component = ModComponents.CURRENCY.get(player);
        long currentBalance = component.getValue();
        if (currentBalance <= 0) return;

        var dropPercentage = world.getGameRules().get(NumismaticOverhaul.MONEY_DROP_PERCENTAGE).get() * .01f;
        long dropped = Math.max(0L, (long) (currentBalance * dropPercentage));
        dropped = Math.min(dropped, currentBalance);

        if (dropped <= 0) return;

        var stacksDropped = CurrencyHelper.getAsStacks(dropped, 5);
        for (var drop : stacksDropped) {
            player.dropItem(drop, true, false);
        }

        component.modify(-dropped);
    }

}
