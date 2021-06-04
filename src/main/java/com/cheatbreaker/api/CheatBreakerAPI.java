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

import com.cheatbreaker.api.event.PlayerRegisterCBEvent;
import com.cheatbreaker.api.event.PlayerUnregisterCBEvent;
import com.cheatbreaker.api.handler.*;
import com.cheatbreaker.api.listener.ConnectionListener;
import com.cheatbreaker.api.listener.RegisterChannelListener;
import com.cheatbreaker.api.listener.UnregisterChannelListener;
import com.cheatbreaker.api.net.CBNetHandler;
import com.cheatbreaker.api.net.CBNetHandlerImpl;
import com.cheatbreaker.api.net.event.CBPacketReceivedEvent;
import com.cheatbreaker.api.net.event.CBPacketSentEvent;
import com.cheatbreaker.api.object.*;
import com.cheatbreaker.api.voice.VoiceChannel;
import com.cheatbreaker.nethandler.CBPacket;
import com.cheatbreaker.nethandler.obj.ServerRule;
import com.cheatbreaker.nethandler.server.*;
import com.cheatbreaker.nethandler.shared.CBPacketAddWaypoint;
import com.cheatbreaker.nethandler.shared.CBPacketRemoveWaypoint;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CheatBreakerAPI extends JavaPlugin implements Listener {

    public static final String MESSAGE_CHANNEL = "CB-Client";

    @Getter
    private static CheatBreakerAPI instance;
    public final Set<UUID> playersRunningCheatBreaker = new HashSet<>();
    public final Set<UUID> playersNotRegistered = new HashSet<>();

    @Setter
    private CBNetHandler netHandlerServer = new CBNetHandlerImpl();

    public boolean voiceEnabled;

    public final Map<UUID, List<CBPacket>> packetQueue = new HashMap<>();
    private final Map<UUID, Function<World, String>> worldIdentifiers = new HashMap<>();

    @Getter
    private CooldownHandler cooldownHandler;
    @Getter
    private HologramHandler hologramHandler;
    @Getter
    private NametagHandler nametagHandler;
    @Getter
    private NotificationHandler notificationHandler;
    @Getter
    private ServerRuleHandler serverRuleHandler;
    @Getter
    private StaffModuleHandler staffModuleHandler;
    @Getter
    private TeammatesHandler teammatesHandler;
    @Getter
    private TitleHandler titleHandler;
    @Getter
    private VoiceChatHandler voiceChatHandler;
    @Getter
    private WaypointHandler waypointHandler;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

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

        Messenger messenger = getServer().getMessenger();
        messenger.registerOutgoingPluginChannel(this, MESSAGE_CHANNEL);
        messenger.registerIncomingPluginChannel(this, MESSAGE_CHANNEL, (channel, player, bytes) -> {
            CBPacket packet = CBPacket.handle(netHandlerServer, bytes, player);
            CBPacketReceivedEvent event;
            Bukkit.getPluginManager().callEvent(event = new CBPacketReceivedEvent(player, packet));
            if (!event.isCancelled()) {
                packet.process(netHandlerServer);
            }
        });

        Bukkit.getPluginManager().registerEvents(new RegisterChannelListener(this), this);
        Bukkit.getPluginManager().registerEvents(new UnregisterChannelListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);

        getServer().getPluginManager().registerEvents(
                new Listener() {
                    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
                    public void onWorldChange(PlayerChangedWorldEvent event) { updateWorld(event.getPlayer()); }
                }, this);
    }

    public void updateWorld(Player player) {
        String worldIdentifier = getWorldIdentifier(player.getWorld());
        sendPacket(player, new CBPacketUpdateWorld(worldIdentifier));
    }

    public String getWorldIdentifier(World world) {
        String worldIdentifier = world.getUID().toString();

        if (worldIdentifiers.containsKey(world.getUID())) {
            worldIdentifier = worldIdentifiers.get(world.getUID()).apply(world);
        }

        return worldIdentifier;
    }

    public void registerWorldIdentifier(World world, Function<World, String> identifier) {
        worldIdentifiers.put(world.getUID(), identifier);
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

}