package com.interordi.iocommands.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.interordi.iocommands.IOCommands;
import com.interordi.iocommands.utilities.MobSpawn;

public class SpawnMob {
	
	IOCommands plugin;
	
	
	public SpawnMob(IOCommands plugin) {
		this.plugin = plugin;
	}
	
	
	//Spawn one or many mobs as requested
	public boolean spawn(CommandSender sender, String rawMobs, int amount, double spread, Location pos) {

		//Get the list of mobs to spawn
		List< MobSpawn > mobs = new ArrayList< MobSpawn >();

		for (String mobDef : Arrays.asList(rawMobs.split("\\|")).reversed()) {

			String pattern = "([a-zA-Z0-9]+)|([*:\\-@])([a-zA-Z0-9.]+)";

			Pattern regex = Pattern.compile(pattern);
			Matcher matcher = regex.matcher(mobDef);

			MobSpawn mobSpawn = null;

			while (matcher.find()) {
				//Set mob type
				if (matcher.group(1) != null) {
					String mob = matcher.group(1);

					EntityType mobType = EntityType.fromName(mob);
					if (mobType == null) {
						sender.sendMessage(ChatColor.RED + "Mob type not found: " + mob);
						return false;
					}

					mobSpawn = new MobSpawn(mobType);
				} else {
					//Set the parameters
					switch(matcher.group(2)) {
						case "*":
							try {
								mobSpawn.size = Float.parseFloat(matcher.group(3));
							} catch (NumberFormatException e) {
								//Ignore
							}
						break;

						case ":":
							mobSpawn.modifiers.add(matcher.group(3).toLowerCase());
						break;
					}
				}
			}

			if (mobSpawn != null)
				mobs.add(mobSpawn);
		}

		if (amount <= 0) {
			sender.sendMessage(ChatColor.RED + "You need at least one!");
			return false;
		}

		Location mobPos = pos.clone();
		for (int i = 0; i < amount; i++) {
			if (spread > 0) {
				double orientation = Math.random() * Math.PI * 2;
				double distance = Math.random() * spread;
				mobPos.setX(pos.getX() + Math.sin(orientation) * distance);
				mobPos.setZ(pos.getZ() + Math.cos(orientation) * distance);
			}

			Entity parent = null;
			for (MobSpawn mob : mobs) {
				Entity entity = pos.getWorld().spawnEntity(mobPos, mob.mobType);

				if (mob.size != 1 && entity instanceof Attributable) {
					Attributable aEntity = (Attributable)entity;
					aEntity.getAttribute(Attribute.SCALE).setBaseValue(mob.size);
				}

				for (String modifier : mob.modifiers) {
					switch (modifier) {
						case "fire":
							entity.setVisualFire(true);
						break;

						case "invisible":
							if (entity instanceof LivingEntity) {
								LivingEntity lEntity = (LivingEntity)entity;
								lEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 180 * 20, 1));
							}
						break;

						case "charged":
							if (entity instanceof Creeper) {
								Creeper lEntity = (Creeper)entity;
								lEntity.setPowered(true);
							}
						break;
					}
				}

				if (parent != null)
					entity.addPassenger(parent);
				parent = entity;

				//zombie.setCustomName("Undead Warrior"); // Example of customization
				//zombie.setCustomNameVisible(true);
				//TODO: drops, equipment, custom NBT?
			}
		}

		return true;
	}

}
