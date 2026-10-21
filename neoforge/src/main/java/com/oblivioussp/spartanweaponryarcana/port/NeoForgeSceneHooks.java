package com.oblivioussp.spartanweaponryarcana.port;

import com.oblivioussp.spartanweaponryarcana.ModSpartanWeaponryArcana;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** The NeoForge events of the test scenes (dev runs only: port/ is not in the jar). */
@EventBusSubscriber(modid = ModSpartanWeaponryArcana.ID, value = Dist.CLIENT)
public final class NeoForgeSceneHooks
{
	@SubscribeEvent
	public static void onTick(ClientTickEvent.Post event)
	{
		DevScene.onTick();
		NetScene.onTick();
	}
}
