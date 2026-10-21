package com.oblivioussp.spartanweaponryarcana.fabric;

import com.oblivioussp.spartanweaponryarcana.platform.Platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.CreativeModeTab;

public class FabricPlatform implements Platform
{
	@Override
	public String name()
	{
		return "fabric";
	}

	@Override
	public boolean isModLoaded(String modId)
	{
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public CreativeModeTab.Builder creativeTabBuilder()
	{
		return FabricItemGroup.builder();
	}

	@Override
	public void sendToServer(CustomPacketPayload payload)
	{
		ClientPlayNetworking.send(payload);
	}
}
