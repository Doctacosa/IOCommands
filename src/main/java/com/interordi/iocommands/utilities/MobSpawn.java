package com.interordi.iocommands.utilities;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.EntityType;

public class MobSpawn {
	public EntityType mobType = EntityType.ZOMBIE;
	public float size = 1;
	public Set< String > modifiers = new HashSet< String >();


	public MobSpawn(EntityType mobType) {
		this.mobType = mobType;
	}
}
