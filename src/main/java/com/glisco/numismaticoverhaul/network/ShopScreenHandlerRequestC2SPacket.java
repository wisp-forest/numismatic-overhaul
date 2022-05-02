package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.block.ShopScreenHandler;
import io.wispforest.owo.network.ServerAccess;

public record ShopScreenHandlerRequestC2SPacket(Action action, int value) {

    public ShopScreenHandlerRequestC2SPacket(Action action) {
        this(action, 0);
    }

    public static void handle(ShopScreenHandlerRequestC2SPacket message, ServerAccess access) {
        final var player = access.player();
        final int value = message.value();

        if (!(player.currentScreenHandler instanceof ShopScreenHandler shopHandler)) return;

        switch (message.action()) {
            case LOAD_OFFER -> shopHandler.loadOffer(value);
            case CREATE_OFFER -> shopHandler.createOffer(value);
            case DELETE_OFFER -> shopHandler.deleteOffer();
            case EXTRACT_CURRENCY -> shopHandler.extractCurrency();
            case TOGGLE_TRANSFER -> shopHandler.toggleTransfer();
        }
    }

    public enum Action {
        CREATE_OFFER, DELETE_OFFER, LOAD_OFFER, EXTRACT_CURRENCY, TOGGLE_TRANSFER
    }

}
