package com.oblivioussp.spartanweaponryarcana.init;

import com.oblivioussp.spartanweaponry.api.WeaponMaterial;
import com.oblivioussp.spartanweaponry.item.ThrowingWeaponItem;
import com.oblivioussp.spartanweaponryarcana.ModSpartanWeaponryArcana;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Port fix (issue #5 of the original, "embers weapons are loaded regardless of mod loaded"): the tab, and so JEI and
 * EMI, only lists the weapons of materials some mod provides ingots for, and the Livingwood / Dreamwood parts only with
 * Botania's twigs. The items exist either way (worlds keep them when a mod is removed).
 */
public class ModCreativeTabs
{
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModSpartanWeaponryArcana.ID);

	private static final TagKey<Item> LIVINGWOOD_TWIGS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "rods/livingwood"));

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ARCANA = REGISTRY.register("arcana", () -> 
		CreativeModeTab.builder()
			.title(Component.translatable("itemGroup." + ModSpartanWeaponryArcana.ID + ".arcana"))
			.icon(() -> new ItemStack(ModItems.WEAPONS.get(ModItems.TERRASTEEL).get(5).get()))
			.displayItems((params, output) -> 
			{
				if(hasItems(LIVINGWOOD_TWIGS))
					ModItems.CRAFTING_MATERIALS.forEach((item) -> output.accept(item.get()));
				for(WeaponMaterial material : ModItems.MATERIALS)
				{
					if(!hasItems(material.getRepairTag()))
						continue;
					for(DeferredItem<Item> weapon : ModItems.WEAPONS.get(material))
						output.accept(weapon.get() instanceof ThrowingWeaponItem throwing ? throwing.makeTabStack() : new ItemStack(weapon.get()));
				}
			})
			.build());

	public static boolean hasItems(TagKey<Item> tag)
	{
		return BuiltInRegistries.ITEM.getTag(tag).map((set) -> set.size() > 0).orElse(false);
	}
}
