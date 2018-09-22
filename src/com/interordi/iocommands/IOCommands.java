package com.interordi.iocommands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.interordi.iocommands.modules.Warp;
import com.interordi.iocommands.modules.Warps;
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
		
		//TODO: Command preprocessor, parse selectors, run multiple command instances if needed
		/*
		 * if(sender instanceof BlockCommandSender){
		 * Block block = (Block)sender;
		 * block.getBlock().getLocation()
		 */
		System.out.print("Command: "); 
		System.out.println(cmd);
		System.out.println(String.join(", ", args));
		
		if (cmd.getName().equalsIgnoreCase("warp")) {
			
			//Fancy display to players, basic for others like the console
			if (!(sender instanceof Player))
				return false;
			
			Player player = (Player)sender;
			if (!player.hasPermission("iocommands.warp")) {
				player.sendMessage("�cYou are not allowed to use this command.");
				return true;
			}
			
			Player target = player;
			String name = "";
			if (args.length > 1) {
				target = Bukkit.getServer().getPlayer(args[0]);
				if (target == null) {
					player.sendMessage("�cTarget player not found!");
					return true;
				}
				name = args[1];
			} else if (args.length == 1) {
				name = args[0];
			} else {
				player.sendMessage("�cMissing parameter: destination");
				return true;
			}
			
			Warp warp = warps.getWarp(name);
			if (warp == null) {
				player.sendMessage("�cDestination not found!");
				return true;
			}
			
			target.teleport(warp.location);
			return true;

		} else if (cmd.getName().equalsIgnoreCase("setwarp")) {
			
			//Fancy display to players, basic for others like the console
			if (!(sender instanceof Player))
				return false;
			
			Player player = (Player)sender;
			if (!player.hasPermission("iocommands.setwarp")) {
				player.sendMessage("�cYou are not allowed to use this command.");
				return true;
			}
			
			String name = "";
			if (args.length > 0) {
				name = args[0];
			} else {
				player.sendMessage("�cMissing parameter: name");
				return true;
			}
			
			warps.setWarp(player, name, player.getLocation());
			player.sendMessage("�aWarp added!");
			return true;
		
		} else if (cmd.getName().equalsIgnoreCase("home")) {
			
			//Fancy display to players, basic for others like the console
			if (!(sender instanceof Player))
				return false;
			
			Player player = (Player)sender;
			if (!player.hasPermission("iocommands.home")) {
				player.sendMessage("�cYou are not allowed to use this command.");
				return true;
			}
			
			Location home = homes.getHome(player);
			if (home == null) {
				player.sendMessage("�cHome not set!");
				return true;
			}
			
			player.teleport(home);
			return true;

		} else if (cmd.getName().equalsIgnoreCase("sethome")) {
			
			//Fancy display to players, basic for others like the console
			if (!(sender instanceof Player))
				return false;
			
			Player player = (Player)sender;
			if (!player.hasPermission("iocommands.home")) {
				player.sendMessage("�cYou are not allowed to use this command.");
				return true;
			}
			
			homes.setHome(player, player.getLocation());
			player.sendMessage("�aHome set!");
			return true;
		
		} else if (cmd.getName().equalsIgnoreCase("flight")) {
			
			//Only players can run this command
			boolean isConsole = !(sender instanceof Player);

			Player user = null;
			if (!isConsole)
				user = (Player)sender;
			
			//Select the target of the command
			Player target = null;
			if (args.length >= 2) {
				target = Bukkit.getServer().getPlayer(args[1]);
				if (target == null) {
					if (user != null)
						user.sendMessage("�cTarget not found!");
					return true;
				}
			} else {
				try {
					target = (Player)sender;
				} catch (ClassCastException e) {
					System.out.println("Failed to cast sender to Player");
					return false;
				}
			}
			
			//Check if the user has permission to use this command
			if (!isConsole && !user.hasPermission("iocommands.flight")) {
				user.sendMessage("�cYou are not allowed to use this command!");
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
			
			thisFlightManager.setFlightStatus(user, target, status);
			
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
