package com.interordi.iocommands.modules;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.interordi.iocommands.IOCommands;

public class FunCommands {

	public static void shock(Player target, int amount, boolean fromPlayer) {
		
		//Actual action
		Runnable task = new Runnable() {
			@Override
			public void run() {
				target.getLocation().getWorld().strikeLightning(target.getLocation());
			}
		};
		
		//Pre-schedule all executions
		for (int i = 0; i < amount; i++)
			Bukkit.getScheduler().runTaskLater(IOCommands.instance, task, i * 20L);
		
		//Notify as appropriate
		if (fromPlayer) {
			if (amount > 1)
				Bukkit.broadcastMessage("§6" + target.getDisplayName() + " is being shocked " + amount + " times!");
			else
				Bukkit.broadcastMessage("§6" + target.getDisplayName() + " was shocked!");
		}
		
	}


	public static void rocket(Player target, int amount, boolean fromPlayer) {
		
		target.setVelocity(new Vector(0, amount + 1.0, 0));
		
		//Notify as appropriate
		if (fromPlayer) {
			Bukkit.broadcastMessage("§6" + target.getDisplayName() + " is heading for the skies!");
		}
		
	}


	public static void slap(Player target, int amount, boolean fromPlayer, float intensity) {
		
		//Actual action
		Runnable task = new Runnable() {
			@Override
			public void run() {
				target.setVelocity(new Vector(
					getRand(-1.0 * intensity, 1.0 * intensity),
					getRand(0.2, 1.0 * intensity),
					getRand(-1.0 * intensity, 1.0 * intensity)
				));
			}
		};
		
		//Pre-schedule all executions
		for (int i = 0; i < amount; i++)
			Bukkit.getScheduler().runTaskLater(IOCommands.instance, task, i * 20L);
		
		//Notify as appropriate
		if (fromPlayer) {
			if (amount > 1)
				Bukkit.broadcastMessage("§6" + target.getDisplayName() + " is being slapped " + amount + " times!");
			else
				Bukkit.broadcastMessage("§6" + target.getDisplayName() + " was slapped!");
		}
		
	}
	
	
	public static double getRand(double min, double max) {
		Random rand = new Random();
		return rand.nextFloat() * (max - min) + min;
	}
}
