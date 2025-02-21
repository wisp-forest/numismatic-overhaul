package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import io.wispforest.owo.network.ServerAccess;

public record RequestPurseActionC2SPacket(Action action, long value) {

    public static void handle(RequestPurseActionC2SPacket message, ServerAccess access) {
        final var player = access.player();
        final long value = message.value();

        switch (message.action()) {
            case STORE_ALL ->
                    ModComponents.CURRENCY.get(player).modify(CurrencyHelper.getMoneyInInventory(player, true));
            case EXTRACT -> {
                //Limit the amount of money we cam extract to prevent cheeky packet forgery
                //It'd be a bit of a problem if a player somehow tried to extract -200 ;)
                var extracting = Math.max(0, Math.min(value, ModComponents.CURRENCY.get(player).getValue()))

                CurrencyConverter.getAsItemStackList(extracting).forEach(stack -> player.getInventory().offerOrDrop(stack));
                ModComponents.CURRENCY.get(player).modify(-extracting);
            }
            case EXTRACT_ALL -> {
                CurrencyConverter.getAsValidStacks(ModComponents.CURRENCY.get(player).getValue())
                        .forEach(stack -> player.getInventory().offerOrDrop(stack));

                ModComponents.CURRENCY.get(player).modify(-ModComponents.CURRENCY.get(player).getValue());
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
