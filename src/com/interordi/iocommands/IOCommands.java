package com.interordi.iocommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.interordi.iocommands.modules.Warp;
import com.interordi.iocommands.modules.Warps;

public class IOCommands extends JavaPlugin {

	public static IOCommands instance;
	public Warps warps;
	
	
	public void onEnable() {
		instance = this;
		
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		
		this.warps = new Warps(this);
		
		getLogger().info("IOCommands enabled");
	}
	
	
	public void onDisable() {
		getLogger().info("IOCommands disabled");
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("warp")) {
			
			//Fancy display to players, basic for others like the console
			if (!(sender instanceof Player))
				return false;
			
			Player player = (Player)sender;
			if (!player.hasPermission("iocommands.warp")) {
				player.sendMessage("§cYou are not allowed to use this command.");
				return true;
			}
			
			Player target = player;
			String name = "";
			if (args.length > 1) {
				target = Bukkit.getServer().getPlayer(args[0]);
				if (target == null) {
					player.sendMessage("§cTarget player not found!");
					return true;
				}
				name = args[1];
			} else if (args.length == 1) {
				name = args[0];
			} else {
				player.sendMessage("§cMissing parameter: destination");
				return true;
			}
			
			Warp warp = warps.getWarp(name);
			if (warp == null) {
				player.sendMessage("§cDestination not found!");
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
		}
		
		return false;
	}
}
