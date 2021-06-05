/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cheatbreaker.api;

import com.cheatbreaker.api.handler.*;
import com.cheatbreaker.api.listener.*;
import com.cheatbreaker.api.net.*;
import com.cheatbreaker.api.net.event.*;
import com.cheatbreaker.nethandler.CBPacket;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class CheatBreakerAPI extends JavaPlugin implements Listener {

    public static final String MESSAGE_CHANNEL = "CB-Client";

    public final Set<UUID> playersRunningCheatBreaker = new HashSet<>();
    public final Set<UUID> playersNotRegistered = new HashSet<>();

    @Setter
    private CBNetHandler netHandlerServer = new CBNetHandlerImpl();
    public final Map<UUID, List<CBPacket>> packetQueue = new HashMap<>();

    @Getter private CooldownHandler cooldownHandler;
    @Getter private HologramHandler hologramHandler;
    @Getter private NametagHandler nametagHandler;
    @Getter private NotificationHandler notificationHandler;
    @Getter private ServerRuleHandler serverRuleHandler;
    @Getter private StaffModuleHandler staffModuleHandler;
    @Getter private TeammatesHandler teammatesHandler;
    @Getter private TitleHandler titleHandler;
    @Getter private VoiceChatHandler voiceChatHandler;
    @Getter private WaypointHandler waypointHandler;
    @Getter private WorldHandler worldHandler;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.cooldownHandler = new CooldownHandler(this);
        this.hologramHandler = new HologramHandler(this);
        this.nametagHandler = new NametagHandler(this);
        this.notificationHandler = new NotificationHandler(this);
        this.serverRuleHandler = new ServerRuleHandler(this);
        this.staffModuleHandler = new StaffModuleHandler(this);
        this.teammatesHandler = new TeammatesHandler(this);
        this.titleHandler = new TitleHandler(this);
        this.voiceChatHandler = new VoiceChatHandler(this);
        this.waypointHandler = new WaypointHandler(this);
        this.worldHandler = new WorldHandler(this);

        Messenger messenger = getServer().getMessenger();
        messenger.registerOutgoingPluginChannel(this, MESSAGE_CHANNEL);
        messenger.registerIncomingPluginChannel(this, MESSAGE_CHANNEL, (channel, player, bytes) -> {
            CBPacket packet = CBPacket.handle(netHandlerServer, bytes, player);
            CBPacketReceivedEvent event = new CBPacketReceivedEvent(player, packet);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled())
                packet.process(netHandlerServer);
        });

        Bukkit.getPluginManager().registerEvents(new RegisterChannelListener(this), this);
        Bukkit.getPluginManager().registerEvents(new UnregisterChannelListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);
    }

    public boolean isRunningCheatBreaker(Player player) {
        return isRunningCheatBreaker(player.getUniqueId());
    }

    public boolean isRunningCheatBreaker(UUID playerUuid) {
        return playersRunningCheatBreaker.contains(playerUuid);
    }

    public Set<Player> getPlayersRunningCheatBreaker() {
        return ImmutableSet.copyOf(playersRunningCheatBreaker
                .stream()
                .map(Bukkit::getPlayer)
                .collect(Collectors.toSet())
        );
    }

    public void isCheatBreakerBanned(UUID playerUuid, Consumer<Boolean> resultListener) {
        resultListener.accept(false);
    }

    /*
     *  This is a boolean to indicate whether or not a CB message was sent.
     *  An example use-case is when you want to send a CheatBreaker
     *  notification if a player is running CheatBreaker, and a chat
     *  message if not.
     */
    public boolean sendPacket(Player player, CBPacket packet) {
        if (isRunningCheatBreaker(player)) {
            player.sendPluginMessage(this, MESSAGE_CHANNEL, CBPacket.getPacketData(packet));
            Bukkit.getPluginManager().callEvent(new CBPacketSentEvent(player, packet));
            return true;
        } else if (!playersNotRegistered.contains(player.getUniqueId())) {
            packetQueue.putIfAbsent(player.getUniqueId(), new ArrayList<>());
            packetQueue.get(player.getUniqueId()).add(packet);
            return false;
        }
        return false;
    }

    public static CheatBreakerAPI getInstance() {
        return JavaPlugin.getPlugin(CheatBreakerAPI.class);
    }
}