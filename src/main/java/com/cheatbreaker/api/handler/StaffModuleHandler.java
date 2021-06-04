package com.cheatbreaker.api.handler;

import com.cheatbreaker.api.CheatBreakerAPI;
import com.cheatbreaker.api.object.CBNotification;
import com.cheatbreaker.api.object.StaffModule;
import com.cheatbreaker.nethandler.server.CBPacketStaffModState;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class StaffModuleHandler {

    private final CheatBreakerAPI plugin;

    public void setStaffModuleState(Player player, StaffModule module, boolean state) {
        plugin.sendPacket(player, new CBPacketStaffModState(module.name(), state));
    }

    public void giveAllStaffModules(Player player) {
        for (StaffModule module : StaffModule.values()) {
            this.setStaffModuleState(player, module, true);
        }

        plugin.getNotificationHandler().sendNotification(player, new CBNotification("Staff modules enabled", 3, TimeUnit.SECONDS));
    }

    public void disableAllStaffModules(Player player) {
        for (StaffModule module : StaffModule.values()) {
            this.setStaffModuleState(player, module, false);
        }

        plugin.getNotificationHandler().sendNotification(player, new CBNotification("Staff modules disabled", 3, TimeUnit.SECONDS));
    }
}
