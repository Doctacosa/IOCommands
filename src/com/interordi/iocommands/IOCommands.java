package com.interordi.iocommands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.interordi.iocommands.modules.Warp;
import com.interordi.iocommands.modules.Warps;
import com.interordi.utilities.Commands;

import javafx.util.Pair;

import com.interordi.iocommands.modules.FlightManager;
import com.interordi.iocommands.modules.Homes;


public class IOCommands extends JavaPlugin {

	public static IOCommands instance;

	public Warps warps;
	public Homes homes;
	public FlightManager thisFlightManager;
	public PlayerListener thisPlayerListener;

	
	public void onEnable() {
		instance = this;
		
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		
		thisPlayerListener = new PlayerListener(this);
		this.warps = new Warps(this);
		this.homes = new Homes(this);
		thisFlightManager = new FlightManager(this);
		
		getLogger().info("IOCommands enabled");
	}
	
	
	public void onDisable() {
		getLogger().info("IOCommands disabled");
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		System.out.print("Command: "); 
		System.out.println(cmd.getName());

		//Get the list of potential targets if a selector was used
		Pair< Integer, List< String > > results = Commands.findTargets(Bukkit.getServer(), sender, cmd, label, args);
		
		int position = results.getKey();
		boolean result = false;
		if (position != -1) {
			//Run the command for each target identified by the selector
			System.out.println("Targets:");
			for (String target : results.getValue()) {
				args[position] = target;
				System.out.println(String.join(", ", args));
				
				result = runCommand(sender, cmd, label, args);
			}
		} else {
			System.out.println("Running as-is");
			//Run the command as-is
			result = runCommand(sender, cmd, label, args);
		}
		
		return result;
	}
	
	
	//Actually run the entered command
	public boolean runCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (cmd.getName().equalsIgnoreCase("warp")) {

			Player target = null;
			
			if (sender instanceof Player) {
				Player player = (Player)sender;
				target = player;
				if (!sender.hasPermission("iocommands.warp")) {
					player.sendMessage("§cYou are not allowed to use this command.");
					return true;
				}
			}
			
			String destination = "";
			if (args.length > 1) {
				target = Bukkit.getServer().getPlayer(args[0]);
				destination = args[1];
			} else if (args.length == 1) {
				destination = args[0];
			} else {
				sender.sendMessage("§cMissing parameter: destination");
				return true;
			}
			
			if (target == null) {
				sender.sendMessage("§cTarget player not found!");
				return true;
			}
			
			Warp warp = warps.getWarp(destination);
			if (warp == null) {
				sender.sendMessage("§cDestination not found!");
				return true;
			}
			
			target.teleport(warp.location);
			return true;

		} else if (cmd.getName().equalsIgnoreCase("setwarp")) {
			
			if (!(sender instanceof Player))
				return false;
			
			Player player = (Player)sender;
			if (!player.hasPermission("iocommands.setwarp")) {
				player.sendMessage("§cYou are not allowed to use this command.");
				return true;
			}
			
			String name = "";
			if (args.length > 0) {
				name = args[0];
			} else {
				player.sendMessage("§cMissing parameter: name");
				return true;
			}
			
			warps.setWarp(player, name, player.getLocation());
			player.sendMessage("§aWarp added!");
			return true;
		
		} else if (cmd.getName().equalsIgnoreCase("home")) {
			
			if (!(sender instanceof Player))
				return false;
			
			Player player = (Player)sender;
			if (!player.hasPermission("iocommands.home")) {
				player.sendMessage("§cYou are not allowed to use this command.");
				return true;
			}
			
			Location home = homes.getHome(player);
			if (home == null) {
				player.sendMessage("§cHome not set!");
				return true;
			}
			
			player.teleport(home);
			return true;

		} else if (cmd.getName().equalsIgnoreCase("sethome")) {
			
			if (!(sender instanceof Player))
				return false;
			
			Player player = (Player)sender;
			if (!player.hasPermission("iocommands.home")) {
				player.sendMessage("§cYou are not allowed to use this command.");
				return true;
			}
			
			homes.setHome(player, player.getLocation());
			player.sendMessage("§aHome set!");
			return true;
		
		} else if (cmd.getName().equalsIgnoreCase("flight")) {
			
			Player target = null;
			
			if (sender instanceof Player) {
				Player user = (Player)sender;
				target = user;
				//Check if the user has permission to use this command
				if (!user.hasPermission("iocommands.flight")) {
					user.sendMessage("§cYou are not allowed to use this command!");
					return true;
				}
			}
			
			//Select the target of the command
			if (args.length >= 2) {
				target = Bukkit.getServer().getPlayer(args[1]);
			}
			
			if (target == null) {
				sender.sendMessage("§cTarget not found!");
				return true;
			}
			
			int status = -1;
			
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("on")) {
					status = 1;
				} else if (args[0].equalsIgnoreCase("off")) {
					status = 0;
				}
			}
			
			Player source = null;
			if (sender instanceof Player)
				source = (Player)sender;
			thisFlightManager.setFlightStatus(source, target, status);
			
			return true;
		}
		
		return false;
	}


	//Set the flight status of the given player
	public void setFlightStatus(Player source, Player target, int status) {
		thisFlightManager.setFlightStatus(source, target, status);
	}


	//Reset our flag on a player's flight status
	public void resetPlayerFlight(Player player) {
		thisFlightManager.resetFlightStatus(player);
	}
	
	
}
