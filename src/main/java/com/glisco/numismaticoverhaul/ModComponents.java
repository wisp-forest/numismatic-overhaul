package com.glisco.numismaticoverhaul;

import com.glisco.numismaticoverhaul.currency.CurrencyComponent;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public class ModComponents implements EntityComponentInitializer {

    public static final ComponentKey<CurrencyComponent> CURRENCY = ComponentRegistry.getOrCreate(new Identifier("numismatic-overhaul", "currency"), CurrencyComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(CURRENCY, CurrencyComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
