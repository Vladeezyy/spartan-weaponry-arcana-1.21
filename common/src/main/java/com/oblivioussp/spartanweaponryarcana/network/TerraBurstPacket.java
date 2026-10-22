package com.oblivioussp.spartanweaponryarcana.network;

import com.oblivioussp.spartanweaponry.api.IWeaponTraitContainer;
import com.oblivioussp.spartanweaponryarcana.ModSpartanWeaponryArcana;
import com.oblivioussp.spartanweaponryarcana.compat.botania.BotaniaCompat;
import com.oblivioussp.spartanweaponryarcana.init.ModTraits;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Sent when the player swings a Terra Slash weapon at the air (1.12.2 PacketManaBurst). The server checks the weapon in
 * the main hand and the attack strength itself, as Botania's Terra Blade does.
 */
public record TerraBurstPacket() implements CustomPacketPayload
{
	public static final TerraBurstPacket INSTANCE = new TerraBurstPacket();
	public static final CustomPacketPayload.Type<TerraBurstPacket> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ModSpartanWeaponryArcana.ID, "terra_burst"));
	public static final StreamCodec<RegistryFriendlyByteBuf, TerraBurstPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public CustomPacketPayload.Type<TerraBurstPacket> type()
	{
		return TYPE;
	}

	public static boolean hasTerraSlash(ItemStack stack)
	{
		return !stack.isEmpty() && stack.getItem() instanceof IWeaponTraitContainer<?> container
				&& container.getAllWeaponTraits().contains(ModTraits.TERRA_SLASH.get());
	}

	public static void handle(Player player)
	{
		if(player != null && !player.isSpectator() && BotaniaCompat.isLoaded() && hasTerraSlash(player.getMainHandItem()))
			BotaniaCompat.trySpawnTerraBurst(player);
	}
}
