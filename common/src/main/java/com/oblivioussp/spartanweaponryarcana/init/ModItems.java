package com.oblivioussp.spartanweaponryarcana.init;

import com.oblivioussp.spartanweaponry.api.ItemIds;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.oblivioussp.spartanweaponry.api.SpartanWeaponryAPI;
import com.oblivioussp.spartanweaponry.api.WeaponMaterial;
import com.oblivioussp.spartanweaponry.api.trait.WeaponTrait;
import com.oblivioussp.spartanweaponryarcana.ModSpartanWeaponryArcana;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import com.oblivioussp.spartanweaponryarcana.platform.Platform;
import com.oblivioussp.spartanweaponryarcana.platform.Registrar;

import net.minecraft.core.registries.Registries;
import com.oblivioussp.spartanweaponry.api.WeaponTraits;

/**
 * The materials and items. 1.12.2 ids were "dagger_manasteel", "material" with metadata; 1.21 follows Spartan
 * Weaponry's own naming ("manasteel_dagger", "livingwood_handle"). The weapon types are those of 1.12.2 (hammer →
 * battle hammer, throwing axe → tomahawk, mace → flanged mace, staff → quarterstaff, crossbow → heavy crossbow).
 */
public class ModItems
{
	public static final Registrar<Item> REGISTRY = Registrar.create(Registries.ITEM);

	// Stats of 1.12.2 (Botania's and Thaumcraft's tool materials, the same numbers without those mods; Dawnstone its own):
	// durability, efficiency, attack damage, enchantability
	public static final WeaponMaterial MANASTEEL = material("manasteel", 300, 6.2f, 2.0f, 20);
	public static final WeaponMaterial TERRASTEEL = material("terrasteel", 2300, 9.0f, 3.0f, 26);
	public static final WeaponMaterial ELEMENTIUM = material("elementium", 720, 6.2f, 2.0f, 20);
	public static final WeaponMaterial THAUMIUM = material("thaumium", 500, 7.0f, 2.5f, 22);
	public static final WeaponMaterial VOIDMETAL = material("void_metal", 150, 8.0f, 3.0f, 10);
	public static final WeaponMaterial DAWNSTONE = material("dawnstone", 644, 7.5f, 2.5f, 18);
	public static final List<WeaponMaterial> MATERIALS = List.of(MANASTEEL, TERRASTEEL, ELEMENTIUM, THAUMIUM, VOIDMETAL, DAWNSTONE);

	/** 1.12.2 "spartanweaponryarcana:material" metadata 0-4 */
	public static final Registrar.Entry<Item, Item> LIVINGWOOD_HANDLE = REGISTRY.register("livingwood_handle", () -> new Item(ItemIds.properties()));
	public static final Registrar.Entry<Item, Item> LIVINGWOOD_POLE = REGISTRY.register("livingwood_pole", () -> new Item(ItemIds.properties()));
	public static final Registrar.Entry<Item, Item> DREAMWOOD_HANDLE = REGISTRY.register("dreamwood_handle", () -> new Item(ItemIds.properties()));
	public static final Registrar.Entry<Item, Item> DREAMWOOD_POLE = REGISTRY.register("dreamwood_pole", () -> new Item(ItemIds.properties()));
	public static final Registrar.Entry<Item, Item> DREAMWOOD_CROSSBOW_LIMB = REGISTRY.register("dreamwood_crossbow_limb", () -> new Item(ItemIds.properties()));
	public static final List<Registrar.Entry<Item, Item>> CRAFTING_MATERIALS = List.of(LIVINGWOOD_HANDLE, LIVINGWOOD_POLE, DREAMWOOD_HANDLE, DREAMWOOD_POLE, DREAMWOOD_CROSSBOW_LIMB);

	/** Weapon type (Spartan Weaponry id) → API factory, in the 1.12.2 order */
	public static final Map<String, Function<WeaponMaterial, Item>> WEAPON_TYPES = new LinkedHashMap<>();
	static
	{
		WEAPON_TYPES.put("dagger", SpartanWeaponryAPI::createDagger);
		WEAPON_TYPES.put("longsword", SpartanWeaponryAPI::createLongsword);
		WEAPON_TYPES.put("katana", SpartanWeaponryAPI::createKatana);
		WEAPON_TYPES.put("saber", SpartanWeaponryAPI::createSaber);
		WEAPON_TYPES.put("rapier", SpartanWeaponryAPI::createRapier);
		WEAPON_TYPES.put("greatsword", SpartanWeaponryAPI::createGreatsword);
		WEAPON_TYPES.put("battle_hammer", SpartanWeaponryAPI::createBattleHammer);
		WEAPON_TYPES.put("warhammer", SpartanWeaponryAPI::createWarhammer);
		WEAPON_TYPES.put("spear", SpartanWeaponryAPI::createSpear);
		WEAPON_TYPES.put("halberd", SpartanWeaponryAPI::createHalberd);
		WEAPON_TYPES.put("pike", SpartanWeaponryAPI::createPike);
		WEAPON_TYPES.put("lance", SpartanWeaponryAPI::createLance);
		WEAPON_TYPES.put("longbow", SpartanWeaponryAPI::createLongbow);
		WEAPON_TYPES.put("heavy_crossbow", SpartanWeaponryAPI::createHeavyCrossbow);
		WEAPON_TYPES.put("throwing_knife", SpartanWeaponryAPI::createThrowingKnife);
		WEAPON_TYPES.put("tomahawk", SpartanWeaponryAPI::createTomahawk);
		WEAPON_TYPES.put("javelin", SpartanWeaponryAPI::createJavelin);
		WEAPON_TYPES.put("boomerang", SpartanWeaponryAPI::createBoomerang);
		WEAPON_TYPES.put("battleaxe", SpartanWeaponryAPI::createBattleaxe);
		WEAPON_TYPES.put("flanged_mace", SpartanWeaponryAPI::createFlangedMace);
		WEAPON_TYPES.put("glaive", SpartanWeaponryAPI::createGlaive);
		WEAPON_TYPES.put("quarterstaff", SpartanWeaponryAPI::createQuarterstaff);
	}

	/** Material → its weapons */
	public static final Map<WeaponMaterial, List<Registrar.Entry<Item, Item>>> WEAPONS = new LinkedHashMap<>();
	static
	{
		for(WeaponMaterial material : MATERIALS)
		{
			List<Registrar.Entry<Item, Item>> weapons = new ArrayList<>();
			WEAPON_TYPES.forEach((type, factory) -> weapons.add(REGISTRY.register(material.getMaterialName() + "_" + type, () -> factory.apply(material))));
			WEAPONS.put(material, weapons);
		}
	}

	private static Map<Item, WeaponMaterial> materialsByItem;

	/** The Arcana material of a weapon, or null */
	public static WeaponMaterial materialOf(Item item)
	{
		if(materialsByItem == null)
		{
			Map<Item, WeaponMaterial> map = new java.util.HashMap<>();
			WEAPONS.forEach((material, weapons) -> weapons.forEach((weapon) -> map.put(weapon.get(), material)));
			materialsByItem = map;
		}
		return materialsByItem.get(item);
	}

	private static WeaponMaterial material(String name, int durability, float speed, float damage, int enchantability)
	{
		TagKey<Item> ingot = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/" + name));
		TagKey<WeaponTrait> traits = TagKey.create(WeaponTraits.REGISTRY_KEY, Identifier.fromNamespaceAndPath(ModSpartanWeaponryArcana.ID, "materials/" + name));
		return new WeaponMaterial(name, ModSpartanWeaponryArcana.ID, durability, speed, damage, enchantability, ingot, traits);
	}
}
