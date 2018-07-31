package com.interordi.iocommands;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
}
