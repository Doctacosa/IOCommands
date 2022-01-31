package com.interordi.iocommands;

import java.util.ArrayList;
//import java.io.ByteArrayOutputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
//import java.util.List;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.interordi.iocommands.modules.Warp;
import com.interordi.iocommands.modules.Warps;
import com.interordi.iocommands.modules.FlightManager;
import com.interordi.iocommands.modules.FunCommands;
import com.interordi.iocommands.modules.Homes;
import com.interordi.iocommands.modules.WorldSpawns;
import com.interordi.iocommands.modules.Tutorial;
import com.interordi.iocommands.modules.Minecarts;
import com.interordi.iocommands.modules.Restart;
import com.interordi.iocommands.utilities.CommandTargets;
import com.interordi.iocommands.utilities.Commands;

public class IOCommands extends JavaPlugin {

	public static IOCommands instance;

	public Warps warps;
	public Homes homes;
	public WorldSpawns worldSpawns;
	public Tutorial tutorial;
	public FlightManager thisFlightManager;
	public Restart restart;
	public PlayerListener thisPlayerListener;

	private Map< String, List< String > > loginMessages;
	
	//private boolean bungeeInit = false;

	
	public void onEnable() {
		instance = this;
		
		//Always ensure we've got a copy of the config in place (does not overwrite existing)
		this.saveDefaultConfig();

		loginMessages = new HashMap< String, List< String > >();

		if (this.getConfig().getConfigurationSection("message-on-login") != null) {
			Map< String, Object> loginPermissions = this.getConfig().getConfigurationSection("message-on-login").getValues(false);
			if (loginPermissions != null && !loginPermissions.isEmpty()) {
				loginPermissions.forEach((key, y) -> {
					//TODO: Find the proper way to use the object "y" directly
					List< String > messages = new ArrayList< String >();
					String permission = this.getConfig().getString("message-on-login." + key + ".permission", "");
					for (String message : this.getConfig().getStringList("message-on-login." + key + ".messages")) {
						message = message.replace("&", "§");
						messages.add(message);
					}
					if (!permission.isEmpty() && !messages.isEmpty())
						loginMessages.put(permission, messages);
				});
			}
		}

		
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		
		thisPlayerListener = new PlayerListener(this);
		this.warps = new Warps(this);
		this.homes = new Homes(this);
		this.worldSpawns = new WorldSpawns(this);
		this.tutorial = new Tutorial(this);
		thisFlightManager = new FlightManager(this);
		this.restart = new Restart();
		Minecarts.init();
		
		getLogger().info("IOCommands enabled");
	}
	
	
	public void onDisable() {
		getLogger().info("IOCommands disabled");
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		//Get the list of potential targets if a selector was used
		CommandTargets results = Commands.findTargets(Bukkit.getServer(), sender, cmd, label, args);
		
		boolean result = false;
		if (results.position != -1) {
			//Run the command for each target identified by the selector
			for (String target : results.targets) {
				args[results.position] = target;
				
				result = runCommand(sender, cmd, label, args);
			}
		} else {
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
					explainNoTeleports(player);
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
				sender.sendMessage(ChatColor.RED + "Missing parameter: destination");
				return true;
			}
			
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Target player not found!");
				return true;
			}
			
			Warp warp = warps.getWarp(destination);
			if (warp == null) {
				sender.sendMessage(ChatColor.RED + "Destination not found!");
				return true;
			}
			
