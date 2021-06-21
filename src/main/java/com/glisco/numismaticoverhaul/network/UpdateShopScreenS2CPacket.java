package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.ShopOffer;
import com.glisco.numismaticoverhaul.client.gui.shop.ShopScreen;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class UpdateShopScreenS2CPacket {

    public static final Identifier ID = new Identifier(NumismaticOverhaul.MOD_ID, "update-shop-screen");

    public static void onPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender) {

        List<ShopOffer> offers = new ArrayList<>();
        ShopOffer.fromTag(buffer.readNbt(), offers);

        int storedCurrency = buffer.readVarInt();

        client.execute(() -> {
            if (MinecraftClient.getInstance().currentScreen instanceof ShopScreen) {
                ShopScreen screen = (ShopScreen) MinecraftClient.getInstance().currentScreen;
                screen.updateScreen(offers, storedCurrency);
            }
        });
    }

    public static Packet<?> create(List<ShopOffer> offers, int storedCurrency) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        NbtCompound tag = ShopOffer.toTag(new NbtCompound(), offers);
        buffer.writeNbt(tag);
        buffer.writeVarInt(storedCurrency);

        return ServerPlayNetworking.createS2CPacket(ID, buffer);
    }
}
