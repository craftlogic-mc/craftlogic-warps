package ru.craftlogic.warps.event;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.warps.Warp;

@Cancelable
public class PlayerWarpEvent extends PlayerEvent {
    public final Player player;
    public final Warp warp;

    public PlayerWarpEvent(Player player, Warp warp) {
        super(player.getEntity());
        this.player = player;
        this.warp = warp;
    }
}