			try {
				target.teleport(warp.location);
			} catch (java.lang.IllegalArgumentException e) {
				sender.sendMessage(ChatColor.RED + "The destination is currently not available.");
			}
			return true;

		} else if (cmd.getName().equalsIgnoreCase("setwarp")) {
			
			if (!(sender instanceof Player))
				return false;
			
			Player player = (Player)sender;
			if (!player.hasPermission("iocommands.setwarp")) {
				explainNoTeleports(player);
				return true;
			}
			
			String name = "";
			if (args.length > 0) {
				name = args[0];
			} else {
				player.sendMessage(ChatColor.RED + "Missing parameter: name");
				return true;
			}
			
			warps.setWarp(player, name, player.getLocation());
			player.sendMessage(ChatColor.GREEN + "Warp added!");
			return true;
		
		} else if (cmd.getName().equalsIgnoreCase("home")) {
			
			if (!(sender instanceof Player))
				return false;
			
			Player player = (Player)sender;
			if (!player.hasPermission("iocommands.home")) {
				explainNoTeleports(player);
				return true;
			}
			
			Location home = homes.getHome(player);
			if (home == null) {
				player.sendMessage(ChatColor.RED + "Home not set!");
				return true;
			}
			
			try {
				player.teleport(home);
			} catch (java.lang.IllegalArgumentException e) {
				player.sendMessage(ChatColor.RED + "The home world is not available!");
				return true;
			}
			return true;

		} else if (cmd.getName().equalsIgnoreCase("sethome")) {
			
			if (!(sender instanceof Player))
				return false;
			
			Player player = (Player)sender;
			if (!player.hasPermission("iocommands.home")) {
				explainNoTeleports(player);
				return true;
			}
			
			homes.setHome(player, player.getLocation());
			player.sendMessage(ChatColor.GREEN + "Home set!");
			return true;
		
		} else if (cmd.getName().equalsIgnoreCase("tpa")) {

			if (sender instanceof Player) {
				Player user = (Player)sender;
				explainNoTeleports(user);
			}
			return true;

		} else if (cmd.getName().equalsIgnoreCase("flight")) {
			
			Player target = null;
			
			if (sender instanceof Player) {
				Player user = (Player)sender;
				target = user;
				//Check if the user has permission to use this command
				if (!user.hasPermission("iocommands.flight")) {
					user.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
					return true;
				}
			}
			
			//Select the target of the command
			if (args.length >= 2) {
				target = Bukkit.getServer().getPlayer(args[1]);
			}
			
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Target not found!");
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
			
		} else if (cmd.getName().equalsIgnoreCase("keepinv")) {
			
			World target = null;
			boolean action = false;
			
			if (sender instanceof Player) {
				Player user = (Player)sender;
				//Check if the user has permission to use this command
				if (!user.hasPermission("iocommands.keepinv")) {
					user.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
					return true;
				}
				target = user.getWorld();
			}
			
			//Select the target of the command
			if (args.length == 1) {
				if (target == null) {
					sender.sendMessage(ChatColor.RED + "A world must be specified!");
					return true;
				}
				action = args[0].equals("true");
			} else if (args.length >= 2) {
				target = Bukkit.getServer().getWorld(args[0]);
				action = args[1].equals("true");
			} else {
				sender.sendMessage(ChatColor.RED + "A world must be specified!");
				return true;
			}
			
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "World not found!");
				return true;
			}
			
			target.setGameRule(GameRule.KEEP_INVENTORY, action);
			sender.sendMessage(ChatColor.GREEN + "The keepInventory rule for " + ChatColor.WHITE + target.getName() + ChatColor.GREEN + " is now " + ChatColor.WHITE + action);
			
			return true;
		
		} else if (cmd.getName().equalsIgnoreCase("time")) {
			
			World target = null;
			boolean set = false;
			long time = 0;
			
			if (sender instanceof Player) {
				Player user = (Player)sender;
				//Check if the user has permission to use this command
				if (!user.hasPermission("iocommands.time")) {
					user.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
					return true;
				}
				target = user.getWorld();
			}
			
			//Select the target of the command
			if (args.length == 0) {
				set = false;
				
			} else if (args.length == 1) {
				try {
					time = extractTime(args, 0);
					set = true;
				} catch (NumberFormatException e) {
					target = Bukkit.getServer().getWorld(args[0]);
					set = false;
				}
				
			} else if (args.length == 2) {
				if (!sender.hasPermission("iocommands.time.set")) {
					sender.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
					return true;
				}
				
				if (!args[0].equals("set")) {
					target = Bukkit.getServer().getWorld(args[0]); 
				}
				
				try {
					time = extractTime(args, 1);
					set = true;
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "Invalid time value: " + args[1]);
					return true;
				}
				
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid command!");
				return true;
			}
			
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "World not found!");
				return true;
			}
			
			//Set or get the time, as requested
			if (set) {
				target.setTime(time);
			}
			
			long currentTime = target.getTime();
			double hours = Math.floor(currentTime / 1000) + 6;
			hours = hours % 24;
			double minutes = Math.floor((currentTime - Math.floor(currentTime / 1000) * 1000) / 1000 * 60);

			if (set) {
				sender.sendMessage(ChatColor.YELLOW + "Time for world " + target.getName() + " set to: " + String.format("%02.0f", hours) + ":" + String.format("%02.0f", minutes));
			} else {
				sender.sendMessage(ChatColor.YELLOW + "Time: " + String.format("%02.0f", hours) + ":" + String.format("%02.0f", minutes));
			}
			
			return true;
		
		} else if (cmd.getName().equalsIgnoreCase("timeplus")) {
			
			//Check if the user has permission to use this command
			if (!sender.hasPermission("iocommands.time.set")) {
				sender.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
				return true;
			}

			World target = null;
			long time = 0;
			
			if (sender instanceof Player) {
				Player user = (Player)sender;
				target = user.getWorld();
			}
			
			//Select the target of the command
			if (args.length <= 1) {
				sender.sendMessage(ChatColor.RED + "Missing parameter!");
				return true;
				
			}
			
			target = Bukkit.getServer().getWorld(args[0]);
			
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "World not found!");
				return true;
			}
			
			String command = args[1];
				
			if (command.equalsIgnoreCase("lodge")) {
				time = target.getTime();
				
				if (time < 6000 || time > 23000) {
					time = (12 - 6) * 1000;
					target.setTime(time);
				}
				
			} else if (command.equalsIgnoreCase("talk")) {
				long currentTime = target.getTime();
				double hours = Math.floor(currentTime / 1000) + 6;
				hours = hours % 24;
				double minutes = Math.floor((currentTime - Math.floor(currentTime / 1000) * 1000) / 1000 * 60);

				getServer().broadcastMessage("Current time: " + String.format("%02.0f", hours) + ":" + String.format("%02.0f", minutes));
				
			} else {
				sender.sendMessage(ChatColor.RED + "Invalid command!");
				return true;
			}
			
			return true;
		
		} else if (cmd.getName().equalsIgnoreCase("spawn")) {
			
			Player target = null;
			
			if (sender instanceof Player) {
				Player user = (Player)sender;
				target = user;
				//Check if the user has permission to use this command
				if (!user.hasPermission("iocommands.warp")) {
					user.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
					return true;
				}
			}
			
			//Select the target of the command
			if (args.length >= 1) {
				target = Bukkit.getServer().getPlayer(args[0]);
			}
			
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Target not found!");
				return true;
			}
			
			World w = target.getWorld();
			Location spawn = worldSpawns.getSpawn(w.getName());
			if (spawn != null)
				target.teleport(spawn);
			else
				target.teleport(w.getSpawnLocation());
			
			return true;
			
		} else if (cmd.getName().equalsIgnoreCase("setworldspawn")) {
			
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command must be run in-game!");
				return true;
			}
			
			Player user = (Player)sender;
			//Check if the user has permission to use this command
			if (!user.hasPermission("iocommands.setworldspawn")) {
				user.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
				return true;
			}
			
			World w = user.getWorld();
			w.setSpawnLocation(user.getLocation());
			worldSpawns.setSpawn(user.getLocation());
			user.sendMessage(ChatColor.GREEN + "World spawn set!");
			
			return true;
			
		} else if (cmd.getName().equalsIgnoreCase("kill")) {
			
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Only players can run this command!");
				return true;
			}
			
			Player user = (Player)sender;
			user.setHealth(0);
			return true;

		} else if (cmd.getName().equalsIgnoreCase("shock") ||
				   cmd.getName().equalsIgnoreCase("rocket") ||
				   cmd.getName().equalsIgnoreCase("slap") ||
				   cmd.getName().equalsIgnoreCase("bslap")) {
			
			String command = cmd.getName().toLowerCase();
			Player target = null;
			boolean fromPlayer = (sender instanceof Player || sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender);
			int amount = 1;
			int delay = 20;
			
			if (sender instanceof Player) {
				Player user = (Player)sender;
				target = user;
				//Check if the user has permission to use this command
				if (!user.hasPermission("iocommands." + command)) {
					user.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
					return true;
				}
			}
			
			//Select the target of the command
			if (args.length >= 1) {
				try {
					amount = Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {
					target = Bukkit.getServer().getPlayer(args[0]);
				}
			}
			
			if (args.length >= 2) {
				try {
					amount = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {}
			}

			if (args.length >= 3) {
				try {
					delay = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {}
			}

			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Target not found!");
				return true;
			}
			
			if (amount <= 0 || amount > 100)
				amount = 1;
			
			if (command.equals("shock"))
				FunCommands.shock(target, amount, delay, fromPlayer);
			else if (command.equals("rocket"))
				FunCommands.rocket(target, amount, delay, fromPlayer);
			else if (command.equals("slap"))
				FunCommands.slap(target, amount, delay, fromPlayer, 1.0f);
			else if (command.equals("bslap"))
				FunCommands.slap(target, amount, delay, fromPlayer, 4.0f);
			
			return true;

		} else if (cmd.getName().equalsIgnoreCase("setvelocity")) {
			
			Player target = null;
			float x = 0.0f;
			float y = 0.0f;
			float z = 0.0f;
			float delay = 0.0f;
			
			//Check if the user has permission to use this command
			if (!sender.hasPermission("iocommands.setvelocity")) {
				sender.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
				return true;
			}
			
			//Check and set the parameters
			if (args.length < 5) {
				sender.sendMessage(ChatColor.RED + "Missing parameters.");
				return true;
			}

			target = Bukkit.getServer().getPlayer(args[0]);

			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Target not found!");
				return true;
			}
			
			try {
				x = Float.parseFloat(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Invalid parameter: " + args[1]);
				return true;
			}
			
			try {
				y = Float.parseFloat(args[2]);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Invalid parameter: " + args[2]);
				return true;
			}
			
			try {
				z = Float.parseFloat(args[3]);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Invalid parameter: " + args[3]);
				return true;
			}
			
			try {
				delay = Float.parseFloat(args[4]);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "Invalid parameter: " + args[4]);
				return true;
			}
			
			FunCommands.setVelocity(target, x, y, z, delay);
			
			return true;

		} else if (cmd.getName().equalsIgnoreCase("pos")) {

			Player target = null;
			
			if (sender instanceof Player) {
				Player user = (Player)sender;
				target = user;
				//Check if the user has permission to use this command
				if (!user.hasPermission("iocommands.pos")) {
					user.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
					return true;
				}
			}
			
			//Select the target of the command
			if (args.length >= 1 && args[0].length() > 3) {
				if (!sender.hasPermission("iocommands.pos.other")) {
					sender.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
					return true;
				}

				target = Bukkit.getServer().getPlayer(args[0]);
			}
			
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Target not found!");
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
			
			thisPlayerListener.setPositionStatus(target, status);
			
			return true;

		} else if (cmd.getName().equalsIgnoreCase("whois")) {
			
			if (!(sender instanceof Player))
				return false;
			
			Player player = (Player)sender;
			if (!player.hasPermission("iocommands.whois")) {
				player.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
				return true;
			}
			
			String name = "";
			if (args.length > 0) {
				name = args[0];
			} else {
				player.sendMessage(ChatColor.RED + "Missing parameter: player name");
				return true;
			}
			
			Player target = Bukkit.getServer().getPlayer(name);
			if (target == null) {
				player.sendMessage(ChatColor.RED + "Target not found!");
				return true;
			}
			
			player.sendMessage(ChatColor.AQUA + "Information on " + target.getName());
			player.sendMessage(ChatColor.AQUA + "UUID: " + ChatColor.WHITE + target.getUniqueId());
			player.sendMessage(ChatColor.AQUA + "IP address: " + ChatColor.WHITE + target.getAddress().getAddress());
			return true;
		
		} else if (cmd.getName().equalsIgnoreCase("tutorial")) {
			
			boolean entry = false;
			boolean exit = false;
			if (args.length > 0) {
				exit = args[0].equalsIgnoreCase("exit");
				entry = args[0].equalsIgnoreCase("entry");
			}
			
			Player player = null;
			String playerName = "";
			
			if (entry || exit) {
				if ((entry && !sender.hasPermission("iocommands.tutorial.entry")) ||
					(exit && !sender.hasPermission("iocommands.tutorial.exit"))) {
					sender.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
					return true;
				}

				if (args.length > 1)
					playerName = args[1];
				else {
					sender.sendMessage(ChatColor.RED + "Missing parameter: player name");
					return true;
				}
				player = getServer().getPlayer(playerName);
			} else {
				if (!(sender instanceof Player))
					return false;
				
				player = (Player)sender;
			}
			
			tutorial.onCommand(player, exit);
			
			return true;

		} else if (cmd.getName().equalsIgnoreCase("profile")) {

			if (!(sender instanceof Player))
				return false;
		
			sender.sendMessage(ChatColor.AQUA + "View your web profile to see your progress!");
			sender.sendMessage(ChatColor.WHITE + "» " + ChatColor.BOLD + "get2.io/cl/profile");

			return true;

		} else if (cmd.getName().equalsIgnoreCase("rimshot")) {
			
			for (Player player: Bukkit.getOnlinePlayers()) {
				player.playSound(player.getLocation(), "rimshot", 1.0f, 1.0f);
				player.sendMessage("Ba-dum-tsch!");
			}
			return true;

		} else if (cmd.getName().equalsIgnoreCase("warning") || cmd.getName().equalsIgnoreCase("w")) {
			
			if (!sender.hasPermission("iocommands.warning")) {
				sender.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
				return true;
			}

			Player target = null;
			String playerName = "";

			if (args.length >= 1)
				playerName = args[0];
			else {
				sender.sendMessage(ChatColor.RED + "Missing parameter: player name");
				return true;
			}
			target = getServer().getPlayer(playerName);

			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Target not found: " + playerName);
				return true;
			}
			
			String message = "";
			if (args.length > 1) {
				message += strJoin(Arrays.copyOfRange(args, 1, args.length), " ");
			} else
				message += "No griefing will be tolerated. Griefing is breaking or taking anything that belongs to someone else, or adding to a structure that isn't yours, without permission.";

			target.sendMessage(ChatColor.RED + "WARNING: " + ChatColor.WHITE + message);
			target.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "WARNING", ChatColor.GOLD + playerName + ", see the chat now.", 10, 100, 10);
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30 * 20, 2), false);
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 30 * 20, 2), false);

			sender.sendMessage(ChatColor.GREEN + "Warning sent to " + playerName + ".");

			//Notify staff
			Bukkit.getServer().getLogger().info("|IOSTAFF|" + playerName + " was warned: " + message);

			return true;

		} else if (cmd.getName().equalsIgnoreCase("minecart")) {
			
			if (!sender.hasPermission("iocommands.minecart")) {
				sender.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
				return true;
			}

			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command must be used by a player.");
				return true;
			}

			Player player = (Player)sender;
			if (Minecarts.request(player)) {
				sender.sendMessage(ChatColor.GREEN + "Have an emergency minecart!");
				player.getInventory().addItem(new ItemStack(Material.MINECART));

			} else {
				sender.sendMessage(ChatColor.RED + "You can only get a minecart once every 24 hours.");
			}

			return true;

		} else if (cmd.getName().equalsIgnoreCase("restart")) {
			
			if (!sender.hasPermission("iocommands.restart")) {
				sender.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
				return true;
			}

			int duration = 1;
			if (args.length > 0) {
				try {
					duration = Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "You must enter a valid duration, in minutes.");
					return true;
				}
			}

			restart.scheduleRestart(duration);

			return true;

		} else if (cmd.getName().equalsIgnoreCase("tips")) {
			
			sender.sendMessage(ChatColor.AQUA + "Here are some useful tips and features available only here!");
			sender.sendMessage(ChatColor.WHITE + "» " + ChatColor.BOLD + "get2.io/cl/tips");
			
			return true;

		} else if (cmd.getName().equalsIgnoreCase("map") || cmd.getName().equalsIgnoreCase("maps")) {
			
			sender.sendMessage(ChatColor.AQUA + "The live maps for all servers can be found here.");
			sender.sendMessage(ChatColor.WHITE + "» " + ChatColor.BOLD + "get2.io/cl/maps");
			
			return true;

		} else if (cmd.getName().equalsIgnoreCase("rules")) {
			
			sender.sendMessage(ChatColor.AQUA + "We have a few rules in place for the benefit of everyone.");
			sender.sendMessage(ChatColor.WHITE + "1- Family friendly");
			sender.sendMessage(ChatColor.WHITE + "2- No discussions on religion and politics");
			sender.sendMessage(ChatColor.WHITE + "3- No flying, running or x-ray mods");
			sender.sendMessage(ChatColor.WHITE + "4- No lagging devices");
			sender.sendMessage(ChatColor.WHITE + "5- No griefing");
			sender.sendMessage(ChatColor.WHITE + "6- No player traps");
			sender.sendMessage(ChatColor.WHITE + "7- Respect private property");
			sender.sendMessage(ChatColor.WHITE + "8- Player safety");
			sender.sendMessage(ChatColor.WHITE + "» " + ChatColor.BOLD + "get2.io/cl/rules");
			
			return true;

		} else if (cmd.getName().equalsIgnoreCase("help")) {
			
			sender.sendMessage(ChatColor.AQUA + "Hello! If you have any questions about the server, don't hesitate to ask, the community likes to help.");
			sender.sendMessage(ChatColor.AQUA + "Useful commands:");
			sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "/rules" + ChatColor.RESET + ": A summary of our rules");
			sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "/register" + ChatColor.RESET + ": Access the registration instructions");
			sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "/tips" + ChatColor.RESET + ": Read on our gameplay changes and commands");
			sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "/map" + ChatColor.RESET + ": View the server maps");
			sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "/wiki" + ChatColor.RESET + ": Access or search our wiki");
			
			return true;

		} else if (cmd.getName().equalsIgnoreCase("kit")) {
			
			sender.sendMessage(ChatColor.AQUA + "Want a starting kit of items?");
			sender.sendMessage(ChatColor.AQUA + "Go to Cimmeria and try out the tutorial!");
			
			return true;
		
		} else if (cmd.getName().equalsIgnoreCase("broadcast")) {
			
			if (!sender.hasPermission("iocommands.broadcast")) {
				sender.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
				return true;
			}
			
			String command = strJoin(args, " ");
			Bukkit.getServer().getLogger().info("|IOBRD|" + command);
			
			return true;
		
		/*
		} else if (cmd.getName().equalsIgnoreCase("switch")) {

			if (!sender.hasPermission("iocommands.switch")) {
				sender.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
				return true;
			}

			Player target = null;
			String destination = "";

			if (args.length >= 2) {
				destination = args[0];
				target = getServer().getPlayer(args[1]);
			} else if (args.length == 1) {
				if (!(sender instanceof Player))
					return false;

				destination = args[0];
				target = (Player)sender;
			} else {
				sender.sendMessage(ChatColor.RED + "Missing parameter: destination server");
				return true;
			}


			//Send the world Switch message to Bungee
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);
			try {
				out.writeUTF("Connect");
				out.writeUTF(destination);
			} catch (IOException e) {
			}

			if (!bungeeInit) {
				this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
				bungeeInit = true;
			}

			//Save players so their inventory and metadata is up to date
			getServer().savePlayers();
			target.sendPluginMessage(this, "BungeeCord", b.toByteArray());

			return true;
		*/
		}
		
		return false;
	}
	
	
	//Process the time as provided in a command
	public long extractTime(String[] args, int pos) throws NumberFormatException {
		long time = 0;
		
		if (args[pos].endsWith("h") && args[pos].length() > 1) {
			time = Long.parseLong(args[pos].substring(0, args[pos].length() - 1));
			time = (time - 6) * 1000;
			
		} else {
			time = Long.parseLong(args[pos]);
		}
		
		return time;
	}


	//Set the flight status of the given player
	public void setFlightStatus(Player source, Player target, int status) {
		thisFlightManager.setFlightStatus(source, target, status);
	}


	//Reset our flag on a player's flight status
	public void resetPlayerFlight(Player player) {
		thisFlightManager.resetFlightStatus(player);
	}
	
	
	
	
	public static String strJoin(String[] aArr, String sSep) {
		return strJoin(aArr, sSep, 0);
	}
	
	
	public static String strJoin(String[] aArr, String sSep, int startPos) {
		if (aArr.length <= startPos)
			return "";
		
		StringBuilder sbStr = new StringBuilder();
		for (int i = startPos, il = aArr.length; i < il; i++) {
			if (i > startPos)
				sbStr.append(sSep);
			sbStr.append(aArr[i]);
		}
		return sbStr.toString();
	}


	//Return a preset message on how we don't allow teleports
	public static void explainNoTeleports(Player player) {
		player.sendMessage("We don't do teleports here!");
		player.sendMessage("We have an extensive network of rails, both in the Nether and on the overworld, to let you get to your destination quickly.");
		player.sendMessage("Look at the /maps and find where the closest rail or portal to you is located!");
		player.sendMessage("To set your spawn, use a bed or a respawn anchor.");
	}


	//Get the restart control instance
	public Restart getRestart() {
		return restart;
	}


	//Return the list of public messages
	public Map< String, List< String > > getLoginMessages() {
		return loginMessages;
	}
}
