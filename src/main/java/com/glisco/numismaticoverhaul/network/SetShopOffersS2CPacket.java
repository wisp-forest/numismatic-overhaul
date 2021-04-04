package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.block.ShopOffer;
import com.glisco.numismaticoverhaul.client.ShopScreen;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class SetShopOffersS2CPacket {

    public static final Identifier ID = new Identifier(NumismaticOverhaul.MOD_ID, "set-shop-offers");

    public static void onPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender) {

        List<ShopOffer> offers = new ArrayList<>();
        ShopOffer.fromTag(buffer.readCompoundTag(), offers);

        client.execute(() -> {
            if (MinecraftClient.getInstance().currentScreen instanceof ShopScreen) {
                ShopScreen screen = (ShopScreen) MinecraftClient.getInstance().currentScreen;
                screen.updateShopOffers(offers);
            }
        });
    }

    public static Packet<?> create(List<ShopOffer> offers) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        CompoundTag tag = ShopOffer.toTag(new CompoundTag(), offers);
        buffer.writeCompoundTag(tag);

        return ServerPlayNetworking.createS2CPacket(ID, buffer);
    }
}
