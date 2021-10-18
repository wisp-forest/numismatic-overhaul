package com.glisco.numismaticoverhaul.network;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.currency.CurrencyHelper;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class RequestPurseActionC2SPacket {

    public static final Identifier ID = new Identifier(NumismaticOverhaul.MOD_ID, "request-purse-action");

    public static void onPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender sender) {

        int[] values = buffer.readIntArray();

        Action action = Action.values()[values[0]];

        server.execute(() -> {
            if (player.currentScreenHandler instanceof PlayerScreenHandler) {
                if (action == Action.STORE_ALL) {

                    ModComponents.CURRENCY.get(player).modify(CurrencyHelper.getMoneyInInventory(player, true));

                } else if (action == Action.EXTRACT) {

                    //Check if we can actually extract this much money to prevent cheeky packet forgery
                    if (ModComponents.CURRENCY.get(player).getValue() < values[1]) return;

                    CurrencyConverter.getAsItemStackList(values[1]).forEach(stack -> player.getInventory().offerOrDrop(stack));

                    ModComponents.CURRENCY.get(player).modify(-values[1]);
                } else if (action == Action.EXTRACT_ALL) {
                    CurrencyConverter.getAsValidStacks(ModComponents.CURRENCY.get(player).getValue())
                            .forEach(stack -> player.getInventory().offerOrDrop(stack));

                    ModComponents.CURRENCY.get(player).modify(-ModComponents.CURRENCY.get(player).getValue());
                }
            }
        });
    }

    public static Packet<?> create(Action action) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        int[] values = new int[]{action.ordinal()};
        buffer.writeIntArray(values);
        return ClientPlayNetworking.createC2SPacket(ID, buffer);
    }

    public static Packet<?> create(Action action, int value) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        int[] values = new int[]{action.ordinal(), value};
        buffer.writeIntArray(values);
        return ClientPlayNetworking.createC2SPacket(ID, buffer);
    }

    public enum Action {
        STORE_ALL, EXTRACT, EXTRACT_ALL
    }
}
