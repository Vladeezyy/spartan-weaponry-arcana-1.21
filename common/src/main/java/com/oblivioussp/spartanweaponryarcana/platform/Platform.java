package com.oblivioussp.spartanweaponryarcana.platform;

import java.util.ServiceLoader;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.CreativeModeTab;

/** What the common code needs from the mod loader (NeoForge or Fabric), found with the {@link ServiceLoader}. */
public interface Platform
{
	Platform INSTANCE = ServiceLoader.load(Platform.class, Platform.class.getClassLoader()).findFirst()
			.orElseThrow(() -> new IllegalStateException("No Spartan Weaponry: Arcana platform implementation"));

	/** "neoforge" or "fabric". */
	String name();

	boolean isModLoaded(String modId);

	CreativeModeTab.Builder creativeTabBuilder();

	void sendToServer(CustomPacketPayload payload);
}
