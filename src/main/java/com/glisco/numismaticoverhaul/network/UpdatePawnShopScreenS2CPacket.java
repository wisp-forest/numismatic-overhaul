package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.pawn.PawnShopBlockEntity;
import com.glisco.numismaticoverhaul.block.pawn.PawnShopOffer;
import com.glisco.numismaticoverhaul.client.gui.PawnShopScreen;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.network.serialization.PacketBufSerializer;

import java.util.List;

public record UpdatePawnShopScreenS2CPacket(List<PawnShopOffer> offers, long storedCurrency, boolean transferEnabled) {

    public UpdatePawnShopScreenS2CPacket(PawnShopBlockEntity shop) {
        this(shop.getOffers(), shop.getStoredCurrency(), shop.isTransferEnabled());
    }

    public static void handle(UpdatePawnShopScreenS2CPacket message, ClientAccess access) {
        if (!(access.runtime().currentScreen instanceof PawnShopScreen screen)) return;
        screen.update(message);
    }

    public static void initialize() {
        //noinspection ConstantConditions
        PacketBufSerializer.register(
                PawnShopOffer.class,
                (buf, shopOffer) -> buf.writeNbt(shopOffer.toNbt()),
                buf -> PawnShopOffer.fromNbt(buf.readNbt())
        );

        NumismaticOverhaul.CHANNEL.registerClientbound(UpdatePawnShopScreenS2CPacket.class, UpdatePawnShopScreenS2CPacket::handle);
    }
}
