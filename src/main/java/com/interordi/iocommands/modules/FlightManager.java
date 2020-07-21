package com.interordi.iocommands.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.interordi.iocommands.IOCommands;

public class FlightManager
{
	private IOCommands plugin;
	private Map< UUID, Boolean > status;
	
	
	public FlightManager(IOCommands plugin) {
		this.plugin = plugin;
		this.status = new HashMap< UUID, Boolean >();
	}
	
	
	//Set (1), unset(0) or toggle(-1) flight status on the given player
	public void setFlightStatus(Player source, Player player, int status) {
		//Get current flight value
		boolean curStatus = false;
		boolean newStatus = false;
		
		//Get previous status
		curStatus = getFlightStatus(player);
		
		if (status == -1) {
			newStatus = !curStatus;
		} else if (status == 0) {
			newStatus = false;
		} else if (status == 1) {
			newStatus = true;
		}
		
		final boolean finalStatus = newStatus;
		new BukkitRunnable() {
			public void run() {
				player.setAllowFlight(finalStatus);				
			}
		}.runTask(this.plugin);
		
		if (newStatus != curStatus) {
			if (newStatus) {
				if (source != null && source.getUniqueId() != player.getUniqueId())
					source.sendMessage("§b" + player.getDisplayName() + " can now fly!");
				player.sendMessage("§bYou can now fly!");
			}
			else {
				if (source != null && source.getUniqueId() != player.getUniqueId())
					source.sendMessage("§b" + player.getDisplayName() + " can no longer fly!");
				player.sendMessage("§bYou can no longer fly!");
			}
		}
		
		this.status.put(player.getUniqueId(), newStatus);
	}


	//Reset our internal flag of a player's flight status
	public void resetFlightStatus(Player player) {
		UUID uuid = player.getUniqueId();
		if (!status.containsKey(uuid))
			return;
		status.put(uuid, false);
	}
	
	
	//Get the flight status of the given player
	private boolean getFlightStatus(Player player) {
		boolean status = false;
		UUID id = player.getUniqueId();
		
		if (this.status.containsKey(id))
			status = this.status.get(id);
		
		return status;
	}
}
