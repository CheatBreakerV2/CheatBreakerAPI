package com.cheatbreaker.api.handler;

import com.cheatbreaker.api.CheatBreakerAPI;
import com.cheatbreaker.nethandler.server.CBPacketOverrideNametags;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@RequiredArgsConstructor
public class NametagHandler {

    private final CheatBreakerAPI plugin;

    public void overrideNametag(Player target, List<String> nametag, Player viewer) {
        plugin.sendPacket(viewer, new CBPacketOverrideNametags(target.getUniqueId(), nametag));
    }

    public void resetNametag(Player target, Player viewer) {
        plugin.sendPacket(viewer, new CBPacketOverrideNametags(target.getUniqueId(), null));
    }

    public void hideNametag(Player target, Player viewer) {
        plugin.sendPacket(viewer, new CBPacketOverrideNametags(target.getUniqueId(), ImmutableList.of()));
    }
}
