package com.glisco.numismaticoverhaul.mixin;

import io.wispforest.owo.ui.layers.Layer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Layer.Instance.class)
public interface LayerInstanceAccessor {

    @Accessor(value = "layoutUpdaters", remap = false)
    List<Runnable> numismatic$getLayoutUpdaters();

}
