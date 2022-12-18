package com.glisco.numismaticoverhaul.villagers.json.adapters;

import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.villagers.exceptions.DeserializationException;
import com.glisco.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class SellSusStewAdapter extends TradeJsonAdapter {

    @Override
    public @NotNull TradeOffers.Factory deserialize(JsonObject json) {
        this.loadDefaultStats(json, true);

        final int price = json.get("price").getAsInt();
        final int duration = JsonHelper.getInt(json, "duration", 100);

        final var effectId = new Identifier(JsonHelper.getString(json, "effect_id"));
        final var effect = Registries.STATUS_EFFECT.getOrEmpty(effectId)
                .orElseThrow(() -> new DeserializationException("Unknown status effect '" + effectId + "'"));

        return new Factory(effect, price, duration, villager_experience, price_multiplier, max_uses);
    }

    static class Factory implements TradeOffers.Factory {
        private final StatusEffect effect;
        private final int price;
        private final int duration;
        private final int experience;
        private final int maxUses;
        private final float multiplier;

        public Factory(StatusEffect effect, int price, int duration, int experience, float multiplier, int maxUses) {
            this.effect = effect;
            this.price = price;
            this.duration = duration;
            this.experience = experience;
            this.multiplier = multiplier;
            this.maxUses = maxUses;
        }

        @Nullable
        @Override
        public TradeOffer create(Entity entity, Random random) {
            ItemStack susStew = new ItemStack(Items.SUSPICIOUS_STEW, 1);
            SuspiciousStewItem.addEffectToStew(susStew, this.effect, this.duration);

            return new TradeOffer(CurrencyHelper.getClosest(price), susStew, this.maxUses, this.experience, this.multiplier);
        }
    }
}
