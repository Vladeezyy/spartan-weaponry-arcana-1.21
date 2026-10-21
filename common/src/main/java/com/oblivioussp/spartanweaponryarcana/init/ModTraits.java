package com.oblivioussp.spartanweaponryarcana.init;

import com.oblivioussp.spartanweaponry.api.WeaponTraits;
import com.oblivioussp.spartanweaponry.api.trait.WeaponTrait;
import com.oblivioussp.spartanweaponry.api.trait.WeaponTrait.TraitQuality;
import com.oblivioussp.spartanweaponryarcana.ModSpartanWeaponryArcana;
import com.oblivioussp.spartanweaponryarcana.trait.PixieTrait;

import com.oblivioussp.spartanweaponryarcana.platform.Registrar;

/**
 * The 1.12.2 weapon properties as Spartan Weaponry weapon traits. Materials get them through the trait tags
 * data/spartanweaponryarcana/tags/spartanweaponry/weapon_traits/materials/*.json. Their effects run in
 * {@link com.oblivioussp.spartanweaponryarcana.event.ArcanaEvents} for every kind of weapon (Spartan Weaponry only
 * updates the traits of melee weapons each tick).
 */
public class ModTraits
{
	public static final Registrar<WeaponTrait> REGISTRY = Registrar.create(WeaponTraits.REGISTRY_KEY);

	public static final String TYPE_MANA_REGENERATE = "mana_regenerate";
	public static final String TYPE_TERRA_SLASH = "terra_slash";
	public static final String TYPE_PIXIELATED = "pixielated";
	public static final String TYPE_WARPED = "warped";

	/** Repairs one durability for magnitude × 2 mana from mana items in the inventory (Botania), every tick */
	public static final Registrar.Entry<WeaponTrait, WeaponTrait> MANA_REGENERATE = REGISTRY.register("mana_regenerate",
			() -> new WeaponTrait(TYPE_MANA_REGENERATE, ModSpartanWeaponryArcana.ID, TraitQuality.POSITIVE).setMagnitude(60.0f).setUniversal());
	public static final Registrar.Entry<WeaponTrait, WeaponTrait> MANA_REGENERATE_TERRA = REGISTRY.register("mana_regenerate_terra",
			() -> new WeaponTrait(TYPE_MANA_REGENERATE, ModSpartanWeaponryArcana.ID, TraitQuality.POSITIVE).setMagnitude(100.0f).setUniversal());
	/** A swing at the air with full attack strength fires a mana burst dealing the weapon's damage (Botania) */
	public static final Registrar.Entry<WeaponTrait, WeaponTrait> TERRA_SLASH = REGISTRY.register("terra_slash",
			() -> new WeaponTrait(TYPE_TERRA_SLASH, ModSpartanWeaponryArcana.ID, TraitQuality.POSITIVE).setMelee());
	/** magnitude % chance that a Pixie spawns when the holder is hurt (Botania's pixie spawn chance attribute) */
	public static final Registrar.Entry<WeaponTrait, WeaponTrait> PIXIELATED = REGISTRY.register("pixielated_5",
			() -> new PixieTrait(TYPE_PIXIELATED, ModSpartanWeaponryArcana.ID, TraitQuality.POSITIVE).setMagnitude(5.0f));
	/** Repairs one durability every second (Thaumcraft void metal) */
	public static final Registrar.Entry<WeaponTrait, WeaponTrait> WARPED = REGISTRY.register("warped",
			() -> new WeaponTrait(TYPE_WARPED, ModSpartanWeaponryArcana.ID, TraitQuality.POSITIVE).setUniversal());
}
