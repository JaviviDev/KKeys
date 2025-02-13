package me.javivi.kkeys.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.javivi.kkeys.network.PacketHandler;
import me.javivi.kkeys.network.UpdateKeysPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class KKeysCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kkeys")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("hidekey")
                        .then(Commands.argument("key", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    builder.suggest("key.keyboard.f3");
                                    builder.suggest("key.keyboard.escape");
                                    // Add more key suggestions as needed
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> hideKey(context, StringArgumentType.getString(context, "key"), EntityArgument.getPlayers(context, "targets"), "hide")))))
                .then(Commands.literal("unhidekey")
                        .then(Commands.argument("key", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    builder.suggest("key.keyboard.f3");
                                    builder.suggest("key.keyboard.escape");
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> hideKey(context, StringArgumentType.getString(context, "key"), EntityArgument.getPlayers(context, "targets"), "unhide")))))
                .then(Commands.literal("blockkey")
                        .then(Commands.argument("key", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    builder.suggest("key.keyboard.f3");
                                    builder.suggest("key.keyboard.escape");
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> hideKey(context, StringArgumentType.getString(context, "key"), EntityArgument.getPlayers(context, "targets"), "block")))))
                .then(Commands.literal("unblockkey")
                        .then(Commands.argument("key", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    builder.suggest("key.keyboard.f3");
                                    builder.suggest("key.keyboard.escape");
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> hideKey(context, StringArgumentType.getString(context, "key"), EntityArgument.getPlayers(context, "targets"), "unblock"))))));
    }

    private static int hideKey(CommandContext<CommandSourceStack> context, String key, Collection<ServerPlayer> targets, String action) {
        UpdateKeysPacket packet = new UpdateKeysPacket(key, action);
        for (ServerPlayer player : targets) {
            PacketHandler.sendToClient(packet, player);
        }

        String actionMsg = switch (action) {
            case "hide" -> "oculta";
            case "unhide" -> "visible";
            case "block" -> "bloqueada";
            case "unblock" -> "desbloqueada";
            default -> action;
        };

        context.getSource().sendSuccess(() -> 
            Component.literal("La tecla " + key + " ha sido " + actionMsg + " para " + targets.size() + " jugador(es)"), true);
        return 1;
    }
}