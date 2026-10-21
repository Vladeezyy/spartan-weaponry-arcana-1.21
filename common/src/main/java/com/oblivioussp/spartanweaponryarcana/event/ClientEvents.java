package com.oblivioussp.spartanweaponryarcana.event;

import com.oblivioussp.spartanweaponryarcana.compat.botania.BotaniaCompat;
import com.oblivioussp.spartanweaponryarcana.network.TerraBurstPacket;
import com.oblivioussp.spartanweaponryarcana.platform.Platform;

import net.minecraft.world.item.ItemStack;

/** Client event handlers; the NeoForge and Fabric modules call them. */
public class ClientEvents
{
	/**
	 * Terra Slash: a swing at the air (fired on the client only; NeoForge PlayerInteractEvent.LeftClickEmpty, Fabric:
	 * a mixin in Minecraft.startAttack)
	 */
	public static void onAttackMiss(ItemStack mainHand)
	{
		if(BotaniaCompat.isLoaded() && TerraBurstPacket.hasTerraSlash(mainHand))
			Platform.INSTANCE.sendToServer(TerraBurstPacket.INSTANCE);
	}
}
