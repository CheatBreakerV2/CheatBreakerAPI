package com.cheatbreaker.api.handler;

import com.cheatbreaker.api.CheatBreakerAPI;
import com.cheatbreaker.api.object.TitleType;
import com.cheatbreaker.nethandler.server.CBPacketTitle;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.time.Duration;

@RequiredArgsConstructor
public class TitleHandler {

    private final CheatBreakerAPI plugin;

    public void sendTitle(Player player, TitleType type, String message, Duration displayTime) {
        this.sendTitle(player, type, message, Duration.ofMillis(500), displayTime, Duration.ofMillis(500));
    }

    public void sendTitle(Player player, TitleType type, String message, Duration displayTime, float scale) {
        this.sendTitle(player, type, message, Duration.ofMillis(500), displayTime, Duration.ofMillis(500), scale);
    }

    public void sendTitle(Player player, TitleType type, String message, Duration fadeInTime, Duration displayTime, Duration fadeOutTime) {
        this.sendTitle(player, type, message, fadeInTime, displayTime, fadeOutTime, 1F);
    }

    public void sendTitle(Player player, TitleType type, String message, Duration fadeInTime, Duration displayTime, Duration fadeOutTime, float scale) {
        plugin.sendPacket(player, new CBPacketTitle(type.name().toLowerCase(), message, scale, displayTime.toMillis(), fadeInTime.toMillis(), fadeOutTime.toMillis()));
    }
}
