package ru.craftlogic.warps.common.commands;

import net.minecraft.command.CommandException;
import ru.craftlogic.api.command.CommandBase;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.warps.WarpManager;

public class CommandSpawn extends CommandBase {
    public CommandSpawn() {
        super("spawn", 0, "");
    }

    @Override
    protected void execute(CommandContext ctx) throws Exception {
        WarpManager warpManager = ctx.server().getManager(WarpManager.class);
        if (!warpManager.isEnabled()) {
            throw new CommandException("commands.warp.disabled");
        }

        CommandWarp.teleport(warpManager, ctx, "spawn");
    }
}
