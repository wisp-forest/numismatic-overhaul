package com.glisco.numismaticoverhaul;

import com.glisco.numismaticoverhaul.currency.Currency;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.offline.OfflineDataLookup;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NumismaticCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {

        dispatcher.register(literal("numismatic")
                .then(literal("balance").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                        .then(argument("player", EntityArgumentType.player())
                                .then(literal("get").executes(NumismaticCommand::get))
                                .then(literal("set")
                                        .then(argument("value", IntegerArgumentType.integer()).executes(NumismaticCommand::set)))))
                .then(literal("serverworth").executes(NumismaticCommand::serverWorth)));

    }

    @SuppressWarnings("ConstantConditions")
    private static int serverWorth(CommandContext<ServerCommandSource> context) {
        final var playerManager = context.getSource().getServer().getPlayerManager();

        var onlineUUIDs = playerManager.getPlayerList().stream().map(Entity::getUuid).toList();
        var offlineUUIDs = OfflineDataLookup.savedPlayers().stream().filter(uuid -> !onlineUUIDs.contains(uuid)).toList();

        int serverWorth = 0;
        for (var onlineId : onlineUUIDs) {
            serverWorth += ModComponents.CURRENCY.get(playerManager.getPlayer(onlineId)).getValue();
        }

        for (var offlineId : offlineUUIDs) {
            serverWorth += OfflineDataLookup.get(offlineId).getCompound("cardinal_components").getCompound("numismatic-overhaul:currency").getInt("Value");
        }

        context.getSource().sendFeedback(TextOps.withColor("numismatic §> server net worth: " + serverWorth,
                Currency.GOLD.getNameColor(), TextOps.color(Formatting.GRAY)), false);

        return serverWorth;
    }

    private static int set(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var value = IntegerArgumentType.getInteger(context, "value");
        final var player = EntityArgumentType.getPlayer(context, "player");

        //noinspection deprecation
        ModComponents.CURRENCY.get(player).setValue(value);
        context.getSource().sendFeedback(TextOps.withColor("numismatic §> balance set to: " + value,
                Currency.GOLD.getNameColor(), TextOps.color(Formatting.GRAY)), false);

        return value;
    }

    private static int get(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var player = EntityArgumentType.getPlayer(context, "player");

        final var balance = ModComponents.CURRENCY.get(player).getValue();
        context.getSource().sendFeedback(TextOps.withColor("numismatic §> balance: " + balance,
                Currency.GOLD.getNameColor(), TextOps.color(Formatting.GRAY)), false);

        return balance;
    }
}
