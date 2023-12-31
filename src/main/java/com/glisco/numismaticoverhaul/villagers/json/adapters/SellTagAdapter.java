package com.glisco.numismaticoverhaul.villagers.json.adapters;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.Currency;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import com.glisco.numismaticoverhaul.villagers.json.TradeJsonAdapter;
import com.glisco.numismaticoverhaul.villagers.json.VillagerJsonHelper;
import com.google.gson.JsonObject;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList.Named;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import org.jetbrains.annotations.NotNull;

public class SellTagAdapter extends TradeJsonAdapter {

    @Override
    @NotNull
    public TradeOffers.Factory deserialize(JsonObject json) {
        loadDefaultStats(json, true);

        VillagerJsonHelper.assertJsonObject(json, "sell");

        final var sellObject = JsonHelper.getObject(json, "sell");
        final var tag = new Identifier(JsonHelper.getString(sellObject, "tag"));
        final int count = JsonHelper.getInt(sellObject, "count", 1);

        int price = json.get("price").getAsInt();
        return new Factory(tag, count, price, max_uses, villager_experience, price_multiplier);
    }

    private static class Factory implements TradeOffers.Factory {
        private final Identifier sellTag;
        private final int count;
        private final int maxUses;
        private final int experience;
        private final int price;
        private final float multiplier;

        public Factory(Identifier sellTag, int count, int price, int maxUses, int experience, float multiplier) {
            this.sellTag = sellTag;
            this.count = count;
            this.maxUses = maxUses;
            this.experience = experience;
            this.price = price;
            this.multiplier = multiplier;
        }

        public TradeOffer create(Entity entity, Random random) {
            final var entries = Registries.ITEM.getEntryList(TagKey.of(RegistryKeys.ITEM, sellTag))
                    .orElse(null);

            if (entries == null) {
                NumismaticOverhaul.LOGGER.warn("Could not generate trade for tag '" + sellTag + "', as it does not exist");

                final var player = entity.getWorld().getClosestPlayer(entity, 15);
                if (player != null) {
                    player.sendMessage(TextOps.withColor("numismatic §> there has been a problem generating trades, check the log for details",
                            Currency.GOLD.getNameColor(), TextOps.color(Formatting.GRAY)), false);
                }

                return null;
            }

            final var sellStack = new ItemStack(entries.get(random.nextInt(entries.size())).value(), this.count);
            return new TradeOffer(CurrencyHelper.getClosest(price), sellStack, this.maxUses, this.experience, multiplier);
        }
    }

}
