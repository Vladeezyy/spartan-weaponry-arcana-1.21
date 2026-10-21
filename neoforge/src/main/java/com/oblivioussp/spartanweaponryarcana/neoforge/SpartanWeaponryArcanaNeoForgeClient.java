package com.oblivioussp.spartanweaponryarcana.neoforge;

import com.oblivioussp.spartanweaponryarcana.ModSpartanWeaponryArcana;
import com.oblivioussp.spartanweaponryarcana.event.ClientEvents;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(value = ModSpartanWeaponryArcana.ID, dist = Dist.CLIENT)
public class SpartanWeaponryArcanaNeoForgeClient
{
	public SpartanWeaponryArcanaNeoForgeClient(IEventBus modBus)
	{
		NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (PlayerInteractEvent.LeftClickEmpty ev) -> ClientEvents.onAttackMiss(ev.getItemStack()));
	}
}
