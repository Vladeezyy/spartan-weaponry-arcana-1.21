package com.oblivioussp.spartanweaponryarcana.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** 1.12.2: a SimpleNetworkWrapper channel; 1.21: NeoForge payloads (client to server) */
public class NetworkHandler
{
	public static void register(RegisterPayloadHandlersEvent ev)
	{
		PayloadRegistrar registrar = ev.registrar("1");
		registrar.playToServer(TerraBurstPacket.TYPE, TerraBurstPacket.STREAM_CODEC, (packet, ctx) -> ctx.enqueueWork(() -> TerraBurstPacket.handle(ctx.player())));
	}
}
