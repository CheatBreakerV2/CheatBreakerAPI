package com.cheatbreaker.api.handler;

import com.cheatbreaker.api.CheatBreakerAPI;
import com.cheatbreaker.api.object.CBCooldown;
import com.cheatbreaker.nethandler.server.CBPacketCooldown;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class CooldownHandler {

    private final CheatBreakerAPI plugin;

    public void sendCooldown(Player player, CBCooldown cooldown) {
        plugin.sendPacket(player, new CBPacketCooldown(cooldown.getMessage(), cooldown.getDurationMs(), cooldown.getIcon().getId()));
    }

    public void clearCooldown(Player player, CBCooldown cooldown) {
        plugin.sendPacket(player, new CBPacketCooldown(cooldown.getMessage(), 0L, cooldown.getIcon().getId()));
    }
}
