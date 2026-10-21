package com.oblivioussp.spartanweaponryarcana.neoforge;

import com.oblivioussp.spartanweaponryarcana.platform.Platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgePlatform implements Platform
{
	@Override
	public String name()
	{
		return "neoforge";
	}

	@Override
	public boolean isModLoaded(String modId)
	{
		return ModList.get() != null && ModList.get().isLoaded(modId);
	}

	@Override
	public CreativeModeTab.Builder creativeTabBuilder()
	{
		return CreativeModeTab.builder();
	}

	@Override
	public void sendToServer(CustomPacketPayload payload)
	{
		PacketDistributor.sendToServer(payload);
	}
}
