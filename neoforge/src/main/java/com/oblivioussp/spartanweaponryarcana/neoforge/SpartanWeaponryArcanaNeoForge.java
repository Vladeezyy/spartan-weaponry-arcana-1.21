package com.oblivioussp.spartanweaponryarcana.neoforge;

import com.oblivioussp.spartanweaponryarcana.ModSpartanWeaponryArcana;
import com.oblivioussp.spartanweaponryarcana.event.ArcanaEvents;
import com.oblivioussp.spartanweaponryarcana.network.TerraBurstPacket;
import com.oblivioussp.spartanweaponryarcana.platform.Registrar;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(ModSpartanWeaponryArcana.ID)
public class SpartanWeaponryArcanaNeoForge
{
	public SpartanWeaponryArcanaNeoForge(IEventBus modBus, ModContainer container)
	{
		ModSpartanWeaponryArcana.init();
		modBus.addListener(SpartanWeaponryArcanaNeoForge::register);
		modBus.addListener((RegisterPayloadHandlersEvent ev) -> ev.registrar("1").playToServer(TerraBurstPacket.TYPE, TerraBurstPacket.STREAM_CODEC,
				(packet, ctx) -> ctx.enqueueWork(() -> TerraBurstPacket.handle(ctx.player()))));
		NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post ev) -> ArcanaEvents.onPlayerTick(ev.getEntity()));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void register(RegisterEvent ev)
	{
		for(Registrar<?> registrar : Registrar.all())
		{
			if(ev.getRegistryKey().equals(registrar.registryKey()))
			{
				for(Registrar.Entry entry : registrar.getEntries())
					ev.register((ResourceKey<? extends Registry<Object>>)registrar.registryKey(), entry.getId(), () -> Registrar.create(entry));
			}
		}
	}
}
