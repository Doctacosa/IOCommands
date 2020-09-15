package com.interordi.iocommands;

//import java.io.ByteArrayOutputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
import java.util.Arrays;
//import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
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
import com.interordi.utilities.CommandTargets;
import com.interordi.utilities.Commands;

public class IOCommands extends JavaPlugin {

	public static IOCommands instance;

	public Warps warps;
	public Homes homes;
	public WorldSpawns worldSpawns;
	public Tutorial tutorial;
	public FlightManager thisFlightManager;
	public PlayerListener thisPlayerListener;
	
	//private boolean bungeeInit = false;

	
	public void onEnable() {
		instance = this;
		
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		
		thisPlayerListener = new PlayerListener(this);
		this.warps = new Warps(this);
		this.homes = new Homes(this);
		this.worldSpawns = new WorldSpawns(this);
		this.tutorial = new Tutorial(this);
		thisFlightManager = new FlightManager(this);
		
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
			
			try {
				target.teleport(warp.location);
			} catch (java.lang.IllegalArgumentException e) {
				sender.sendMessage("§cThe destination is currently not available.");
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
				explainNoTeleports(player);
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
				explainNoTeleports(player);
				return true;
			}
			
			homes.setHome(player, player.getLocation());
			player.sendMessage("§aHome set!");
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
			
		} else if (cmd.getName().equalsIgnoreCase("keepinv")) {
			
			World target = null;
			boolean action = false;
			
			if (sender instanceof Player) {
				Player user = (Player)sender;
				//Check if the user has permission to use this command
				if (!user.hasPermission("iocommands.keepinv")) {
					user.sendMessage("§cYou are not allowed to use this command!");
					return true;
				}
				target = user.getWorld();
			}
			
			//Select the target of the command
			if (args.length == 1) {
				if (target == null) {
					sender.sendMessage("§cA world must be specified!");
					return true;
				}
				action = args[0].equals("true");
			} else if (args.length >= 2) {
				target = Bukkit.getServer().getWorld(args[0]);
				action = args[1].equals("true");
			} else {
				sender.sendMessage("§cA world must be specified!");
				return true;
			}
			
			if (target == null) {
				sender.sendMessage("§cWorld not found!");
				return true;
			}
			
			target.setGameRule(GameRule.KEEP_INVENTORY, action);
			sender.sendMessage("§aThe keepInventory rule for §f" + target.getName() + "§a is now §f" + action);
			
			return true;
		
		} else if (cmd.getName().equalsIgnoreCase("time")) {
			
			World target = null;
			boolean set = false;
			long time = 0;
			
			if (sender instanceof Player) {
				Player user = (Player)sender;
				//Check if the user has permission to use this command
				if (!user.hasPermission("iocommands.time")) {
					user.sendMessage("§cYou are not allowed to use this command!");
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
					sender.sendMessage("§cYou are not allowed to use this command!");
					return true;
				}
				
				if (!args[0].equals("set")) {
					target = Bukkit.getServer().getWorld(args[0]); 
				}
				
				try {
					time = extractTime(args, 1);
					set = true;
				} catch (NumberFormatException e) {
					sender.sendMessage("§cInvalid time value: " + args[1]);
					return true;
				}
				
			} else {
				sender.sendMessage("§cInvalid command!");
				return true;
			}
			
			if (target == null) {
				sender.sendMessage("§cWorld not found!");
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
				sender.sendMessage("§eTime for world " + target.getName() + " set to: " + String.format("%02.0f", hours) + ":" + String.format("%02.0f", minutes));
			} else {
				sender.sendMessage("§eTime: " + String.format("%02.0f", hours) + ":" + String.format("%02.0f", minutes));
			}
			
			return true;
		
		} else if (cmd.getName().equalsIgnoreCase("timeplus")) {
			
			//Check if the user has permission to use this command
			if (!sender.hasPermission("iocommands.time.set")) {
				sender.sendMessage("§cYou are not allowed to use this command!");
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
				sender.sendMessage("§cMissing parameter!");
				return true;
				
			}
			
			target = Bukkit.getServer().getWorld(args[0]);
			
			if (target == null) {
				sender.sendMessage("§cWorld not found!");
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

				getServer().broadcastMessage(String.format("%02.0f", hours) + ":" + String.format("%02.0f", minutes));
				
			} else {
				sender.sendMessage("§cInvalid command!");
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
					user.sendMessage("§cYou are not allowed to use this command!");
					return true;
				}
			}
			
