package com.interordi.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

import javafx.util.Pair;

public class Commands {
	
	//Given a command, determine the potential targets by analyzing selectors
	public static Pair< Integer, List< String > > findTargets(Server server, CommandSender sender, Command cmd, String label, String[] args) {
		
		//TODO: Command preprocessor, parse selectors, run multiple command instances if needed
		/*
		 * if(sender instanceof BlockCommandSender){
		 * Block block = (Block)sender;
		 * block.getBlock().getLocation()
		 */
		//TODO: Fail on parse error
		
		List< String > targets = new ArrayList< String >();
		
		int position = -1;
		boolean sort = false;
		
		int i = 0;
		for (String arg : args) {
			
			int limit = 1;
			
			if (arg.startsWith("@p")) {
				position = i;
				limit = 1;
				sort = true;
				//TODO: Stuff
			} else if (arg.startsWith("@r")) {
				position = i;
				limit = 1;
				//TODO: Stuff
			} else if (arg.startsWith("@a")) {
				position = i;
				limit = 0;
				//TODO: Stuff
			}
			
			if (position != -1) {
				//Selector detected, process it
				
				Location location = null;
				if (sender instanceof BlockCommandSender) {
					BlockCommandSender source = (BlockCommandSender)sender;
					location = source.getBlock().getLocation();
					
				} else if (sender instanceof Player) {
					Player source = (Player)sender;
					location = source.getLocation();
					
				} else if (sender instanceof ConsoleCommandSender) {
					ConsoleCommandSender source = (ConsoleCommandSender)sender;
					
				} else if (sender instanceof RemoteConsoleCommandSender) {
					RemoteConsoleCommandSender source = (RemoteConsoleCommandSender)sender;
				
				} else {
					System.out.println("Source not found!!");
					return null;
				}
				
				int minDistance = 0;
				int maxDistance = Integer.MAX_VALUE;
				int x = Integer.MAX_VALUE;
				int y = Integer.MAX_VALUE;
				int z = Integer.MAX_VALUE;
				int dx = Integer.MAX_VALUE;
				int dy = Integer.MAX_VALUE;
				int dz = Integer.MAX_VALUE;
				
				if (arg.indexOf("[") == 2) {
					//Parameters found, parse them
					String params = arg.substring(2, -1);
					String[] paramsSplit = params.split(",");
					for (String param : paramsSplit) {
						String key = param.split("=")[0];
						String value = param.split("=")[1];
						
						switch (key) {
						case "x":
							x = Integer.parseInt(value);
							break;
						case "y":
							y = Integer.parseInt(value);
							break;
						case "z":
							z = Integer.parseInt(value);
							break;
						case "distance":
							if (value.contains("..")) {
								String min = value.split("..")[0];
								if (min.length() > 0)
									minDistance = Integer.parseInt(min);
								String max = value.split("..")[1];
								if (max.length() > 0)
									maxDistance = Integer.parseInt(max);
							} else
								minDistance = maxDistance = Integer.parseInt(value);
							break;
						case "dx":
							dx = Integer.parseInt(value);
							break;
						case "dy":
							dy = Integer.parseInt(value);
							break;
						case "dz":
							dz = Integer.parseInt(value);
							break;
						case "limit":
							break;
						case "level":
							break;
						case "gamemode":
							break;
						case "name":
							break;
						case "x_rotation":
							break;
						case "y_rotation":
							break;
						case "type":
							break;
						case "scores":
						case "tag":
						case "team":
						case "sort":
							sender.sendMessage("Not supported: " + key);
							break;
						default:
							sender.sendMessage("Unknown parameter: " + key);
							return null;
						}
						
					}
					
				}
				
				//Check if we got all the parameters required
				if (x != Integer.MAX_VALUE && y != Integer.MAX_VALUE && z != Integer.MAX_VALUE) {
					location = new Location(location.getWorld(), x, y, z);
				}
				
				
				Set< PlayerSort > players = null;
				
				if (sort) {
					//Get the distance of each player and sort them
					players = new TreeSet< PlayerSort >(new Comparator< PlayerSort >() {
						@Override
						public int compare(PlayerSort o1, PlayerSort o2) {
							if (o1.distance < o2.distance)
								return -1;
							else if (o1.distance > o2.distance)
								return 1;
							return 0;
						}
					});
					
					Collection<? extends Player> playersRaw = server.getOnlinePlayers();
					for (Player p : playersRaw) {
						double distance = location.distance(p.getLocation());
						players.add(new PlayerSort(p, distance));
					}
					
				} else {
					//Basic list with no distance
					players = new HashSet< PlayerSort >();
					Collection<? extends Player> playersRaw = server.getOnlinePlayers();
					for (Player p : playersRaw) {
						players.add(new PlayerSort(p, 0));
					}
				}
				
				
				for (PlayerSort ps : players) {
					double distance = ps.distance;
					Player player = ps.player;
					
					//Include players that match all conditions
					if (distance >= minDistance &&
						distance <= maxDistance) {
						targets.add(player.getDisplayName());
						
						//Stop as soon as we reach the number of wanted targets
						if (targets.size() >= limit)
							break;
					}
				}
				
				break;
			}

			i++;
		}
		
		return new Pair< Integer, List< String > >(position, targets);
	}

}



class PlayerSort {
	public PlayerSort(Player p, double i) {
		player = p;
		distance = i;
	}
	
	public Player player;
	public double distance;
}
