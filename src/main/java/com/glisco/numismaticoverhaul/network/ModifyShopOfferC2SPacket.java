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

public class ModifyShopOfferC2SPacket {

    public static final Identifier ID = new Identifier(NumismaticOverhaul.MOD_ID, "modify-shop-offer");

    public static void onPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender) {

        int[] values = buffer.readIntArray();

        ModifyShopOfferC2SPacket.Action action = ModifyShopOfferC2SPacket.Action.values()[values[0]];

        server.execute(() -> {
            if (player.currentScreenHandler instanceof ShopScreenHandler) {

                ShopScreenHandler shopHandler = (ShopScreenHandler) player.currentScreenHandler;

                if (action == Action.LOAD) {
                    shopHandler.loadOffer(values[1]);
                } else if (action == Action.CREATE) {
                    shopHandler.createOffer(values[1]);
                } else if (action == Action.DELETE) {
                    shopHandler.deleteOffer();
                }

            }
        });
    }

    public static Packet<?> createCREATE(int price) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        int[] values = new int[]{Action.CREATE.ordinal(), price};
        buffer.writeIntArray(values);

        return ClientPlayNetworking.createC2SPacket(ID, buffer);
    }

    public static Packet<?> createLOAD(int index) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        int[] values = new int[]{Action.LOAD.ordinal(), index};
        buffer.writeIntArray(values);

        return ClientPlayNetworking.createC2SPacket(ID, buffer);
    }

    public static Packet<?> createDELETE() {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        int[] values = new int[]{Action.DELETE.ordinal()};
        buffer.writeIntArray(values);

        return ClientPlayNetworking.createC2SPacket(ID, buffer);
    }

    public enum Action {
        CREATE, DELETE, LOAD
    }

}
