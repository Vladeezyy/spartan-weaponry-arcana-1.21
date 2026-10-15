package com.oblivioussp.spartanweaponryarcana.event;

import com.oblivioussp.spartanweaponryarcana.ModSpartanWeaponryArcana;
import com.oblivioussp.spartanweaponryarcana.compat.botania.BotaniaCompat;
import com.oblivioussp.spartanweaponryarcana.network.TerraBurstPacket;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ModSpartanWeaponryArcana.ID, value = Dist.CLIENT)
public class ClientEvents
{
	/** Terra Slash: a swing at the air (fired on the client only) */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onAttackMiss(PlayerInteractEvent.LeftClickEmpty ev)
	{
		if(BotaniaCompat.isLoaded() && TerraBurstPacket.hasTerraSlash(ev.getItemStack()))
			PacketDistributor.sendToServer(TerraBurstPacket.INSTANCE);
	}
}
