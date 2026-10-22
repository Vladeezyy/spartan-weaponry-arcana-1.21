package com.oblivioussp.spartanweaponryarcana.util;

import com.oblivioussp.spartanweaponry.util.ItemNbt;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Small values on an item (minecraft:custom_data) */
public class ItemData
{
	public static void setFloat(ItemStack stack, String key, float value)
	{
		CustomData.update(DataComponents.CUSTOM_DATA, stack, (tag) -> tag.putFloat(key, value));
	}

	/** Without a copy of the item data (Spartan Weaponry's ItemNbt; 1.21.11 has no CustomData#getUnsafe) */
	public static float getFloat(ItemStack stack, String key, float defaultValue)
	{
		return ItemNbt.read(stack).getFloatOr(key, defaultValue);
	}
}
