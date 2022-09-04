package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.ShopBlockEntity;
import com.glisco.numismaticoverhaul.block.ShopOffer;
import com.glisco.numismaticoverhaul.client.gui.ShopScreen;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.network.serialization.PacketBufSerializer;

import java.util.List;

public record UpdateShopScreenS2CPacket(List<ShopOffer> offers, long storedCurrency, boolean transferEnabled) {

    public UpdateShopScreenS2CPacket(ShopBlockEntity shop) {
        this(shop.getOffers(), shop.getStoredCurrency(), shop.isTransferEnabled());
    }

    public static void handle(UpdateShopScreenS2CPacket message, ClientAccess access) {
        if (!(access.runtime().currentScreen instanceof ShopScreen screen)) return;
        screen.update(message);
    }

    public static void register() {
        //noinspection ConstantConditions
        PacketBufSerializer.register(
                ShopOffer.class,
                (buf, shopOffer) -> buf.writeNbt(shopOffer.toNbt()),
                buf -> ShopOffer.fromNbt(buf.readNbt())
        );

        NumismaticOverhaul.CHANNEL.registerClientbound(UpdateShopScreenS2CPacket.class, UpdateShopScreenS2CPacket::handle);
    }
}
