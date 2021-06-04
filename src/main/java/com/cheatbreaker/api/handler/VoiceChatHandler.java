package com.cheatbreaker.api.handler;

import com.cheatbreaker.api.CheatBreakerAPI;
import com.cheatbreaker.api.voice.VoiceChannel;
import com.cheatbreaker.nethandler.server.CBPacketDeleteVoiceChannel;
import com.cheatbreaker.nethandler.server.CBPacketVoiceChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class VoiceChatHandler {

    private final CheatBreakerAPI plugin;

    @Getter private List<VoiceChannel> voiceChannels = new ArrayList<>();
    @Getter private final Map<UUID, VoiceChannel> playerActiveChannels = new HashMap<>();
    public final Map<UUID, List<UUID>> muteMap = new HashMap<>();

    public void createVoiceChannels(VoiceChannel... voiceChannels) {
        this.voiceChannels.addAll(Arrays.asList(voiceChannels));
        for (VoiceChannel channel : voiceChannels) {
            for (Player player : channel.getPlayersInChannel()) {
                sendVoiceChannel(player, channel);
            }
        }
    }

    public void deleteVoiceChannel(VoiceChannel channel) {
        this.voiceChannels.removeIf(c -> {
            boolean remove = c == channel;
            if (remove) {
                channel.validatePlayers();
                for (Player player : channel.getPlayersInChannel()) {
                    plugin.sendPacket(player, new CBPacketDeleteVoiceChannel(channel.getUuid()));
                    if (getPlayerActiveChannels().get(player.getUniqueId()) == channel) {
                        getPlayerActiveChannels().remove(player);
                    }
                }
            }
            return remove;
        });
    }

    public void deleteVoiceChannel(UUID channelUUID) {
        getChannel(channelUUID).ifPresent(c -> deleteVoiceChannel(c));
    }

    public List<VoiceChannel> getPlayerChannels(Player player) {
        return this.voiceChannels.stream().filter(channel -> channel.hasPlayer(player)).collect(Collectors.toList());
    }

    public void sendVoiceChannel(Player player, VoiceChannel channel) {
        channel.validatePlayers();
        plugin.sendPacket(player, new CBPacketVoiceChannel(channel.getUuid(), channel.getName(), channel.toPlayersMap(), channel.toListeningMap()));
    }

    public void setActiveChannel(Player player, UUID uuid) {
        getChannel(uuid).ifPresent(channel -> setActiveChannel(player, channel));
    }

    public Optional<VoiceChannel> getChannel(UUID uuid) {
        return voiceChannels.stream().filter(channel -> channel.getUuid().equals(uuid)).findFirst();
    }

    public void setActiveChannel(Player player, VoiceChannel channel) {
        channel.setActive(player);
    }

    public void toggleVoiceMute(Player player, UUID other) {
        if (!muteMap.get(player.getUniqueId()).removeIf(uuid -> uuid.equals(other))) {
            muteMap.get(player.getUniqueId()).add(other);
        }
    }

    public boolean playerHasPlayerMuted(Player player, Player other) {
        return muteMap.get(other.getUniqueId()).contains(player.getUniqueId());
    }

}
