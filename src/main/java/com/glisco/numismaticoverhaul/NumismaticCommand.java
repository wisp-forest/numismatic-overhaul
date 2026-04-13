package com.glisco.numismaticoverhaul;

import com.glisco.numismaticoverhaul.currency.*;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.offline.OfflineDataLookup;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NumismaticCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var root = literal("numismatic").build();

        var balance = literal("balance").executes(NumismaticCommand::getSelf).build();
        var detailedBalance = argument("player", EntityArgumentType.players())
            .requires(src -> src.hasPermissionLevel(2))
            .then(literal("get").executes(NumismaticCommand::get))
            .then(longSubcommand("set", "value", NumismaticCommand::set))
            .then(longSubcommand("add", "amount", NumismaticCommand.modify(1)))
            .then(longSubcommand("subtract", "amount", NumismaticCommand.modify(-1)))
            .build();
        var serverWorth = literal("serverworth").executes(NumismaticCommand::serverWorth).build();
        balance.addChild(detailedBalance);

        root.addChild(balance);
        root.addChild(serverWorth);

        dispatcher.getRoot().addChild(root);
    }

    private static int getSelf(CommandContext<ServerCommandSource> src) {
        var player = src.getSource().getPlayer();
        if (player == null) return 0;

        var coins = ModComponents.CURRENCY.get(player).getValue();

        printBalance(src.getSource(), coins);

        return (int) coins;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> longSubcommand(String name, String argName, Command<ServerCommandSource> command) {
        return literal(name).then(argument(argName, LongArgumentType.longArg(0)).executes(command));
    }

    private static void printBalance(ServerCommandSource src, long balance) {
        var values = CurrencyResolver.splitValues(balance);
        src.sendFeedback(() -> Text.translatable("chat.numismatic-overhaul.balance", values[0], values[1], values[2]), false);
    }

    @SuppressWarnings("ConstantConditions")
    private static int serverWorth(CommandContext<ServerCommandSource> context) {
        final var playerManager = context.getSource().getServer().getPlayerManager();

        var onlineUUIDs = playerManager.getPlayerList().stream().map(Entity::getUuid).toList();
        var offlineUUIDs = OfflineDataLookup.savedPlayers().stream().filter(uuid -> !onlineUUIDs.contains(uuid)).toList();

        long serverWorth = 0;
        for (var onlineId : onlineUUIDs) {
            serverWorth += ModComponents.CURRENCY.get(playerManager.getPlayer(onlineId)).getValue();
        }

        for (var offlineId : offlineUUIDs) {
            serverWorth += OfflineDataLookup.get(offlineId).getCompound("cardinal_components").getCompound("numismatic-overhaul:currency").getLong("Value");
        }

        long finalServerWorth = serverWorth;
        context.getSource().sendFeedback(() -> TextOps.withColor("numismatic §> server net worth: " + finalServerWorth,
                Currency.GOLD.getNameColor(), TextOps.color(Formatting.GRAY)), false);

        return (int) serverWorth;
    }

    private static int set(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var value = LongArgumentType.getLong(context, "value");
        final var players = EntityArgumentType.getPlayers(context, "player");

        for (var player : players) {
            //noinspection deprecation
            ModComponents.CURRENCY.get(player).setValue(value);
            context.getSource().sendFeedback(() -> TextOps.withColor("numismatic §> balance of " + player.getEntityName() + " set to: " + value,
                    Currency.GOLD.getNameColor(), TextOps.color(Formatting.GRAY)), false);
        }

        return CurrencyConverter.asInt(value);
    }

    private static int get(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var players = EntityArgumentType.getPlayers(context, "player");
        long totalBalance = 0;

        for (var player : players) {
            final long balance = ModComponents.CURRENCY.get(player).getValue();
            totalBalance += balance;

            context.getSource().sendFeedback(() -> TextOps.withColor("numismatic §> balance of " + player.getEntityName() + ": " + balance,
                    Currency.GOLD.getNameColor(), TextOps.color(Formatting.GRAY)), false);
        }

        return CurrencyConverter.asInt(totalBalance);
    }

    private static Command<ServerCommandSource> modify(long multiplier) {
        return context -> {
            final var amount = LongArgumentType.getLong(context, "amount");
            final var players = EntityArgumentType.getPlayers(context, "player");

            long lastValue = 0;

            for (var player : players) {
                final var currencyComponent = ModComponents.CURRENCY.get(player);

                currencyComponent.silentModify(amount * multiplier);
                lastValue = currencyComponent.getValue();

                context.getSource().sendFeedback(() -> TextOps.withColor("numismatic §> balance of " + player.getEntityName() + " set to: " + currencyComponent.getValue(),
                        Currency.GOLD.getNameColor(), TextOps.color(Formatting.GRAY)), false);
            }

            return CurrencyConverter.asInt(lastValue);
        };
    }
}
