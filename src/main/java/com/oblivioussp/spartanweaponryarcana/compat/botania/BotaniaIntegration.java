package com.oblivioussp.spartanweaponryarcana.compat.botania;

import com.oblivioussp.spartanweaponryarcana.init.ModItems;
import com.oblivioussp.spartanweaponryarcana.util.ItemData;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.entity.ManaBurstEntity;

/** The parts that use Botania's classes (only loaded with Botania, see {@link BotaniaCompat}) */
class BotaniaIntegration
{
	/** Mana per point of weapon durability, as Botania's Terra Blade and 1.12.2 (ManaBurstHelper.MANA_PER_DAMAGE) */
	static final int BURST_MANA = 100;
	private static final ResourceLocation TERRABLADE_SOUND = ResourceLocation.fromNamespaceAndPath("botania", "terrablade");

	static Item createTerraBurstLens()
	{
		return new TerraBurstLensItem(new Item.Properties().stacksTo(1));
	}

	static boolean requestManaForTool(ItemStack stack, Player player, int mana)
	{
		return ManaItemHandler.instance().requestManaExactForTool(stack, player, mana, true);
	}

	/**
	 * Botania's TerraBladeItem.trySpawnBurst / getBurst (1.12.2 ManaBurstHelper, "taken from the Botania mod by Vazkii"),
	 * with the weapon's damage instead of the Terra Blade's: the burst's lens is a Terra Burst lens carrying the
	 * damage and the attacker.
	 */
	static void trySpawnTerraBurst(Player player)
	{
		ItemStack weapon = player.getMainHandItem();
		if(player.getAttackStrengthScale(0.0f) != 1.0f || ModItems.TERRA_BURST_LENS == null)
			return;
		ManaBurstEntity burst = new ManaBurstEntity(player);
		float motionModifier = 7.0f;
		burst.setColor(0x20FF20);
		burst.setMana(BURST_MANA);
		burst.setStartingMana(BURST_MANA);
		burst.setMinManaLoss(40);
		burst.setManaLossPerTick(4.0f);
		burst.setGravity(0.0f);
		burst.setDeltaMovement(burst.getDeltaMovement().scale(motionModifier));
		ItemStack lens = new ItemStack(ModItems.TERRA_BURST_LENS.get());
		// 1.12.2: weapon damage + 1, which is the player's attack damage with the weapon in hand
		ItemData.setFloat(lens, TerraBurstLensItem.TAG_DAMAGE, (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE));
		burst.setSourceLens(lens);
		player.level().addFreshEntity(burst);
		// The burst costs one durability, paid with mana when the player has some (1.12.2 ToolCommons.damageItem)
		if(!requestManaForTool(weapon, player, BURST_MANA))
			weapon.hurtAndBreak(1, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
		BuiltInRegistries.SOUND_EVENT.getOptional(TERRABLADE_SOUND).ifPresent((sound) -> 
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 0.4f, 1.4f));
	}
}
