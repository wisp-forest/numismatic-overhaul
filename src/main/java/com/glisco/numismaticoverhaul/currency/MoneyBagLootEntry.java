package com.glisco.numismaticoverhaul.currency;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class MoneyBagLootEntry extends LeafEntry {

    private final int min;
    private final int max;

    private MoneyBagLootEntry(int min, int max, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
        super(weight, quality, conditions, functions);
        this.min = min;
        this.max = max;
    }

    @Override
    protected void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        int value = MathHelper.nextInt(context.getRandom(), min, max);
        if (value == 0) return;

        lootConsumer.accept(MoneyBagItem.create(value));
    }

    public static LeafEntry.Builder<?> builder(int min, int max) {
        return builder((weight, quality, conditions, functions) -> new MoneyBagLootEntry(min, max, weight, quality, conditions, functions));
    }

    @Override
    public LootPoolEntryType getType() {
        return NumismaticOverhaul.MONEY_BAG_ENTRY;
    }

    public static class Serializer extends LeafEntry.Serializer<MoneyBagLootEntry> {

        @Override
        public void addEntryFields(JsonObject jsonObject, MoneyBagLootEntry moneyBagLootEntry, JsonSerializationContext jsonSerializationContext) {
            super.addEntryFields(jsonObject, moneyBagLootEntry, jsonSerializationContext);
            jsonObject.addProperty("min", moneyBagLootEntry.min);
            jsonObject.addProperty("max", moneyBagLootEntry.max);
        }

        @Override
        protected MoneyBagLootEntry fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, int i, int j, LootCondition[] lootConditions, LootFunction[] lootFunctions) {
            final int mix = JsonHelper.getInt(jsonObject, "min", 0);
            final int max = JsonHelper.getInt(jsonObject, "max");
            return new MoneyBagLootEntry(mix, max, i, j, lootConditions, lootFunctions);
        }
    }
}
