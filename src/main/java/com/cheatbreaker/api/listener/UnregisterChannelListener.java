package com.cheatbreaker.api.listener;

import com.cheatbreaker.api.CheatBreakerAPI;
import com.cheatbreaker.api.event.PlayerUnregisterCBEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerUnregisterChannelEvent;

@RequiredArgsConstructor
public class UnregisterChannelListener implements Listener {

    private final CheatBreakerAPI plugin;

    @EventHandler
    public void onUnregister(PlayerUnregisterChannelEvent event) {
        if (event.getChannel().equals(CheatBreakerAPI.MESSAGE_CHANNEL)) {
            plugin.playersRunningCheatBreaker.remove(event.getPlayer().getUniqueId());
            plugin.getVoiceChatHandler().getPlayerActiveChannels().remove(event.getPlayer().getUniqueId());
            plugin.getVoiceChatHandler().muteMap.remove(event.getPlayer().getUniqueId());

            plugin.getServer().getPluginManager().callEvent(new PlayerUnregisterCBEvent(event.getPlayer()));
        }
    }
}