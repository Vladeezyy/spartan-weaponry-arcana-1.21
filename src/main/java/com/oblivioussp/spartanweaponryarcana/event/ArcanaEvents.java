package com.oblivioussp.spartanweaponryarcana.event;

import com.oblivioussp.spartanweaponry.api.IWeaponTraitContainer;
import com.oblivioussp.spartanweaponry.api.trait.WeaponTrait;
import com.oblivioussp.spartanweaponryarcana.ModSpartanWeaponryArcana;
import com.oblivioussp.spartanweaponryarcana.compat.botania.BotaniaCompat;
import com.oblivioussp.spartanweaponryarcana.init.ModTraits;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Mana Repair and Warped. 1.12.2 ran them in the weapons' onUpdate, for every weapon kind; Spartan Weaponry 1.21 only
 * updates the traits of melee weapons, so they run here for every Arcana trait weapon in a player's inventory.
 */
@EventBusSubscriber(modid = ModSpartanWeaponryArcana.ID)
public class ArcanaEvents
{
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post ev)
	{
		Player player = ev.getEntity();
		if(player.level().isClientSide || player.isSpectator())
			return;
		// Port fix: 1.12.2 counted a global tick counter up once per player per tick, so with several players online
		// Warped weapons repaired faster and at uneven times; this uses the world time (once per second)
		boolean secondTick = player.level().getGameTime() % 20 == 0;
		var inventory = player.getInventory();
		for(int i = 0; i < inventory.getContainerSize(); i++)
		{
			ItemStack stack = inventory.getItem(i);
			if(stack.isEmpty() || !stack.isDamaged() || !(stack.getItem() instanceof IWeaponTraitContainer<?> container))
				continue;
			for(WeaponTrait trait : container.getAllWeaponTraits())
			{
				if(trait == ModTraits.MANA_REGENERATE.get() || trait == ModTraits.MANA_REGENERATE_TERRA.get())
				{
					if(BotaniaCompat.isLoaded() && BotaniaCompat.requestManaForTool(stack, player, (int)(trait.getMagnitude() * 2)))
						stack.setDamageValue(stack.getDamageValue() - 1);
				}
				else if(trait == ModTraits.WARPED.get() && secondTick)
					// 1.12.2 also set the damage to -1 on undamaged weapons (no damage check)
					stack.setDamageValue(stack.getDamageValue() - 1);
				if(!stack.isDamaged())
					break;
			}
		}
	}
}
