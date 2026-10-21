package com.oblivioussp.spartanweaponryarcana.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.oblivioussp.spartanweaponryarcana.event.ClientEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** NeoForge PlayerInteractEvent.LeftClickEmpty: an attack that hits nothing (CommonHooks.onEmptyLeftClick, same place). */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin
{
	@Shadow
	public LocalPlayer player;

	@Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V", shift = At.Shift.AFTER))
	private void spartanweaponryarcana$attackMiss(CallbackInfoReturnable<Boolean> cir)
	{
		ClientEvents.onAttackMiss(player.getMainHandItem());
	}
}
