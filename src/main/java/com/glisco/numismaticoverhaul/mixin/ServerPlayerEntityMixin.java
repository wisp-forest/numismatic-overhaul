package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import io.wispforest.owo.ops.ItemOps;
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

        final var world = player.world;
        if (world.isClient) return;

        final var component = ModComponents.CURRENCY.get(player);

        var dropPercentage = world.getGameRules().get(NumismaticOverhaul.MONEY_DROP_PERCENTAGE).get() * .01f;
        int dropped = (int) (component.getValue() * dropPercentage);

        var stacksDropped = CurrencyConverter.getAsValidStacks(dropped);
        for (var drop : stacksDropped) {
            for (int i = 0; i < drop.getCount(); i++) {
                player.dropItem(ItemOps.singleCopy(drop), true, false);
            }
        }

        component.modify(-dropped);
    }

}
