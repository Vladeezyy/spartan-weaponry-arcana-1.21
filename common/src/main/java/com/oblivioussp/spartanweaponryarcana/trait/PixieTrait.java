package com.oblivioussp.spartanweaponryarcana.trait;

import com.oblivioussp.spartanweaponryarcana.compat.botania.BotaniaCompat;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableMultimap;
import com.oblivioussp.spartanweaponry.api.trait.IGenericTraitCallback;
import com.oblivioussp.spartanweaponry.api.trait.WeaponTrait;
import com.oblivioussp.spartanweaponryarcana.ModSpartanWeaponryArcana;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

/**
 * Pixielated: 1.12.2 implemented Botania's IPixieSpawner on the weapon. Botania 1.21 reads the
 * "botania:pixie_spawn_chance" attribute instead, so the trait adds it to the weapon in the main hand (as Botania's
 * own Elementium sword does). Without Botania the attribute does not exist and the trait does nothing.
 */
public class PixieTrait extends WeaponTrait implements IGenericTraitCallback
{
	public static final Identifier PIXIE_SPAWN_CHANCE = Identifier.fromNamespaceAndPath("botania", "pixie_spawn_chance");
	public static final Identifier MODIFIER_ID = Identifier.fromNamespaceAndPath(ModSpartanWeaponryArcana.ID, "pixielated");

	public PixieTrait(String typeIn, String modIdIn, TraitQuality qualityIn)
	{
		super(typeIn, modIdIn, qualityIn);
		setUniversal();
	}

	@Override
	public Optional<IGenericTraitCallback> getGenericCallback()
	{
		return Optional.of(this);
	}

	@Override
	public void onModifyAttributes(ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder)
	{
		// Botania's own holder of the attribute (see BotaniaCompat#pixieSpawnChance)
		Holder<Attribute> attribute = BotaniaCompat.pixieSpawnChance();
		if(attribute != null)
			builder.put(attribute, new AttributeModifier(MODIFIER_ID, magnitude / 100.0f, AttributeModifier.Operation.ADD_VALUE));
	}

	@Override
	protected void addTooltipDescription(ItemStack stack, List<Component> tooltip)
	{
		tooltip.add(tooltipIndent().append(Component.translatable(String.format("tooltip.%s.trait.%s.desc", modId, type), String.format("%.0f", magnitude)).withStyle(DESCRIPTION_FORMAT)));
	}
}
