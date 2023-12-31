package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.block.ShopScreenHandler;
import io.wispforest.owo.network.ServerAccess;
import net.minecraft.server.network.ServerPlayerEntity;

public record ShopScreenHandlerRequestC2SPacket(Action action, long value) {

    public ShopScreenHandlerRequestC2SPacket(Action action) {
        this(action, 0);
    }

    public static void handle(ShopScreenHandlerRequestC2SPacket message, ServerAccess access) {
        final var player = access.player();
        final long value = message.value();

        if (!(player.currentScreenHandler instanceof ShopScreenHandler shopHandler)) return;

        switch (message.action()) {
            case LOAD_OFFER -> shopHandler.loadOffer(value);
            case CREATE_OFFER -> shopHandler.createOffer(value);
            case DELETE_OFFER -> shopHandler.deleteOffer();
            case EXTRACT_CURRENCY -> shopHandler.extractCurrency();
            case TOGGLE_TRANSFER -> shopHandler.toggleTransfer();
            case CLICK_BUFFER -> shopHandler.handleBufferClick();
        }
    }

    public enum Action {
        CREATE_OFFER, DELETE_OFFER, LOAD_OFFER, EXTRACT_CURRENCY, TOGGLE_TRANSFER, CLICK_BUFFER
    }

}
