package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.block.pawn.PawnShopScreenHandler;
import com.glisco.numismaticoverhaul.block.shop.ShopScreenHandler;
import io.wispforest.owo.network.ServerAccess;

public record PawnShopScreenHandlerRequestC2SPacket(Action action, long value) {

    public PawnShopScreenHandlerRequestC2SPacket(Action action) {
        this(action, 0);
    }

    public static void handle(PawnShopScreenHandlerRequestC2SPacket message, ServerAccess access) {
        final var player = access.player();
        final long value = message.value();

        if (!(player.currentScreenHandler instanceof PawnShopScreenHandler shopHandler)) return;

        switch (message.action()) {
            case LOAD_OFFER -> shopHandler.loadOffer(value);
            case CREATE_OFFER -> shopHandler.createOffer(value);
            case DELETE_OFFER -> shopHandler.deleteOffer();
            case INSERT_CURRENCY -> shopHandler.insertCurrency();
            case EXTRACT_CURRENCY -> shopHandler.extractCurrency();
            case TOGGLE_TRANSFER -> shopHandler.toggleTransfer();
            case CLICK_BUFFER -> shopHandler.handleBufferClick();
        }
    }

    public enum Action {
        CREATE_OFFER, DELETE_OFFER, LOAD_OFFER, INSERT_CURRENCY, EXTRACT_CURRENCY, TOGGLE_TRANSFER, CLICK_BUFFER
    }

}
