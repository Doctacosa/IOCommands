package com.interordi.iocommands.modules;

import com.interordi.iocommands.IOCommands;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class Restart {

	private boolean shuttingDown = false;
	private BossBar indicator = null;
	private int countdown = 999;	//Countdown period, in seconds


	//Schedule a shutdown
	public void scheduleRestart(int time) {

		shuttingDown = true;
		countdown = time * 60;

		//Display a status bar for everyone currently corrected
		indicator = Bukkit.createBossBar("Shutdown", BarColor.RED, BarStyle.SOLID);
		for (Player player : Bukkit.getOnlinePlayers()) {
			indicator.addPlayer(player);
			indicator.setProgress(1.0);

			player.sendTitle(ChatColor.YELLOW + "Preparing to shutdown", "Please disconnect when ready", 10, 70, 10);
		}

		//Update the display every second
		Bukkit.getScheduler().scheduleSyncRepeatingTask(IOCommands.instance, new Runnable() {
			@Override
			public void run() {
				if (countdown <= 0)
					return;

				//Countdown to the shutdown
				indicator.setProgress(1.0 * countdown / (time * 60L));

				//Display a countdown
				if (countdown <= 10) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						player.sendTitle(ChatColor.YELLOW + "" + countdown, "Shutdown in progress", 0, 5 * 20, 0);
					}
				}

				countdown -= 1;
			}
		}, 0L, 20L);

		//Cleanly close the server when done
		Bukkit.getScheduler().runTaskLater(IOCommands.instance, new Runnable() {
			@Override
			public void run() {
				Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "save-all");
				Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "stop");
			}
		}, time * 60 * 20L);
	}


	public boolean isShuttingDown() {
		return shuttingDown;
	}
}
