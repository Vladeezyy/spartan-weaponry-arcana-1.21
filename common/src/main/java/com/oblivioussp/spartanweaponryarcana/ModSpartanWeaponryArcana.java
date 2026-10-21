package com.oblivioussp.spartanweaponryarcana;

/**
 * Copyright (C) 2019  ObliviousSpartan
 * Port to NeoForge and Fabric 1.21.1 (C) 2026  Vladeezyy
 * 
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
*/

import com.oblivioussp.spartanweaponry.api.SpartanWeaponryAPI;
import com.oblivioussp.spartanweaponryarcana.init.ModCreativeTabs;
import com.oblivioussp.spartanweaponryarcana.init.ModItems;
import com.oblivioussp.spartanweaponryarcana.init.ModTraits;
import com.oblivioussp.spartanweaponryarcana.platform.Platform;
import com.oblivioussp.spartanweaponryarcana.util.Log;

/**
 * Spartan Weaponry: Arcana. Weapons of every Spartan Weaponry type made of Manasteel, Terrasteel, Elementium
 * (Botania), Thaumium, Void Metal (Thaumcraft) and Dawnstone (Embers). The weapons are made by Spartan Weaponry's API;
 * their recipes need the material's ingot tag (c:ingots/manasteel, ...). 1.12.2 registered the Thaumcraft and Embers
 * materials only with stats of their own; here every material works with any mod that adds its ingots.
 */
public class ModSpartanWeaponryArcana
{
	public static final String ID = "spartanweaponryarcana";
	public static final String NAME = "Spartan Weaponry: Arcana";
	/** The Spartan Weaponry API version this addon is written against */
	public static final int SPARTAN_WEAPONRY_API_VERSION = 12;

	/**
	 * The common part of the mod; the NeoForge and Fabric entry points call it. Creates the registrars (loading the
	 * classes is enough); the loader registers their entries afterwards.
	 */
	public static void init()
	{
		Log.info("Starting up Spartan Weaponry Arcana! (" + Platform.INSTANCE.name() + ")");
		SpartanWeaponryAPI.assertAPIVersion(ID, SPARTAN_WEAPONRY_API_VERSION);
		ModTraits.REGISTRY.getEntries();
		ModItems.REGISTRY.getEntries();
		ModCreativeTabs.REGISTRY.getEntries();
	}
}
