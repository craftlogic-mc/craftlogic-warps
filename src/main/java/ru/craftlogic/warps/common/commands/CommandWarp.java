package ru.craftlogic.warps.common.commands;

import net.minecraft.command.CommandException;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.warps.Warp;
import ru.craftlogic.warps.WarpManager;
import ru.craftlogic.warps.event.PlayerWarpEvent;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class CommandWarp extends CommandBase {
    public CommandWarp() {
        super("warp", 1,
            "<name:Warp> create|delete|public|private",
            "<name:Warp> invite|expel|transfer <target:Player>",
            "<name:Warp>"
        );
    }

    @Override
    protected void execute(CommandContext ctx) throws Exception {
        WarpManager warpManager = ctx.server().getManager(WarpManager.class);
        if (!warpManager.isEnabled()) {
            throw new CommandException("commands.warp.disabled");
        }

        String name = ctx.get("name").asString().toLowerCase(Locale.ROOT);

        if (ctx.hasAction(0)) {
            switch (ctx.action(0)) {
                case "create": {
                    Player player = ctx.senderAsPlayer();
                    if (ctx.checkPermission(true, "commands.warp.create", 1)) {
                        if (warpManager.getWarp(name) != null) {
                            throw new CommandException("commands.warp.create.exists", name);
                        }
                        Warp warp = warpManager.createWarp(name, player.getLocation(), player.getId());
                        if (warp != null) {
                            ctx.sendNotification(
                                Text.translation("commands.warp.create.success").green()
                                    .arg(name, Text::darkGreen)
                            );
                        }
                    }
                    break;
                }
                case "delete": {
                    Warp warp = warpManager.getOwnedWarp(name, ctx);
                    if (ctx.checkPermission(true, "commands.warp.delete", 1)) {
                        ctx.sender().sendQuestionIfPlayer("delete-warp", new TextComponentTranslation("commands.warp.delete.question", name), 60, choice -> {
                            if (choice) {
                                try {
                                    if (warpManager.deleteWarp(warp.getName()) == warp) {
                                        ctx.sendNotification(
                                            Text.translation("commands.warp.delete.success").yellow()
                                                .arg(name, Text::gold)
                                        );
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    }
                    break;
                }
                case "invite": {
                    OfflinePlayer target = ctx.get("target").asOfflinePlayer();
                    Warp warp = warpManager.getOwnedWarp(name, ctx);
                    if (ctx.checkPermission(true, "commands.warp.invite", 1)) {
                        if (warp.isPublic()) {
                            throw new CommandException("commands.warp.invite.public");
                        }
                        if (warp.addInvited(target.getId())) {
                            warpManager.save(true);
                            ctx.sendMessage(
                                Text.translation("commands.warp.invite.success").green()
                                    .arg(target.getName(), Text::darkGreen)
                            );
                        } else {
                            throw new CommandException("commands.warp.invite.already", target.getName());
                        }
                    }
                    break;
                }
                case "expel": {
                    OfflinePlayer target = ctx.get("target").asOfflinePlayer();
                    Warp warp = warpManager.getOwnedWarp(name, ctx);
                    if (ctx.checkPermission(true, "commands.warp.expel", 1)) {
                        if (warp.isPublic()) {
                            throw new CommandException("commands.warp.expel.public");
                        }
                        if (warp.removeInvited(target.getId())) {
                            warpManager.save(true);
                            ctx.sendMessage(
                                Text.translation("commands.warp.expel.success").yellow()
                                    .arg(target.getName(), Text::gold)
                            );
                        } else {
                            throw new CommandException("commands.warp.expel.already", target.getName());
                        }
                    }
                    break;
                }
                case "transfer": {
                    OfflinePlayer target = ctx.get("target").asOfflinePlayer();
                    boolean isPlayer = ctx.sender() instanceof Player;
                    if (isPlayer && ctx.senderAsPlayer().getId().equals(target.getId())) {
                        throw new CommandException("commands.warp.transfer.yourself");
                    }
                    Warp warp = warpManager.getOwnedWarp(name, ctx);
                    if (ctx.checkPermission(true, "commands.warp.transfer", 1)) {
                        Callable<Void> task = () -> {
                            warp.setOwner(target.getId());
                            warpManager.save(true);
                            ctx.sendMessage(
                                Text.translation("commands.warp.transfer.success").yellow()
                                    .arg(name, Text::gold)
                                    .arg(target.getName(), Text::gold)
                            );
                            return null;
                        };
                        ctx.sender().sendQuestionIfPlayer("transfer-warp", new TextComponentTranslation("commands.warp.transfer.question", name), 60, choice -> {
                            if (choice) {
                                try {
                                    task.call();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    }
                    break;
                }
                case "public": {
                    Warp warp = warpManager.getOwnedWarp(name, ctx);
                    if (ctx.checkPermission(true, "commands.warp.public", 1)) {
                        if (warp.isPublic()) {
                            throw new CommandException("commands.warp.public.already", name);
                        }
                        warp.setPublic(true);
                        warpManager.save(true);
                        ctx.sendNotification(
                            Text.translation("commands.warp.public.success").green()
                                .arg(name, Text::darkGreen)
                        );
                    }
                    break;
                }
                case "private": {
                    Warp warp = warpManager.getOwnedWarp(name, ctx);
                    if (ctx.checkPermission(true, "commands.warp.private", 1)) {
                        if (!warp.isPublic()) {
                            throw new CommandException("commands.warp.private.already", name);
                        }
                        warp.setPublic(false);
                        warpManager.save(true);
                        ctx.sendNotification(
                            Text.translation("commands.warp.private.success").green()
                                .arg(name, Text::darkGreen)
                        );
                    }
                    break;
                }
            }
        } else {
            teleport(warpManager, ctx, name);
        }
    }

    public static void teleport(WarpManager warpManager, CommandContext ctx, String name) throws CommandException {
        Warp warp = warpManager.getWarp(name);
        if (warp == null) {
            throw new CommandException("commands.warp.not-found", name);
        }
        Player player = ctx.senderAsPlayer();
        Location location = warp.getLocation();
        boolean interdimensional = !Objects.equals(player.getWorldName(), location.getWorldName());
        if (interdimensional && !player.hasPermission("commands.warp.teleport.dimension")) {
            throw new CommandException("commands.warp.teleport.dimension", name);
        }
        if (!warp.isPublic() && !warp.isInvited(player.getId()) && !player.hasPermission("commands.warp.teleport.private")) {
            throw new CommandException("commands.warp.teleport.private", name);
        }
        if (!MinecraftForge.EVENT_BUS.post(new PlayerWarpEvent(player, warp))) {
            boolean spawn = name.equals("spawn");
            Consumer<Server> callback = server -> ctx.sendMessage(Text.translation("commands.warp." + (spawn ? "spawn" : "teleport")).green()
                .arg(name, Text::darkGreen));
            player.teleportDelayed(callback, "warp", Text.translation("tooltip.warp_teleport"), location, interdimensional ? 15 : 5, true);
        }
    }
}
