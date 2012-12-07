package me.yukonapplegeek.regionalweather;

import java.util.HashMap;

import net.minecraft.server.Packet70Bed;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionalWeather extends JavaPlugin implements Listener {
    HashMap<String, Boolean> raining = new HashMap<String, Boolean>();

    public void onDisable() {
        // TODO: Place any custom disable code here.
    }

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            this.getServer().getLogger().severe("WorldGuard not found!");
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        String username = event.getPlayer().getName();
        Location loc = event.getTo();
        WorldGuardPlugin guard = getWorldGuard();
        Vector v = new Vector(loc.getX(), loc.getY(), loc.getZ());
        RegionManager manager = guard.getRegionManager(loc.getWorld());
        ApplicableRegionSet set = manager.getApplicableRegions(v);
        for (ProtectedRegion region : set) {
            if(region.getId().endsWith("rain")){
                if (raining.containsKey(username)) {
                    if (!raining.get(username)) {
                        raining.put(username, true);
                        ((CraftPlayer)event.getPlayer()).getHandle().netServerHandler.sendPacket(new Packet70Bed(1, 0));
                    }
                } else {
                    raining.put(username, true);
                    ((CraftPlayer)event.getPlayer()).getHandle().netServerHandler.sendPacket(new Packet70Bed(1, 0));
                }
            }
        }
        if (set.size() == 0) {
            if (raining.containsKey(username)) {
                ((CraftPlayer)event.getPlayer()).getHandle().netServerHandler.sendPacket(new Packet70Bed(2, 0));
                raining.remove(username);
            }
        }
    }
}