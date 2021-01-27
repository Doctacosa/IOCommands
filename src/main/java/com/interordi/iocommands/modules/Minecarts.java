package com.interordi.iocommands.modules;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class Minecarts {

	private static Map< UUID, LocalDateTime > lastUsage;


	public static void init() {
		//TODO: Load and save values to keep them between restarts
		lastUsage = new HashMap< UUID, LocalDateTime >();
	}


	//Try to request a minecart, deny if done within 24 hours
	public static boolean request(Player player) {
		UUID uuid = player.getUniqueId();
		if (lastUsage.containsKey(uuid) && lastUsage.get(uuid) != null) {
			LocalDateTime check = LocalDateTime.now().minusDays(1);
			if (lastUsage.get(uuid).compareTo(check) > 0)
				return false;
		}

		lastUsage.put(uuid, LocalDateTime.now());
		return true;
	}
	
}
