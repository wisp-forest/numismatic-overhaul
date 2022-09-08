package com.glisco.numismaticoverhaul;

import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import net.minecraft.item.ItemStack;

public class NumismaticOverhaulItemGroup extends OwoItemGroup {

    public NumismaticOverhaulItemGroup() {
        super(NumismaticOverhaul.id("main"));
    }

    @Override
    protected void setup() {
        this.addButton(ItemGroupButton.modrinth("https://modrinth.com/mod/numismatic-overhaul"));
        this.addButton(ItemGroupButton.curseforge("https://www.curseforge.com/minecraft/mc-mods/numismatic-overhaul"));
        this.addButton(ItemGroupButton.github("https://github.com/wisp-forest/numismatic-overhaul"));
        this.addButton(ItemGroupButton.discord("https://discord.gg/xrwHKktV2d"));
    }

    @Override
    public ItemStack createIcon() {
        return MoneyBagItem.createCombined(new long[]{0, 1, 0});
    }
}
