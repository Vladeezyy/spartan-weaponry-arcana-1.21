package com.oblivioussp.spartanweaponryarcana.port;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/** The Fabric events of the test scenes (dev runs only: port/ is not in the jar; the client entry point loads this by name). */
public final class FabricSceneHooks
{
	public static void init()
	{
		ClientTickEvents.END_CLIENT_TICK.register(mc ->
		{
			DevScene.onTick();
			NetScene.onTick();
		});
	}
}
