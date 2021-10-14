package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.villagers.data.NumismaticTradeOfferExtensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {

    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract int getReputation(PlayerEntity player);

    @Inject(method = "prepareOffersFor", at = @At("TAIL"))
    private void captureReputation(PlayerEntity player, CallbackInfo ci) {
        final int reputation = this.getReputation(player) == 0 ? 1 : this.getReputation(player);

        this.getOffers().forEach(offer -> ((NumismaticTradeOfferExtensions) offer).numismatic$setReputation(
                (int) (reputation *
                        (player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE) ?
                                (1 + player.getStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE).getAmplifier() * 0.1)
                                :
                                1))
        ));

    }

}
