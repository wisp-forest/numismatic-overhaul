package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.ShopScreenHandler;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ShopScreenHandlerRequestC2SPacket {

    public static final Identifier ID = new Identifier(NumismaticOverhaul.MOD_ID, "shop-screen-handler-request");

    public static void onPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender) {

        int[] values = buffer.readIntArray();

        ShopScreenHandlerRequestC2SPacket.Action action = ShopScreenHandlerRequestC2SPacket.Action.values()[values[0]];

        server.execute(() -> {
            if (player.currentScreenHandler instanceof ShopScreenHandler) {

                ShopScreenHandler shopHandler = (ShopScreenHandler) player.currentScreenHandler;

                if (action == Action.LOAD_OFFER) {
                    shopHandler.loadOffer(values[1]);
                } else if (action == Action.CREATE_OFFER) {
                    shopHandler.createOffer(values[1]);
                } else if (action == Action.DELETE_OFFER) {
                    shopHandler.deleteOffer();
                } else if (action == Action.EXTRACT_CURRENCY) {
                    shopHandler.extractCurrency();
                }

            }
        });
    }

    public static Packet<?> createCREATE(int price) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        int[] values = new int[]{Action.CREATE_OFFER.ordinal(), price};
        buffer.writeIntArray(values);

        return ClientPlayNetworking.createC2SPacket(ID, buffer);
    }

    public static Packet<?> createLOAD(int index) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        int[] values = new int[]{Action.LOAD_OFFER.ordinal(), index};
        buffer.writeIntArray(values);

        return ClientPlayNetworking.createC2SPacket(ID, buffer);
    }

    public static Packet<?> createDELETE() {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        int[] values = new int[]{Action.DELETE_OFFER.ordinal()};
        buffer.writeIntArray(values);

        return ClientPlayNetworking.createC2SPacket(ID, buffer);
    }

    public static Packet<?> createEXTRACT() {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        int[] values = new int[]{Action.EXTRACT_CURRENCY.ordinal()};
        buffer.writeIntArray(values);

        return ClientPlayNetworking.createC2SPacket(ID, buffer);
    }

    public enum Action {
        CREATE_OFFER, DELETE_OFFER, LOAD_OFFER, EXTRACT_CURRENCY
    }

}
