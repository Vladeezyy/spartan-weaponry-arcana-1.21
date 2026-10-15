package com.oblivioussp.spartanweaponryarcana.compat.botania;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/**
 * Entry point to the Botania integration. This class does not touch Botania's classes; {@link BotaniaIntegration}
 * does, and is only loaded when Botania is. Botania has no 1.21.1 release yet: the port is compiled against its
 * 1.21.1-porting branch, and a linkage error (a changed Botania API) turns the integration off instead of crashing.
 */
public class BotaniaCompat
{
	private static final boolean LOADED = ModList.get() != null && ModList.get().isLoaded("botania");
	private static boolean broken = false;

	public static boolean isLoaded()
	{
		return LOADED && !broken;
	}

	public static Item createTerraBurstLens()
	{
		return BotaniaIntegration.createTerraBurstLens();
	}

	/** Takes mana from the player's mana items for a tool (Botania ManaItemHandler.requestManaExactForTool) */
	public static boolean requestManaForTool(ItemStack stack, Player player, int mana)
	{
		try
		{
			return BotaniaIntegration.requestManaForTool(stack, player, mana);
		}
		catch(LinkageError e)
		{
			disable(e);
			return false;
		}
	}

	public static void trySpawnTerraBurst(Player player)
	{
		try
		{
			BotaniaIntegration.trySpawnTerraBurst(player);
		}
		catch(LinkageError e)
		{
			disable(e);
		}
	}

	private static void disable(LinkageError e)
	{
		broken = true;
		com.oblivioussp.spartanweaponryarcana.util.Log.error("Botania integration disabled, this Botania version has a different API: " + e);
	}
}
