package com.glisco.numismaticoverhaul;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.RestartRequired;

@Modmenu(modId = "numismatic-overhaul")
@Config(name = "numismatic-overhaul", wrapperName = "NumismaticOverhaulConfig")
public class NumismaticOverhaulConfigModel {

    @RestartRequired
    @Comment("Whether villagers should use Numismatic currency for trading")
    public boolean enableVillagerTrading = true;

    @Comment("Whether taxes from Minecraft Comes Alive: Reborn should be delivered as Numismatic currency")
    public boolean enableMcaCompatibility = true;

    @RestartRequired
    @Comment("Whether Numismatic currency should be injected into the loot tables of loot chests")
    public boolean generateCurrencyInChests = true;

    @Comment("Where the purse in your inventory should be placed on the X axis")
    public int pursePositionX = 129;

    @Comment("Where the purse in your inventory should be placed on the Y axis")
    public int pursePositionY = 20;

    @Nest
    public LootOptions_ lootOptions = new LootOptions_();

    public static class LootOptions_ {
        @Comment("Affects money gained from Dungeon and Mineshaft chests")
        public int desertMinLoot = 300;
        @Comment("Affects money gained from Dungeon and Mineshaft chests")
        public int desertMaxLoot = 1200;
        @Comment("Affects money gained from Dungeon and Mineshaft chests")
        public int dungeonMinLoot = 500;
        @Comment("Affects money gained from Dungeon and Mineshaft chests")
        public int dungeonMaxLoot = 2000;

        @Comment("Affects money gained from Bastion, Stronghold, Outpost and Buried Treasure chests")
        public int structureMinLoot = 1500;
        public int structureMaxLoot = 4000;

        public int strongholdLibraryMinLoot = 2000;
        public int strongholdLibraryMaxLoot = 6000;
    }
}
