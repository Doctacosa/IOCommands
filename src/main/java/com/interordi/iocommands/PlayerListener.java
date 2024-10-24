package com.interordi.iocommands;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayerListener implements Listener, Runnable {

	private IOCommands plugin;
	private Set< UUID > posSticky;
	private Set< UUID > posActive;

	
	public PlayerListener(IOCommands plugin) {
		this.plugin = plugin;
		this.posSticky = new HashSet< UUID >();
		this.posActive = new HashSet< UUID >();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);


		//Refresh the display for sticky users every second
		Bukkit.getScheduler().scheduleSyncRepeatingTask(IOCommands.instance, this, 10 * 20L, 1 * 20L);
	}


	//Set if a player's position would be tracked
	public void setPositionStatus(Player player, int status) {
		if (status == 2) {
			posSticky.add(player.getUniqueId());
			posActive.add(player.getUniqueId());
			displayPosition(player, player.getLocation());
		} if (status == 1) {
			posSticky.remove(player.getUniqueId());
			posActive.add(player.getUniqueId());
			displayPosition(player, player.getLocation());
		} else if (status == 0) {
			posSticky.remove(player.getUniqueId());
			posActive.remove(player.getUniqueId());
		} else if (status == -1) {
			displayPosition(player, player.getLocation());
		}
	}


	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (plugin.getRestart().isShuttingDown())
			event.getPlayer().kickPlayer("The server is being prepared for a restart, please try again later.");

		//Display any relevant login message to the incoming player
		for (String permission : plugin.getLoginMessages().keySet()) {
			if (event.getPlayer().hasPermission(permission)) {
				for (String message : plugin.getLoginMessages().get(permission))
					event.getPlayer().sendMessage(message);
			}
		}
	}
	

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.plugin.setFlightStatus(null, event.getPlayer(), 0);
		posSticky.remove(event.getPlayer().getUniqueId());
		posActive.remove(event.getPlayer().getUniqueId());
	}

	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!posActive.contains(event.getPlayer().getUniqueId()))
			return;
		
		//If subscribed to move notifications, update
		if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
			event.getFrom().getBlockY() != event.getTo().getBlockY() ||
			event.getFrom().getBlockZ() != event.getTo().getBlockZ() ||
			Math.abs(event.getFrom().getYaw() - event.getTo().getYaw()) >= 0.5) {

			displayPosition(event.getPlayer(), event.getTo());
		}
	}


	//Keep on display by forcing a refresh
	public void run() {
		for (UUID uuid : posSticky) {
			Player player = Bukkit.getPlayer(uuid);

			displayPosition(player, player.getLocation());
		}
	}


	public void displayPosition(Player player, Location loc) {
		String direction = "";
		float yaw = ((loc.getYaw() % 360) + 360) % 360;
		if (yaw > 180)
			yaw -= 360;
		if (yaw > -22.5 && yaw <= 22.5)			direction = "south";
		else if (yaw > 22.5 && yaw <= 67.5)		direction = "southwest";
		else if (yaw > 67.5 && yaw <= 112.5)	direction = "west";
		else if (yaw > 112.5 && yaw <= 157.5)	direction = "northwest";
		else if (yaw > 157.5 || yaw <= -157.5)	direction = "north";
		else if (yaw > -157.5 && yaw <= -112.5)	direction = "northeast";
		else if (yaw > -112.5 && yaw <= -67.5)	direction = "east";
		else if (yaw > -67.5 && yaw <= -22.5)	direction = "southeast";
		String output = loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + "; " + direction;
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(output));
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
