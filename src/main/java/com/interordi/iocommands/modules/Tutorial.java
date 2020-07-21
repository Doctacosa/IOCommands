package com.interordi.iocommands.modules;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import com.interordi.iocommands.IOCommands;

public class Tutorial {
	
	IOCommands plugin;
	
	private String filePath = "plugins/IOCommands/tutorial.yml";
	private Map< UUID, Location > locations;
	
	
	public Tutorial(IOCommands plugin) {
		this.plugin = plugin;
		locations = new HashMap< UUID, Location >();
		loadTutorial();
	}
	
	
	public void onCommand(Player player, boolean exit) {
		
		if (!player.getWorld().getName().equals("world_cimmeria")) {
			player.sendMessage("§aThe tutorial is in Cimmeria! Do /lobby and head there.");
			return;
		}
		
		//Exit
		if (exit) {
			Location loc = locations.get(player.getUniqueId());
			if (loc == null) {
				System.out.println("No destination found!");
				return;
			}
			player.teleport(loc);
			return;
		}
		
		//Check
		if (locations.containsKey(player.getUniqueId())) {
			player.sendMessage("§aYou have already gone through the tutorial!");
			return;
		}
		
		//Enter
		locations.put(player.getUniqueId(), player.getLocation());
		plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "warp " + player.getName()  + " tutorial");
		saveTutorial();
	}
	
	
	//Get the list from the file
	public void loadTutorial() {
		File statsFile = new File(this.filePath);
		FileConfiguration statsAccess = YamlConfiguration.loadConfiguration(statsFile);
		
		ConfigurationSection playersData = statsAccess.getConfigurationSection("players");
		if (playersData == null) {
			plugin.getLogger().info("ERROR: YML section not found");
			return;	//Nothing yet, exit
		}
		Set< String > cs = playersData.getKeys(false);
		if (cs == null) {
			plugin.getLogger().info("ERROR: Couldn't get player keys");
			return;	//No players found, exit
		}
		
		if (cs.size() == 0) {
			//Nothing recorded yet, whatever
			//plugin.getLogger().info("ERROR: Death YML section not found");
		}
		
		
		//Loop on each player
		for (String temp : cs) {
			UUID uuid = UUID.fromString(temp);
			String tempLoc = playersData.getString(temp);
			String[] parts = tempLoc.split("\\|");
			if (parts.length < 4)
				continue;
			
			Location loc = new Location(
				plugin.getServer().getWorld(parts[0]),
				Double.parseDouble(parts[1]),
				Double.parseDouble(parts[2]),
				Double.parseDouble(parts[3])
			);
			locations.put(uuid, loc);
		}
	}
	
	
	//Update the list to the file
	public void saveTutorial() {
		File statsFile = new File(this.filePath);
		FileConfiguration statsAccess = YamlConfiguration.loadConfiguration(statsFile);
		
		statsAccess.set("players", "");
		
		for (Map.Entry< UUID , Location > entry : this.locations.entrySet()) {
			UUID uuid = entry.getKey();
			Location loc = entry.getValue();
			
			statsAccess.set("players." + uuid, loc.getWorld().getName() + "|" + loc.getX() + "|" + loc.getY() + "|" + loc.getZ());
		}
		
		try {
			statsAccess.save(statsFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
