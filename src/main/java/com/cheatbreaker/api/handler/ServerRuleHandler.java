package com.cheatbreaker.api.handler;

import com.cheatbreaker.api.CheatBreakerAPI;
import com.cheatbreaker.api.object.MinimapStatus;
import com.cheatbreaker.nethandler.obj.ServerRule;
import com.cheatbreaker.nethandler.server.CBPacketServerRule;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class ServerRuleHandler {

    private final CheatBreakerAPI plugin;

    public void setMinimapStatus(Player player, MinimapStatus status) {
        plugin.sendPacket(player, new CBPacketServerRule(ServerRule.MINIMAP_STATUS, status.name()));
    }

    public void setCompetitiveGame(Player player, boolean isCompetitive) {
        plugin.sendPacket(player, new CBPacketServerRule(ServerRule.COMPETITIVE_GAMEMODE, isCompetitive));
    }

    public void setVoiceEnabled(boolean enabled) {
        plugin.getVoiceChatHandler().voiceEnabled = enabled;
    }
}
