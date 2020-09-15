package com.interordi.iocommands.modules;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.interordi.iocommands.IOCommands;

public class FunCommands {

	public static void shock(Player target, int amount, int delay, boolean fromPlayer) {
		
		//Actual action
		Runnable task = new Runnable() {
			@Override
			public void run() {
				target.getLocation().getWorld().strikeLightning(target.getLocation());
			}
		};
		
		//Pre-schedule all executions
		for (int i = 0; i < amount; i++)
			Bukkit.getScheduler().runTaskLater(IOCommands.instance, task, i * delay);
		
		//Notify as appropriate
		if (fromPlayer) {
			if (amount > 1)
				Bukkit.broadcastMessage("§6" + target.getDisplayName() + " is being shocked " + amount + " times!");
			else
				Bukkit.broadcastMessage("§6" + target.getDisplayName() + " was shocked!");
		}
		
	}


	public static void rocket(Player target, int amount, int delay, boolean fromPlayer) {
		
		target.setVelocity(new Vector(0, amount + 1.0, 0));
		
		//Notify as appropriate
		if (fromPlayer) {
			Bukkit.broadcastMessage("§6" + target.getDisplayName() + " is heading for the skies!");
		}
		
	}


	public static void slap(Player target, int amount, int delay, boolean fromPlayer, float intensity) {
		
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
			Bukkit.getScheduler().runTaskLater(IOCommands.instance, task, i * delay);
		
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


	//Set the velocity of a player and make them go wheeeee
	public static void setVelocity(Player target, float x, float y, float z, float delay) {
		//Actual action
		Runnable task = new Runnable() {
			@Override
			public void run() {
				target.setVelocity(new Vector(x, y, z));
			}
		};
		
		//Pre-schedule all executions
		Bukkit.getScheduler().runTaskLater(IOCommands.instance, task, Math.round(delay * 20));
	}
}
