package ru.craftlogic.warps;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.command.CommandException;
import net.minecraft.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.command.CommandContext;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.util.ConfigurableManager;
import ru.craftlogic.api.world.CommandSender;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.warps.common.commands.*;
import ru.craftlogic.common.command.CommandManager;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class WarpManager extends ConfigurableManager {
    private static final Logger LOGGER = LogManager.getLogger("WarpManager");

    private final Map<String, Warp> warps = new HashMap<>();
    private boolean enabled;

    public WarpManager(Server server, Path settingsDirectory) {
        super(server, settingsDirectory.resolve("warps.json"), LOGGER);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    protected String getModId() {
        return CraftWarps.MOD_ID;
    }

    @Override
    protected void load(JsonObject config) {
        this.enabled = JsonUtils.getBoolean(config, "enabled", false);
        JsonObject warps = JsonUtils.getJsonObject(config, "warps", new JsonObject());
        for (Map.Entry<String, JsonElement> entry : warps.entrySet()) {
            String name = entry.getKey();
            JsonObject value = (JsonObject) entry.getValue();
            this.warps.put(name, new Warp(name, value));
        }
    }

    @Override
    protected void save(JsonObject config) {
        config.addProperty("enabled", this.enabled);
        JsonObject warps = new JsonObject();
        for (Map.Entry<String, Warp> entry : this.warps.entrySet()) {
            warps.add(entry.getKey(), entry.getValue().toJson());
        }
        config.add("warps", warps);
    }

    @Override
    public void registerCommands(CommandManager commandManager) {
        if (server.isDedicated()) {
            commandManager.registerArgumentType("Warp", false, ctx -> warps.keySet());
            commandManager.registerCommand(new CommandWarp());
            commandManager.registerCommand(new CommandSpawn());
        }
    }

    public Warp getWarp(String name) {
        return warps.get(name);
    }

    @Nonnull
    public Warp getOwnedWarp(String name, CommandContext ctx) throws CommandException {
        Warp warp = getWarp(name);
        if (warp == null) {
            throw new CommandException("commands.warp.not-found", name);
        } else if (!ctx.sender().hasPermission("commands.warp.delete.admin") && !warp.getOwner().equals(ctx.senderAsPlayer().getId())) {
            throw new CommandException("commands.warp.not-an-owner");
        } else {
            return warp;
        }
    }

    public Warp createWarp(String name, Location location, UUID owner) throws IOException {
        Warp warp = new Warp(name, location, owner, true, -1F, new HashSet<>());
        warps.put(name, warp);
        save(true);
        return warp;
    }

    public Warp deleteWarp(String name) throws IOException {
        Warp warp = warps.remove(name);
        if (warp != null) {
            save(true);
        }
        return warp;
    }
}
