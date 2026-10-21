package com.oblivioussp.spartanweaponryarcana.compat.botania;

import java.util.List;

import com.oblivioussp.spartanweaponryarcana.util.ItemData;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.mana.LensEffectItem;

/**
 * The effect of a Terra Burst: Botania's TerraBladeItem.updateBurst (1.12.2 ManaBurstHelper.updateBurst), dealing the
 * damage stored on the lens. 1.12.2 implemented this on every weapon item; 1.21 weapons are Spartan Weaponry's own
 * classes, so the burst carries this internal item as its lens instead.
 */
public class TerraBurstLensItem extends Item implements LensEffectItem
{
	public static final String TAG_DAMAGE = "Damage";

	public TerraBurstLensItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public void apply(ItemStack stack, BurstProperties props, Level level) {}

	@Override
	public boolean collideBurst(ManaBurst burst, HitResult pos, boolean isManaBlock, boolean shouldKill, ItemStack stack)
	{
		return shouldKill;
	}

	@Override
	public void updateBurst(ManaBurst burst, ItemStack stack)
	{
		Projectile entity = burst.entity();
		AABB axis = new AABB(entity.getX(), entity.getY(), entity.getZ(), entity.xOld, entity.yOld, entity.zOld).inflate(1);
		List<LivingEntity> entities = entity.level().getEntitiesOfClass(LivingEntity.class, axis);
		Entity thrower = entity.getOwner();

		for(LivingEntity living : entities)
		{
			if(living == thrower || living instanceof Player livingPlayer && thrower instanceof Player throwingPlayer && !throwingPlayer.canHarmPlayer(livingPlayer))
				continue;

			if(living.hurtTime == 0)
			{
				int cost = BotaniaIntegration.BURST_MANA / 3;
				int mana = burst.getMana();
				if(mana >= cost)
				{
					burst.setMana(mana - cost);
					float damage = ItemData.getFloat(stack, TAG_DAMAGE, 1.0f);
					if(!burst.isFake() && !entity.level().isClientSide())
					{
						DamageSource source = living.damageSources().magic();
						if(thrower instanceof Player player)
							source = player.damageSources().playerAttack(player);
						else if(thrower instanceof LivingEntity livingEntity)
							source = livingEntity.damageSources().mobAttack(livingEntity);
						living.hurt(source, damage);
						entity.discard();
						break;
					}
				}
			}
		}
	}

	@Override
	public boolean doParticles(ManaBurst burst, ItemStack stack)
	{
		return true;
	}
}
