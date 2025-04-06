package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.villagers.data.NumismaticTradeOfferExtensions;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

// TODO: Review, not sure if this is correct. Seems to work?
@Mixin(SetTradeOffersS2CPacket.class)
public class SetTradeOffersS2CPacketMixin implements NumismaticTradeOfferExtensions {
    @Unique
    private int numismatic$reputation = 0;

    @Override
    public void numismatic$setReputation(int reputation) {
        this.numismatic$reputation = reputation;
    }

    @Override
    public int numismatic$getReputation() {
        return numismatic$reputation;
    }

    @Inject(method = "write", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void saveReputation(RegistryByteBuf buf, CallbackInfo ci) {
        buf.writeVarInt(numismatic$reputation);
    }

    @Inject(method = "<init>(ILnet/minecraft/village/TradeOfferList;IIZZ)V", at = @At("RETURN"))
    private void loadReputation1(int syncId, TradeOfferList offers, int levelProgress, int experience, boolean leveled, boolean refreshable, CallbackInfo ci) {
        this.numismatic$reputation = -69420;
    }

    @Inject(method = "<init>(Lnet/minecraft/network/RegistryByteBuf;)V", at = @At("RETURN"))
    private void loadReputation(RegistryByteBuf buf, CallbackInfo ci) {
        this.numismatic$reputation = buf.readVarInt();
    }
}
