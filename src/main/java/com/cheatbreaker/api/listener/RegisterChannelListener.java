package com.cheatbreaker.api.listener;

import com.cheatbreaker.api.CheatBreakerAPI;
import com.cheatbreaker.api.event.PlayerRegisterCBEvent;
import com.cheatbreaker.nethandler.obj.ServerRule;
import com.cheatbreaker.nethandler.server.CBPacketServerRule;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRegisterChannelEvent;

import java.util.ArrayList;

@RequiredArgsConstructor
public class RegisterChannelListener implements Listener {

    private final CheatBreakerAPI plugin;

    @EventHandler
    public void onRegister(PlayerRegisterChannelEvent event) {
        if (!event.getChannel().equals(CheatBreakerAPI.MESSAGE_CHANNEL)) {
            return;
        }

        plugin.playersNotRegistered.remove(event.getPlayer().getUniqueId());
        plugin.playersRunningCheatBreaker.add(event.getPlayer().getUniqueId());

        plugin.getVoiceChatHandler().muteMap.put(event.getPlayer().getUniqueId(), new ArrayList<>());

        if (plugin.voiceEnabled) {
            plugin.sendPacket(event.getPlayer(), new CBPacketServerRule(ServerRule.VOICE_ENABLED, true));
        }

        if (plugin.packetQueue.containsKey(event.getPlayer().getUniqueId())) {
            plugin.packetQueue.get(event.getPlayer().getUniqueId()).forEach(p -> plugin.sendPacket(event.getPlayer(), p));

            plugin.packetQueue.remove(event.getPlayer().getUniqueId());
        }

        plugin.getServer().getPluginManager().callEvent(new PlayerRegisterCBEvent(event.getPlayer()));
        plugin.updateWorld(event.getPlayer());
    }
}
