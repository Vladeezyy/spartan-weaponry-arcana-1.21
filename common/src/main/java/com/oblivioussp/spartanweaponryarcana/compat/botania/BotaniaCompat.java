package com.oblivioussp.spartanweaponryarcana.compat.botania;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Botania has no 1.21.11 version, so this branch has no Botania integration (1.21.1: BotaniaIntegration behind these
 * calls). The Botania materials' weapons are there, their recipes need Botania items; Mana Repair, Pixie and Terra
 * Slash do nothing without it, as on 1.21.1 without Botania.
 */
public class BotaniaCompat
{
	public static boolean isLoaded()
	{
		return false;
	}

	/** Botania's pixie spawn chance attribute: none without Botania */
	public static Holder<Attribute> pixieSpawnChance()
	{
		return null;
	}

	/** Takes mana from the player's mana items for a tool: no mana without Botania */
	public static boolean requestManaForTool(ItemStack stack, Player player, int mana)
	{
		return false;
	}

	public static void trySpawnTerraBurst(Player player)
	{
	}
}
