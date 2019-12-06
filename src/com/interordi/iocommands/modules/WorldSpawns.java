package com.interordi.iocommands.modules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.interordi.iocommands.IOCommands;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class WorldSpawns {
	
	private String file;
	private Map< String, Location > spawns;
	IOCommands plugin;
	
	
	public WorldSpawns(IOCommands plugin) {
		this.plugin = plugin;
		this.file = plugin.getDataFolder().toString() + File.separatorChar + "world_spawns.csv";
		this.spawns = new HashMap< String, Location>();
		
		load();
	}
	
	
	//Load the list of spawns from the file
	public void load() {
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(this.file));
		} catch (FileNotFoundException e) {
			System.err.println("Failed to load the spawns file");
			e.printStackTrace();
			return;
		}
		
		try {
			String[] line;
			while ((line = reader.readNext()) != null) {
				Location spawn = new Location(
					Bukkit.getServer().getWorld(line[0]),
					Double.parseDouble(line[1]), Double.parseDouble(line[2]), Double.parseDouble(line[3]),
					Float.parseFloat(line[5]), Float.parseFloat(line[4])
				);
				spawns.put(line[0], spawn);
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("Failed to read from the spawns file");
			e.printStackTrace();
			return;
		}
	}
	
	
	//Save the list of spawns to the file
	@SuppressWarnings("deprecation")
	public void save() {
		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new FileWriter(this.file), ',');
		} catch (IOException e) {
			System.err.println("Failed to load the spawns file");
			e.printStackTrace();
			return;
		}
		
		for(Map.Entry< String, Location > entry: spawns.entrySet()) {
			Location pos = entry.getValue();
			
			//Skip unknown worlds
			if (pos.getWorld() == null)
				continue;

			String[] line = {
					pos.getWorld().getName(),
					String.valueOf(pos.getX()), String.valueOf(pos.getY()), String.valueOf(pos.getZ()),
					String.valueOf(pos.getPitch()), String.valueOf(pos.getYaw())
			};
			writer.writeNext(line);
		}
		
		try {
			writer.close();
		} catch (IOException e) {
			System.err.println("Failed to write to the spawns file");
			e.printStackTrace();
			return;
		}
	}
	
	
	//Get a wrap based on its name
	public Location getSpawn(String world) {
		return spawns.get(world);
	}
	
	
	//Add a spawn or update an existing one
	public void setSpawn(Location pos) {
		spawns.put(pos.getWorld().getName(), pos);
		save();
	}

}
