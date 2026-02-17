package com.interordi.iocommands.utilities;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
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

public class Commands {
	
	//Given a command, determine the potential targets by analyzing selectors
	public static CommandTargets findTargets(Server server, CommandSender sender, Command cmd, String label, String[] args) {
		
		//TODO: Clean fail on parse error
		
		CommandTargets cmdTargets = new CommandTargets();

		boolean sort = false;
		boolean random = false;
		
		int i = 0;
		for (String arg : args) {
			
			int limit = 1;
			
			if (arg.startsWith("@p")) {
				cmdTargets.position = i;
				limit = 1;
				sort = true;
			} else if (arg.startsWith("@r")) {
				cmdTargets.position = i;
				limit = 1;
				random = true;
			} else if (arg.startsWith("@a")) {
				cmdTargets.position = i;
				limit = 0;
			}
			
			if (cmdTargets.position != -1) {
				//Selector detected, process it
				
				Location location = null;
				if (sender instanceof BlockCommandSender) {
					BlockCommandSender source = (BlockCommandSender)sender;
					location = source.getBlock().getLocation();
					
				} else if (sender instanceof Player) {
					Player source = (Player)sender;
					location = source.getLocation();
					
				} else if (sender instanceof ConsoleCommandSender) {
					@SuppressWarnings("unused")
					ConsoleCommandSender source = (ConsoleCommandSender)sender;
					
				} else if (sender instanceof RemoteConsoleCommandSender) {
					@SuppressWarnings("unused")
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
					//IOCommands.instance.getLogger().info("arg: " + arg);
					String params = arg.substring(3, arg.length()-1);
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
								String[] parts = value.split("\\.\\.");
								if (parts.length > 0 && parts[0].length() > 0)
									minDistance = Integer.parseInt(parts[0]);
								if (parts.length > 1 && parts[1].length() > 0)
									maxDistance = Integer.parseInt(parts[1]);
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
						case "level":
						case "gamemode":
						case "name":
						case "x_rotation":
						case "y_rotation":
						case "type":
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

				//Add missing values to complete a set
				if (dx != Integer.MAX_VALUE) {
					if (dy == Integer.MAX_VALUE)	dy = 0;
					if (dz == Integer.MAX_VALUE)	dz = 0;
				}
				if (dy != Integer.MAX_VALUE) {
					if (dx == Integer.MAX_VALUE)	dx = 0;
					if (dz == Integer.MAX_VALUE)	dz = 0;
				}
				if (dz != Integer.MAX_VALUE) {
					if (dx == Integer.MAX_VALUE)	dx = 0;
					if (dy == Integer.MAX_VALUE)	dy = 0;
				}
				
				//Check if we got all the parameters required
				if (x != Integer.MAX_VALUE && y != Integer.MAX_VALUE && z != Integer.MAX_VALUE) {
					location = new Location(location.getWorld(), x, y, z);
				}
				
				
				//Define the sorting order of players
				Set< PlayerSort > players = null;
				
				if (sort) {
					//Sort by distance
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
				} else {
					//Sort in no special order
					players = new HashSet< PlayerSort >();
				}

				//Build a list of players for checks
				Collection<? extends Player> playersRaw = server.getOnlinePlayers();
				for (Player p : playersRaw) {
					if (location.getWorld() != p.getWorld())
						continue;

					double distance = location.distance(p.getLocation());
					players.add(new PlayerSort(p, distance));
				}

				//Check by bounding box
				if (dx != Integer.MAX_VALUE) {
					playersRaw = server.getOnlinePlayers();
					for (PlayerSort ps : players) {
						Player player = ps.player;
						if (location.getWorld() != player.getWorld()) {
							continue;
						}
						
						if (player.getLocation().getBlockX() >= x && player.getLocation().getBlockX() <= x + dx &&
							player.getLocation().getBlockY() >= y && player.getLocation().getBlockY() <= y + dy &&
							player.getLocation().getBlockZ() >= z && player.getLocation().getBlockZ() <= z + dz) {
							cmdTargets.targets.add(player.getDisplayName());
						}
					}
				//} else if (maxDistance != Integer.MAX_VALUE) {
				} else {
					//Not restricting per distance; catch all we're interested in
					//Necessary sorting and limits are already defined
					for (PlayerSort ps : players) {
						double distance = ps.distance;
						Player player = ps.player;

						//Include players that match all conditions for distance
						if (distance >= minDistance &&
							distance <= maxDistance) {
							cmdTargets.targets.add(player.getDisplayName());
							
							//Stop as soon as we reach the number of wanted targets
							if (limit > 0 && cmdTargets.targets.size() >= limit && !random)
								break;
						}
					}
				}
				
				break;
			}

			if (random) {
				Collections.shuffle(cmdTargets.targets);
				cmdTargets.targets = cmdTargets.targets.subList(0, limit);
			}
			
			i++;
		}

		return cmdTargets;
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
