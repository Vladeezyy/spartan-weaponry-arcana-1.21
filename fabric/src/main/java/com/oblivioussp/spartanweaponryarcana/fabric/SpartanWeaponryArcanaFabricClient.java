package com.oblivioussp.spartanweaponryarcana.fabric;

import net.fabricmc.api.ClientModInitializer;

public class SpartanWeaponryArcanaFabricClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		// The Terra Slash swing at the air: mixin/MinecraftMixin. The test scenes (dev runs only, not in the jar):
		if(System.getProperty("spartanweaponryarcana.scene") != null || System.getProperty("spartanweaponryarcana.netscene") != null)
		{
			try
			{
				Class.forName("com.oblivioussp.spartanweaponryarcana.port.FabricSceneHooks").getMethod("init").invoke(null);
			}
			catch(ReflectiveOperationException e)
			{
				throw new IllegalStateException("The test scenes are not in this build", e);
			}
		}
	}
}
