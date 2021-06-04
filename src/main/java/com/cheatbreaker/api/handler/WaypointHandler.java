package com.cheatbreaker.api.handler;

import com.cheatbreaker.api.CheatBreakerAPI;
import com.cheatbreaker.api.object.CBWaypoint;
import com.cheatbreaker.nethandler.shared.CBPacketAddWaypoint;
import com.cheatbreaker.nethandler.shared.CBPacketRemoveWaypoint;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class WaypointHandler {

    private final CheatBreakerAPI plugin;

    public void sendWaypoint(Player player, CBWaypoint waypoint) {
        plugin.sendPacket(player, new CBPacketAddWaypoint(
                waypoint.getName(),
                waypoint.getWorld(),
                waypoint.getColor(),
                waypoint.getX(),
                waypoint.getY(),
                waypoint.getZ(),
                waypoint.isForced(),
                waypoint.isVisible()
        ));
    }

    public void removeWaypoint(Player player, CBWaypoint waypoint) {
        plugin.sendPacket(player, new CBPacketRemoveWaypoint(
                waypoint.getName(),
                waypoint.getWorld()
        ));
    }
}
