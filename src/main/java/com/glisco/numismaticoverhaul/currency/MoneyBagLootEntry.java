package com.glisco.numismaticoverhaul.currency;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Consumer;

public class MoneyBagLootEntry extends LeafEntry {

    public static final Codec<MoneyBagLootEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.createStrictOptionalFieldCodec(Codec.INT, "min", 0).forGetter(o -> o.min),
            Codec.INT.fieldOf("max").forGetter(o -> o.max)
    ).and(method_53290(instance)).apply(instance, MoneyBagLootEntry::new));

    private final int min;
    private final int max;

    private MoneyBagLootEntry(int min, int max, int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions) {
        super(weight, quality, conditions, functions);
        this.min = min;
        this.max = max;
    }

    @Override
    protected void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        int value = MathHelper.nextInt(context.getRandom(), min, max);
        if (value == 0) return;

        lootConsumer.accept(MoneyBagItem.createCombined(CurrencyResolver.splitValues(value)));
    }

    public static LeafEntry.Builder<?> builder(int min, int max) {
        return builder((weight, quality, conditions, functions) -> new MoneyBagLootEntry(min, max, weight, quality, conditions, functions));
    }

    @Override
    public LootPoolEntryType getType() {
        return NumismaticOverhaul.MONEY_BAG_ENTRY;
    }
}
