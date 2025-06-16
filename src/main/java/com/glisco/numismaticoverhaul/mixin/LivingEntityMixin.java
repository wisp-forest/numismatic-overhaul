package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.glisco.numismaticoverhaul.NumismaticOverhaul.CONFIG;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow @Nullable protected PlayerEntity attackingPlayer;

    @Shadow public abstract float getMaxHealth();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "dropLoot", at = @At("TAIL"))
    public void injectCoins(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (this.attackingPlayer == null) return;
        // Handle config dependent coin drops
        var entityType = this.getType();
        if (NumismaticOverhaul.MOBS_IN_BOURGEOISIE.containsKey(entityType)) {
            long baseValue = NumismaticOverhaul.MOBS_IN_BOURGEOISIE.get(entityType);
            float variance = this.getWorld().getGameRules().get(NumismaticOverhaul.MONEY_MOB_DROP_VARIANCE).get() * .01f;
            if (variance > 0.02f) {
                variance = MathHelper.nextBetween(random, 1.0f - variance, 1.0f + variance);
            } else {
                variance = 1.0f;
            }
            if (CONFIG.scaleOnHealth()) {
                variance *= (this.getMaxHealth() / (20 * CONFIG.healthScaleReduction()));
            }
            long finalValue = MathHelper.clamp(((long) (baseValue * variance)), 0, Long.MAX_VALUE);
            var moneyStacks = CurrencyHelper.getAsStacks(finalValue, 4);
            moneyStacks.forEach(this::dropStack);
        }
    }

}
