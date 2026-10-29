package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.block.pawn.PawnShopScreenHandler;
import io.wispforest.owo.network.ServerAccess;

public record PawnShopScreenHandlerRequestC2SPacket(int syncId, Action action, long value) {

    public PawnShopScreenHandlerRequestC2SPacket(int syncId, Action action) {
        this(syncId, action, 0);
    }

    public static void handle(PawnShopScreenHandlerRequestC2SPacket message, ServerAccess access) {
        final var player = access.player();
        if (player == null || player.isSpectator() || !player.isAlive()) return;
        if (player.currentScreenHandler == null || player.currentScreenHandler.syncId != message.syncId()) return;
        if (!(player.currentScreenHandler instanceof PawnShopScreenHandler shopHandler)) return;
        if (!shopHandler.canUse(player)) return;
        if (!shopHandler.isOwner(player)) return;

        if (message.action() == null) return;

        final long value = message.value();

        switch (message.action()) {
            case LOAD_OFFER -> {
                if (value < 0) return;
                shopHandler.loadOffer(value);
            }
            case CREATE_OFFER -> {
                if (value <= 0) return;
                shopHandler.createOffer(value);
            }
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
