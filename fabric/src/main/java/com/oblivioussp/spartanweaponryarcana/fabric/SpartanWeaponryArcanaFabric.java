package com.oblivioussp.spartanweaponryarcana.fabric;

import com.oblivioussp.spartanweaponryarcana.ModSpartanWeaponryArcana;
import com.oblivioussp.spartanweaponryarcana.event.ArcanaEvents;
import com.oblivioussp.spartanweaponryarcana.network.TerraBurstPacket;
import com.oblivioussp.spartanweaponryarcana.platform.Registrar;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;

/**
 * The Fabric start of the mod: Spartan Weaponry calls it (entrypoint "spartanweaponry" in fabric.mod.json) after its
 * own start, since the weapons are made by its API (Fabric does not order the "main" entrypoints of dependent mods).
 */
public class SpartanWeaponryArcanaFabric implements ModInitializer
{
	@Override
	public void onInitialize()
	{
		ModSpartanWeaponryArcana.init();
		registerAll();

		PayloadTypeRegistry.serverboundPlay().register(TerraBurstPacket.TYPE, TerraBurstPacket.STREAM_CODEC);
		// Fabric runs the receivers on the server thread
		ServerPlayNetworking.registerGlobalReceiver(TerraBurstPacket.TYPE, (packet, ctx) -> TerraBurstPacket.handle(ctx.player()));
		// NeoForge PlayerTickEvent.Post (the handler works on the server side only)
		ServerTickEvents.END_SERVER_TICK.register(server ->
		{
			for(ServerPlayer player : server.getPlayerList().getPlayers())
				ArcanaEvents.onPlayerTick(player);
		});
	}

	/** Registers every registrar, registry by registry in the order of BuiltInRegistries.REGISTRY (as NeoForge does). */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void registerAll()
	{
		for(Registry<?> registry : BuiltInRegistries.REGISTRY)
		{
			for(Registrar<?> registrar : Registrar.all())
			{
				if(!registry.key().equals(registrar.registryKey()))
					continue;
				for(Registrar.Entry entry : registrar.getEntries())
					Registry.register((Registry)registry, entry.getKey(), Registrar.create(entry));
			}
		}
	}
}
