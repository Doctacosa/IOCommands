package com.interordi.iocommands.modules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.interordi.iocommands.IOCommands;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class Homes {
	
	private String file;
	private Map< UUID, Location > homes;
	IOCommands plugin;
	
	
	public Homes(IOCommands plugin) {
		this.plugin = plugin;
		this.file = plugin.getDataFolder().toString() + File.separatorChar + "homes.csv";
		this.homes = new HashMap< UUID, Location>();
		
		load();
	}
	
	
	//Load the list of homes from the file
	public void load() {
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(this.file));
		} catch (FileNotFoundException e) {
			System.err.println("Failed to load the homes file");
			e.printStackTrace();
			return;
		}
		
		try {
			String[] line;
			while ((line = reader.readNext()) != null) {
				Location home = new Location(
					Bukkit.getServer().getWorld(line[1]),
					Double.parseDouble(line[3]), Double.parseDouble(line[4]), Double.parseDouble(line[5]),
					Float.parseFloat(line[6]), Float.parseFloat(line[7])
				);
				homes.put(UUID.fromString(line[2]), home);
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("Failed to read from the homes file");
			e.printStackTrace();
			return;
		}
	}
	
	
	//Save the list of homes to the file
	@SuppressWarnings("deprecation")
	public void save() {
		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new FileWriter(this.file), ',');
		} catch (IOException e) {
			System.err.println("Failed to load the homes file");
			e.printStackTrace();
			return;
		}
		
		for(Map.Entry< UUID, Location > entry: homes.entrySet()) {
			Location pos = entry.getValue();
			
			//Skip unknown worlds
			if (pos.getWorld() == null)
				continue;

			String[] line = {
					"UUID",
					pos.getWorld().getName(),
					entry.getKey().toString(),
					String.valueOf(pos.getX()), String.valueOf(pos.getY()), String.valueOf(pos.getZ()),
					String.valueOf(pos.getPitch()), String.valueOf(pos.getYaw())
			};
			writer.writeNext(line);
		}
		
		try {
			writer.close();
		} catch (IOException e) {
			System.err.println("Failed to write to the homes file");
			e.printStackTrace();
			return;
		}
	}
	
	
	//Get a wrap based on its name
	public Location getHome(Player player) {
		return homes.get(player.getUniqueId());
	}
	
	
	//Add a home or update an existing one
	public void setHome(Player creator, Location pos) {
		homes.put(creator.getUniqueId(), pos);
		save();
	}

}
