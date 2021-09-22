package ru.craftlogic.warps.common;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import ru.craftlogic.api.event.server.ServerAddManagersEvent;
import ru.craftlogic.api.network.AdvancedMessageHandler;
import ru.craftlogic.warps.WarpManager;
import ru.craftlogic.warps.network.message.MessageClearChat;
import ru.craftlogic.util.ReflectiveUsage;

import static ru.craftlogic.warps.CraftWarps.NETWORK;

@ReflectiveUsage
public class ProxyCommon extends AdvancedMessageHandler {
    public void preInit() {

    }

    public void init() {

    }

    public void postInit() {

    }

    @SubscribeEvent
    public void onServerAddManagers(ServerAddManagersEvent event) {
        event.addManager(WarpManager.class, WarpManager::new);
    }
}
