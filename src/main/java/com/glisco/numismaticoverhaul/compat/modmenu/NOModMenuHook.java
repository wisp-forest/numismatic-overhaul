package com.glisco.numismaticoverhaul.compat.modmenu;

import com.glisco.numismaticoverhaul.NumismaticOverhaulConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

public class NOModMenuHook implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(NumismaticOverhaulConfig.class, parent).get();
    }
}
