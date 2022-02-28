package com.glisco.numismaticoverhaul.compat.rei;

import com.glisco.numismaticoverhaul.client.gui.shop.ShopScreen;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;

import java.util.Arrays;
import java.util.Collections;

public class NOReiPlugin implements REIClientPlugin {

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(ShopScreen.class, screen -> {

            int x = screen.getRootX();
            int y = screen.getRootY();

            int tab = screen.getSelectedTab();

            return tab == 0 ? Collections.singletonList(new Rectangle(x + 160, y, 45, 60)) : Arrays.asList(new Rectangle(x + 160, y, 115, 60), new Rectangle(x + 160, y + 60, 45, 60));
        });
    }

}
