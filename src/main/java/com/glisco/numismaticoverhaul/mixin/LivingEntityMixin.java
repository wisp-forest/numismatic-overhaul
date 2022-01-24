package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.item.NumismaticOverhaulItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "dropLoot", at = @At("TAIL"))
    public void injectCoins(DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (!NumismaticOverhaul.THE_BOURGEOISIE.contains(this.getType())) return;
        if (random.nextFloat() > .5f) dropStack(new ItemStack(NumismaticOverhaulItems.BRONZE_COIN, random.nextInt(9, 35)));
        if (random.nextFloat() > .2f) dropStack(new ItemStack(NumismaticOverhaulItems.SILVER_COIN));
    }

}
