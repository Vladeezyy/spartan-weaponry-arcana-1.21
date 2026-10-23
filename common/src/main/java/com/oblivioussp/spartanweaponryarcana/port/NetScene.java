package com.oblivioussp.spartanweaponryarcana.port;

import com.oblivioussp.spartanweaponryarcana.compat.botania.BotaniaCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;
import com.oblivioussp.spartanweaponryarcana.ModSpartanWeaponryArcana;
import com.oblivioussp.spartanweaponryarcana.util.Log;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.phys.AABB;

/**
 * Multiplayer check (issue #3 of the original: swinging a Terrasteel weapon on a server kicked the player, the 1.12.2
 * channel name was too long). Inert unless the {@code spartanweaponryarcana.netscene} system property is set: the dev
 * client joins the dev server ({@code tools/nettest.sh}, the player is an operator), sets up a platform with a golem
 * through commands, swings a Terrasteel greatsword at the air and checks on the client that a mana burst flies, the
 * golem is hurt and the connection holds. Logs {@code [netscene] CHECK}, then quits.
 */
public final class NetScene
{
	private static final boolean ENABLED = System.getProperty("spartanweaponryarcana.netscene") != null;
	private static final List<Pair<Integer, Consumer<Minecraft>>> STEPS = new ArrayList<>();
	private static int tick;
	private static int failures;
	private static boolean done;

	static
	{
		at(40, mc -> {
			command(mc, "gamemode creative");
			command(mc, "fill ~-4 199 ~-4 ~4 199 ~14 minecraft:stone");
		});
		at(50, mc -> command(mc, "tp @s ~ 200 ~ 0 5"));
		at(60, mc -> {
			command(mc, "clear @s");
			command(mc, "give @s spartanweaponryarcana:terrasteel_greatsword");
			command(mc, "summon minecraft:iron_golem ~ 200 ~9 {NoAI:1b,Rotation:[180f,0f]}");
		});
		at(70, mc -> mc.player.getInventory().setSelectedSlot(0));
		// The attack strength fills up (more than a second with the item in hand)
		at(110, mc -> {
			check("holding a Terrasteel greatsword", BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem()).getPath().equals("terrasteel_greatsword"), mc.player.getMainHandItem());
			check("golem in front", golem(mc) != null, "");
			KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.getKey(mc.options.keyAttack.saveString()));
		});
		// 1.21.11 has no Botania: the swing sends no Terra Burst, the connection must hold all the same
		boolean botania = BotaniaCompat.isLoaded();
		at(113, mc -> check(botania ? "a mana burst flies (sent by the server)" : "no mana burst without Botania", (bursts(mc) > 0) == botania, bursts(mc)));
		at(140, mc -> {
			IronGolem golem = golem(mc);
			if(botania)
				check("the burst hurt the golem", golem != null && golem.getHealth() < golem.getMaxHealth(), golem == null ? "no golem" : golem.getHealth() + "/" + golem.getMaxHealth());
			check("still connected", mc.getConnection() != null && mc.level != null, "");
		});
		// Several swings in a row (the 1.12.2 kick happened on every swing)
		at(145, mc -> KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.getKey(mc.options.keyAttack.saveString())));
		at(175, mc -> KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.getKey(mc.options.keyAttack.saveString())));
		at(205, mc -> KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.getKey(mc.options.keyAttack.saveString())));
		at(230, mc -> check("still connected after four swings", mc.getConnection() != null && mc.level != null, ""));
		at(235, mc -> {
			Log.info("[netscene] DONE, failed checks: " + failures);
			done = true;
			mc.level.disconnect(net.minecraft.network.chat.Component.empty());
			mc.disconnectWithProgressScreen();
			mc.stop();
		});
	}

	private static void at(int t, Consumer<Minecraft> action)
	{
		STEPS.add(Pair.of(t, action));
	}

	private static void command(Minecraft mc, String command)
	{
		mc.player.connection.sendCommand(command);
	}

	private static void check(String name, boolean ok, Object detail)
	{
		if(!ok)
			failures++;
		Log.info("[netscene] CHECK " + (ok ? "OK   " : "FAIL ") + name + " (" + detail + ")");
	}

	private static IronGolem golem(Minecraft mc)
	{
		List<IronGolem> golems = mc.level.getEntitiesOfClass(IronGolem.class, new AABB(mc.player.blockPosition()).inflate(20));
		return golems.isEmpty() ? null : golems.get(0);
	}

	private static int bursts(Minecraft mc)
	{
		int count = 0;
		for(Entity entity : mc.level.entitiesForRendering())
			if(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().equals("botania:mana_burst"))
				count++;
		return count;
	}

	// The loader modules call this after every client tick (port/*SceneHooks)
	public static void onTick()
	{
		if(!ENABLED || done)
			return;
		Minecraft mc = Minecraft.getInstance();
		if(mc.gui.screen() instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)
		{
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.gui.setScreen(new TitleScreen());
			return;
		}
		if(mc.player == null || mc.level == null)
			return;
		mc.options.pauseOnLostFocus = false;
		if(mc.gui.screen() instanceof PauseScreen)
			mc.gui.setScreen(null);
		tick++;
		for(Pair<Integer, Consumer<Minecraft>> step : STEPS)
			if(step.getFirst() == tick)
			{
				try
				{
					step.getSecond().accept(mc);
				}
				catch(RuntimeException e)
				{
					check("step at tick " + tick + " ran", false, e);
				}
			}
	}
}
