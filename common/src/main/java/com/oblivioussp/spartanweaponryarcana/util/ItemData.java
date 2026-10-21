package com.oblivioussp.spartanweaponryarcana.util;

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

	@SuppressWarnings("deprecation")
	public static float getFloat(ItemStack stack, String key, float defaultValue)
	{
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data != null && data.contains(key) ? data.getUnsafe().getFloat(key) : defaultValue;
	}
}
