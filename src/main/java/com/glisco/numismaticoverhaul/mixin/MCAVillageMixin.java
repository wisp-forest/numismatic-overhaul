package com.glisco.numismaticoverhaul.mixin;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.item.NumismaticOverhaulItems;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "mca.server.world.data.Village", remap = false)
public class MCAVillageMixin {

    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyVariable(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", remap = true), name = "emeraldValue")
    private int removeDivisor(int value) {
        return NumismaticOverhaul.CONFIG.enableMcaCompatibility() ? 1 : value;
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "tick", at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/item/Items;EMERALD:Lnet/minecraft/item/Item;", remap = true))
    private Item weWantCoins() {
        return NumismaticOverhaul.CONFIG.enableMcaCompatibility() ? NumismaticOverhaulItems.BRONZE_COIN : Items.EMERALD;
    }

}
