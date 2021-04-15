package com.glisco.numismaticoverhaul.compat.rei;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.client.gui.shop.ShopScreen;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.BaseBoundsHandler;
import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collections;

public class NOReiPlugin implements REIPluginV0 {

    public static final Identifier ID = new Identifier(NumismaticOverhaul.MOD_ID, "rei_plugin");

    @Override
    public Identifier getPluginIdentifier() {
        return ID;
    }

    @Override
    public void registerBounds(DisplayHelper displayHelper) {
        BaseBoundsHandler.getInstance().registerExclusionZones(ShopScreen.class, () -> {

            ShopScreen screen = (ShopScreen) MinecraftClient.getInstance().currentScreen;

            int x = screen.getRootX();
            int y = screen.getRootY();

            int tab = screen.getSelectedTab();

            return tab == 0 ? Collections.singletonList(new Rectangle(x + 160, y, 45, 60)) : Arrays.asList(new Rectangle(x + 160, y, 115, 60), new Rectangle(x + 160, y + 60, 45, 60));
        });
    }
}