			//Select the target of the command
			if (args.length >= 1) {
				target = Bukkit.getServer().getPlayer(args[0]);
			}
			
			if (target == null) {
				sender.sendMessage("§cTarget not found!");
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
				sender.sendMessage("§cThis command must be run in-game!");
				return true;
			}
			
			Player user = (Player)sender;
			//Check if the user has permission to use this command
			if (!user.hasPermission("iocommands.setworldspawn")) {
				user.sendMessage("§cYou are not allowed to use this command!");
				return true;
			}
			
			World w = user.getWorld();
			w.setSpawnLocation(user.getLocation());
			worldSpawns.setSpawn(user.getLocation());
			user.sendMessage("§aWorld spawn set!");
			
			return true;
			
		} else if (cmd.getName().equalsIgnoreCase("kill")) {
			
			if (!(sender instanceof Player)) {
				sender.sendMessage("§cOnly players can run this command!");
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
					user.sendMessage("§cYou are not allowed to use this command!");
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
				sender.sendMessage("§cTarget not found!");
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
				sender.sendMessage("§cYou are not allowed to use this command!");
				return true;
			}
			
			//Check and set the parameters
			if (args.length < 5) {
				sender.sendMessage("§cMissing parameters.");
				return true;
			}

			target = Bukkit.getServer().getPlayer(args[0]);

			if (target == null) {
				sender.sendMessage("§cTarget not found!");
				return true;
			}
			
			try {
				x = Float.parseFloat(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage("§cInvalid parameter: " + args[1]);
				return true;
			}
			
			try {
				y = Float.parseFloat(args[2]);
			} catch (NumberFormatException e) {
				sender.sendMessage("§cInvalid parameter: " + args[2]);
				return true;
			}
			
			try {
				z = Float.parseFloat(args[3]);
			} catch (NumberFormatException e) {
				sender.sendMessage("§cInvalid parameter: " + args[3]);
				return true;
			}
			
			try {
				delay = Float.parseFloat(args[4]);
			} catch (NumberFormatException e) {
				sender.sendMessage("§cInvalid parameter: " + args[4]);
				return true;
			}
			
			FunCommands.setVelocity(target, x, y, z, delay);
			
			return true;

		} else if (cmd.getName().equalsIgnoreCase("whois")) {
			
			if (!(sender instanceof Player))
				return false;
			
			Player player = (Player)sender;
			if (!player.hasPermission("iocommands.whois")) {
				player.sendMessage("§cYou are not allowed to use this command.");
				return true;
			}
			
			String name = "";
			if (args.length > 0) {
				name = args[0];
			} else {
				player.sendMessage("§cMissing parameter: player name");
				return true;
			}
			
			Player target = Bukkit.getServer().getPlayer(name);
			if (target == null) {
				player.sendMessage("§cTarget not found!");
				return true;
			}
			
			player.sendMessage("§aInformation on " + target.getName());
			player.sendMessage("§aUUID: §r" + target.getUniqueId());
			player.sendMessage("§aIP address: §r" + target.getAddress().getAddress());
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
					sender.sendMessage("§cYou are not allowed to use this command.");
					return true;
				}

				if (args.length > 1)
					playerName = args[1];
				else {
					sender.sendMessage("§cMissing parameter: player name");
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
		
			Player player = (Player)sender;

			sender.sendMessage("§aView your web profile to see your progress!");
			sender.sendMessage(ChatColor.WHITE + "https://www.interordi.com/mboard/mc_profile.php?username=" + player.getDisplayName());

			return true;

		} else if (cmd.getName().equalsIgnoreCase("rimshot")) {
			
			for (Player player: Bukkit.getOnlinePlayers()) {
				player.playSound(player.getLocation(), "rimshot", 1.0f, 1.0f);
				player.sendMessage("Ba-dum-tsch!");
			}
			return true;

		} else if (cmd.getName().equalsIgnoreCase("warning") || cmd.getName().equalsIgnoreCase("w")) {
			
			if (!sender.hasPermission("iocommands.warning")) {
				sender.sendMessage("§cYou are not allowed to use this command.");
				return true;
			}

			Player target = null;
			String playerName = "";

			if (args.length >= 1)
				playerName = args[0];
			else {
				sender.sendMessage("§cMissing parameter: player name");
				return true;
			}
			target = getServer().getPlayer(playerName);

			if (target == null) {
				sender.sendMessage("§cTarget not found: " + playerName);
				return true;
			}
			
			String message = "";
			if (args.length > 1) {
				message += strJoin(Arrays.copyOfRange(args, 1, args.length), " ");
			} else
				message += "No griefing will be tolerated. Griefing is breaking or taking anything that belongs to someone else, or adding to a structure that isn't yours, without permission.";

			target.sendMessage("§cWARNING: §f" + message);
			target.sendTitle("§c§lWARNING", "§6" + playerName + ", see the chat now.", 10, 100, 10);
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30 * 20, 2), false);
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 30 * 20, 2), false);

			//Notify staff
			Bukkit.getServer().getLogger().info("|IOSTAFF|" + playerName + " was warned: " + message);

			return true;

		} else if (cmd.getName().equalsIgnoreCase("tips")) {
			
			sender.sendMessage("§aHere are some useful tips and features available only here!");
			sender.sendMessage(ChatColor.WHITE + "https://www.creeperslab.net/tips.php");
			
			return true;

		} else if (cmd.getName().equalsIgnoreCase("map") || cmd.getName().equalsIgnoreCase("maps")) {
			
			sender.sendMessage("§aThe live maps for all servers can be found here.");
			sender.sendMessage(ChatColor.WHITE + "https://map.creeperslab.net/");
			
			return true;

		} else if (cmd.getName().equalsIgnoreCase("rules")) {
			
			sender.sendMessage("§aWe have a few rules in place for the benefit of everyone.");
			sender.sendMessage("§61- Family friendly");
			sender.sendMessage("§62- No discussions on religion and politics");
			sender.sendMessage("§63- No flying, running or x-ray mods");
			sender.sendMessage("§64- No lagging devices");
			sender.sendMessage("§65- No griefing");
			sender.sendMessage("§66- No player traps");
			sender.sendMessage("§67- Respect private property");
			sender.sendMessage("§68- Player safety");
			sender.sendMessage(ChatColor.WHITE + "https://www.creeperslab.net/rules.php");
			
			return true;

		} else if (cmd.getName().equalsIgnoreCase("help")) {
			
			sender.sendMessage("§aHello! If you have any questions about the server, don't hesitate to ask, the community likes to help.");
			sender.sendMessage("§rUseful commands:");
			sender.sendMessage("§6§l/rules§r: A summary of our rules");
			sender.sendMessage("§6§l/register§r: Access the registration instructions");
			sender.sendMessage("§6§l/tips§r: Read on our gameplay changes and commands");
			sender.sendMessage("§6§l/map§r: View the server maps");
			
			return true;

		} else if (cmd.getName().equalsIgnoreCase("kit")) {
			
			sender.sendMessage("§aWant a starting kit of items?");
			sender.sendMessage("§aGo to Cimmeria and try out the tutorial!");
			
			return true;
		
		} else if (cmd.getName().equalsIgnoreCase("broadcast")) {
			
			if (!sender.hasPermission("iocommands.broadcast")) {
				sender.sendMessage("§cYou are not allowed to use this command.");
				return true;
			}
			
			String command = strJoin(args, " ");
			Bukkit.getServer().getLogger().info("|IOBRD|" + command);
			
			return true;
		
		/*
		} else if (cmd.getName().equalsIgnoreCase("switch")) {

			if (!sender.hasPermission("iocommands.switch")) {
				sender.sendMessage("§cYou are not allowed to use this command.");
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
				sender.sendMessage("§cMissing parameter: destination server");
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


}
