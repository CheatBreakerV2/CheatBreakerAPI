package com.cheatbreaker.api.handler;

import com.cheatbreaker.api.CheatBreakerAPI;
import com.cheatbreaker.nethandler.server.CBPacketUpdateWorld;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class WorldHandler implements Listener {

    private CheatBreakerAPI plugin;
    private final Map<UUID, Function<World, String>> worldIdentifiers = new HashMap<>();

    public WorldHandler(CheatBreakerAPI plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void updateWorld(Player player) {
        String worldIdentifier = getWorldIdentifier(player.getWorld());
        plugin.sendPacket(player, new CBPacketUpdateWorld(worldIdentifier));
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        updateWorld(event.getPlayer());
    }
}
