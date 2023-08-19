package com.glisco.numismaticoverhaul;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;

@SuppressWarnings("unused")
@Modmenu(modId = "numismatic-overhaul")
@Config(name = "numismatic-overhaul", wrapperName = "NumismaticOverhaulConfig")
public class NumismaticOverhaulConfigModel {

    @RestartRequired
    @Comment("Whether villagers should use Numismatic currency for trading")
    public boolean enableVillagerTrading = true;

    @RestartRequired
    @Comment("Whether Numismatic currency should be injected into the loot tables of loot chests")
    public boolean generateCurrencyInChests = true;

    @Comment("Where the purse in your inventory should be placed on the X axis")
    public int pursePositionX = 27;

    @Comment("Where the purse in your inventory should be placed on the Y axis")
    public int pursePositionY = -28;

    @Comment("Where the notification for adding/removing money from the purse should be (requires rejoining to apply)")
    @Sync(Option.SyncMode.INFORM_SERVER)
    public MoneyMessageLocation moneyMessageLocation = MoneyMessageLocation.ACTIONBAR;

    @Nest
    public LootOptions lootOptions = new LootOptions();


    public static class LootOptions {
        @Comment("Affects money gained from Desert Temple chests")
        public int desertMinLoot = 300;
        @Comment("Affects money gained from Desert Temple chests")
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

    public enum MoneyMessageLocation {
        ACTIONBAR,
        CHAT,
        DISABLED
    }
}
