package com.glisco.numismaticoverhaul.item;

import com.glisco.numismaticoverhaul.currency.CurrencyResolver;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static com.glisco.numismaticoverhaul.NumismaticOverhaul.MONEY_BAG_COMPONENT;
import static com.glisco.numismaticoverhaul.NumismaticOverhaul.id;

public record MoneyBagComponent(long bronze, long silver, long gold) {

    public static final StructEndec<MoneyBagComponent> ENDEC = StructEndecBuilder.of(
        StructEndec.LONG.fieldOf("bronze", MoneyBagComponent::bronze),
        StructEndec.LONG.fieldOf("silver", MoneyBagComponent::silver),
        StructEndec.LONG.fieldOf("gold", MoneyBagComponent::gold),
        MoneyBagComponent::new
    );

    public static MoneyBagComponent of(long[] values) {
        return MoneyBagComponent.of(
            values[0],
            values[1],
            values[2]
        );
    }

    public static MoneyBagComponent of(long value) {
        var money = CurrencyResolver.splitValues(value);
        return new MoneyBagComponent(money[0], money[1], money[2]);
    }

    public static MoneyBagComponent of(long bronze, long silver, long gold) {
        return new MoneyBagComponent(bronze, silver, gold);
    }

    public static MoneyBagComponent combine(long[] money, long[] money2) {
        return new MoneyBagComponent(
            money[0] + money2[0],
            money[1] + money2[1],
            money[2] + money2[2]
        );
    }

    public static MoneyBagComponent combine(ItemStack stack1, ItemStack stack2) {
        var values1 = stack1.getOrDefault(MONEY_BAG_COMPONENT, of(0));
        var values2 = stack2.getOrDefault(MONEY_BAG_COMPONENT, of(0));
        return new MoneyBagComponent(
            values1.bronze + values2.bronze,
            values1.silver + values2.silver,
            values1.gold + values2.gold
        );
    }

    public long value() {
        return CurrencyResolver.combineValues(bronze, silver, gold);
    }

    public static ComponentType<MoneyBagComponent> register() {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, id("money_bag"), ComponentType.<MoneyBagComponent>builder()
            .codec(CodecUtils.toCodec(MoneyBagComponent.ENDEC))
            .packetCodec(CodecUtils.toPacketCodec(MoneyBagComponent.ENDEC))
            .build()
        );
    }
}
