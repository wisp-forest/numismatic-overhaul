package com.glisco.numismaticoverhaul.mixin;

import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Set;

@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixin {

    @SuppressWarnings("unchecked")
    @Inject(method = "fixStack", at = @At("TAIL"))
    private static void numismaticoverhaul$migrateToDataComponents(ItemStackComponentizationFix.StackData data, Dynamic dynamic, CallbackInfo ci) {
        if (data.itemMatches(Set.of("numismatic-overhaul:money_bag"))) {
            var values = data.getAndRemove("Values").asList(dynamic1 -> dynamic1.asLong(0));
            data.setComponent("numismatic-overhaul:money_bag", dynamic.emptyMap()
                .set("bronze", dynamic.createLong(values.get(0)))
                .set("silver", dynamic.createLong(values.get(1)))
                .set("gold", dynamic.createLong(values.get(2)))
            );
        }
    }
}
