package com.glisco.numismaticoverhaul;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "numismatic-overhaul")
public class NumismaticOverhaulConfig implements ConfigData {

    @ConfigEntry.Gui.RequiresRestart
    @Comment("Whether villagers should use Numismatic currency for trading")
    public boolean enableVillagerTrading = true;

    @Comment("Whether taxes from Minecraft Comes Alive: Reborn should be delivered as Numismatic currency")
    public boolean enableMcaCompatibility = true;

    @Comment("Where the purse in your inventory should be placed on the X axis")
    public int pursePositionX = 129;

    @Comment("Where the purse in your inventory should be placed on the Y axis")
    public int pursePositionY = 20;
}
