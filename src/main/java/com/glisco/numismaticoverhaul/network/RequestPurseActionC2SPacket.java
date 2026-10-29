package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import io.wispforest.owo.network.ServerAccess;

public record RequestPurseActionC2SPacket(Action action, long value) {

    public static void handle(RequestPurseActionC2SPacket message, ServerAccess access) {
        final var player = access.player();
        if (player == null || player.isSpectator() || !player.isAlive()) return;

        switch (message.action()) {
            case STORE_ALL -> {
                long stored = CurrencyHelper.getMoneyInInventory(player, true);
                if (stored > 0) {
                    ModComponents.CURRENCY.get(player).modify(stored);
                }
            }
            case EXTRACT -> {
                final long value = message.value();
                if (value <= 0) return;

                //Limit the amount of money we cam extract to prevent cheeky packet forgery
                //It'd be a bit of a problem if a player somehow tried to extract -200 ;)
                var currency = ModComponents.CURRENCY.get(player);
                var extracting = Math.min(value, currency.getValue());
                if (extracting <= 0) return;

                CurrencyHelper.getAsStacks(extracting, 5).forEach(stack -> player.getInventory().offerOrDrop(stack));
                currency.modify(-extracting);
            }
            case EXTRACT_ALL -> {
                var currency = ModComponents.CURRENCY.get(player);
                long total = currency.getValue();
                if (total <= 0) return;

                CurrencyHelper.getAsStacks(total, 5)
                        .forEach(stack -> player.getInventory().offerOrDrop(stack));

                currency.modify(-total);
            }
        }
    }

    public static RequestPurseActionC2SPacket storeAll() {
        return new RequestPurseActionC2SPacket(Action.STORE_ALL, 0);
    }

    public static RequestPurseActionC2SPacket extractAll() {
        return new RequestPurseActionC2SPacket(Action.EXTRACT_ALL, 0);
    }

    public static RequestPurseActionC2SPacket extract(long amount) {
        return new RequestPurseActionC2SPacket(Action.EXTRACT, amount);
    }

    public enum Action {
        STORE_ALL, EXTRACT, EXTRACT_ALL
    }
}
