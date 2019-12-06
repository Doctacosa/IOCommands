package com.interordi.iocommands;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PlayerListener implements Listener {

	private IOCommands plugin;
	
	
	public PlayerListener(IOCommands plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		this.plugin.setFlightStatus(null, event.getPlayer(), 0);
	}

	
	
	@EventHandler
	public void onGamemodeChange(PlayerGameModeChangeEvent event) {
		//If the new gamemode comes with built-in flight, disable our internal flight status
		if (event.getNewGameMode() == GameMode.CREATIVE || event.getNewGameMode() == GameMode.SPECTATOR) {
			plugin.resetPlayerFlight(event.getPlayer());
		}
	}
	
	
	@EventHandler
	public void onPlayerSpawnLocationEvent(PlayerSpawnLocationEvent event) {
		//Get spawning events to set the player's position properly, if possible
		
		Player player = event.getPlayer();
		
		//Ignore players with a valid spawn position
		if (player.hasPlayedBefore())
			return;
		
		Location spawn = plugin.worldSpawns.getSpawn(player.getLocation().getWorld().getName());
		if (spawn != null) {
			event.setSpawnLocation(spawn);
		}
	}
}
