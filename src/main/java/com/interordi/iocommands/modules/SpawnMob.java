package com.interordi.iocommands.modules;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.interordi.iocommands.IOCommands;

public class SpawnMob {
	
	IOCommands plugin;
	
	
	public SpawnMob(IOCommands plugin) {
		this.plugin = plugin;
	}
	
	
	//Spawn one or many mobs as requested
	public boolean spawn(CommandSender sender, String mob, int amount, double spread, Location pos) {

		EntityType mobType = EntityType.fromName(mob);
		if (mobType == null) {
			sender.sendMessage(ChatColor.RED + "Mob type not found.");
			return false;
		}

		if (amount <= 0) {
			sender.sendMessage(ChatColor.RED + "You need at least one!");
			return false;
		}

		Location mobPos = pos.clone();
		for (int i = 0; i < amount; i++) {
			if (spread > 0) {
				double orientation = Math.random() * Math.PI * 2;
				double distance = Math.random() * spread;
				mobPos.setX(pos.getX() + Math.sin(orientation) * distance);
				mobPos.setZ(pos.getZ() + Math.cos(orientation) * distance);
			}
			Entity entity = pos.getWorld().spawnEntity(mobPos, mobType);
			//zombie.setCustomName("Undead Warrior"); // Example of customization
			//zombie.setCustomNameVisible(true);
		}

		return true;
	}

}
