package com.oblivioussp.spartanweaponryarcana.port;

import com.oblivioussp.spartanweaponryarcana.compat.botania.BotaniaCompat;

import static com.oblivioussp.spartanweaponryarcana.port.DevScene.at;
import static com.oblivioussp.spartanweaponryarcana.port.DevScene.check;
import static com.oblivioussp.spartanweaponryarcana.port.DevScene.server;
import static com.oblivioussp.spartanweaponryarcana.port.DevScene.shot;
import static com.oblivioussp.spartanweaponryarcana.port.DevScene.weapon;

import com.oblivioussp.spartanweaponryarcana.init.ModItems;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.patchouli.api.PatchouliAPI;

/** The Botania part of {@link DevScene}: mana repair, Pixielated, Terra Burst (real input), the lexicon entries */
final class BotaniaScene
{
	private static final ResourceLocation BOOK = ResourceLocation.fromNamespaceAndPath("botania", "lexica_botania");
	private static final String[] ENTRIES = { "mana_weaponry", "terra_weaponry", "elementium_weaponry" };
	private static int manaBefore;
	private static int golemId;
	private static float golemHealth;

	static int steps(int t)
	{
		at(t, mc -> server(mc, BotaniaScene::prepareManaRepair));
		at(t + 40, mc -> server(mc, BotaniaScene::checkManaRepair));
		at(t + 45, mc -> server(mc, p -> p.setItemSlot(EquipmentSlot.MAINHAND, weapon(ModItems.ELEMENTIUM, "katana"))));
		at(t + 50, mc -> server(mc, BotaniaScene::checkPixie));
		// Terra Burst: a swing at the air with a Terrasteel weapon (real input), the burst hits a golem 10 blocks away
		at(t + 52, mc -> server(mc, p -> p.setItemSlot(EquipmentSlot.MAINHAND, weapon(ModItems.TERRASTEEL, "greatsword"))));
		at(t + 54, mc -> server(mc, p -> check("Pixielated: none with a Terrasteel weapon", pixieChance(p) == 0.0, pixieChance(p))));
		at(t + 55, mc -> server(mc, BotaniaScene::prepareTerraBurst));
		at(t + 85, mc -> { mc.options.hideGui = false; KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.getKey(mc.options.keyAttack.saveString())); });
		at(t + 86, mc -> shot(mc, "terra_burst"));
		at(t + 88, mc -> { server(mc, BotaniaScene::checkBurstFlying); });
		at(t + 110, mc -> server(mc, BotaniaScene::checkTerraBurst));
		// Two swings one tick apart: the second one is not at full attack strength, one burst only
		at(t + 112, mc -> server(mc, BotaniaScene::countBursts));
		at(t + 113, mc -> KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.getKey(mc.options.keyAttack.saveString())));
		at(t + 114, mc -> KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.getKey(mc.options.keyAttack.saveString())));
		at(t + 117, mc -> server(mc, p -> {
			check("Terra Burst: a second swing right after the first sends none (attack strength)", bursts(p) == 1, bursts(p));
			p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
		}));
		// Lexicon entries (category Tools)
		at(t + 120, mc -> server(mc, BotaniaScene::unlockLexicon));
		int s = t + 130;
		for(String entry : ENTRIES)
		{
			at(s, mc -> PatchouliAPI.get().openBookEntry(BOOK, ResourceLocation.fromNamespaceAndPath("botania", "tools/spartanweaponryarcana_" + entry), 0));
			at(s + 20, mc -> {
				check("lexicon entry " + entry + " opens", mc.screen != null && mc.screen.getClass().getSimpleName().equals("GuiBookEntry"), mc.screen == null ? "no screen" : mc.screen.getClass().getSimpleName());
				shot(mc, "lexicon_" + entry);
			});
			s += 25;
		}
		at(s, mc -> PatchouliAPI.get().openBookEntry(BOOK, ResourceLocation.fromNamespaceAndPath("botania", "tools/spartanweaponryarcana_terra_weaponry"), 2));
		at(s + 20, mc -> shot(mc, "lexicon_terra_weaponry_recipes"));
		at(s + 25, mc -> { mc.setScreen(null); mc.options.hideGui = true; });
		return s + 30;
	}

	private static ItemStack tablet(int mana)
	{
		ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
		ManaItem item = ManaItem.LOOKUP.find(tablet);
		item.addMana(mana);
		return tablet;
	}

	private static int mana(ItemStack tablet)
	{
		return ManaItem.LOOKUP.find(tablet).getMana();
	}

	private static long manaRepairStart;

	private static void prepareManaRepair(ServerPlayer player)
	{
		player.getInventory().clearContent();
		ItemStack manasteel = weapon(ModItems.MANASTEEL, "longsword");
		manasteel.setDamageValue(50);
		ItemStack terrasteel = weapon(ModItems.TERRASTEEL, "longbow");
		terrasteel.setDamageValue(50);
		player.getInventory().setItem(5, manasteel);
		player.getInventory().setItem(6, terrasteel);
		player.getInventory().setItem(7, tablet(100000));
		manaBefore = mana(player.getInventory().getItem(7));
		manaRepairStart = player.level().getGameTime();
	}

	private static void checkManaRepair(ServerPlayer player)
	{
		int manasteel = player.getInventory().getItem(5).getDamageValue();
		int terrasteel = player.getInventory().getItem(6).getDamageValue();
		int mana = mana(player.getInventory().getItem(7));
		// One durability per server tick (as 1.12.2) while the mana lasts. Counted in server ticks: a client under load
		// (shaders) runs its 40 ticks while the integrated server runs fewer
		long ticks = player.level().getGameTime() - manaRepairStart;
		int expected = (int)Math.min(50, Math.max(0, ticks - 5));
		check("Mana Repair: Manasteel repairs with mana from a tablet", ticks > 10 && 50 - manasteel >= expected, "50 → " + manasteel + " in " + ticks + " server ticks");
		check("Mana Repair: Terrasteel longbow (a ranged weapon) repairs too", ticks > 10 && 50 - terrasteel >= expected, "50 → " + terrasteel + " in " + ticks + " server ticks");
		check("Mana Repair: the tablet loses mana", mana < manaBefore, manaBefore + " → " + mana);
		player.getInventory().clearContent();
	}

	private static double pixieChance(ServerPlayer player)
	{
		var attribute = BotaniaCompat.pixieSpawnChance();
		return player.getAttributes().hasAttribute(attribute) ? player.getAttributeValue(attribute) : -1;
	}

	private static void checkPixie(ServerPlayer player)
	{
		double chance = pixieChance(player);
		check("Pixielated: an Elementium weapon gives a 5% pixie chance (Botania attribute)", Math.abs(chance - 0.05) < 0.001, chance);
	}

	private static void prepareTerraBurst(ServerPlayer player)
	{
		player.getInventory().clearContent();
		// Survival: creative players take no durability damage
		player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
		player.setItemSlot(EquipmentSlot.MAINHAND, weapon(ModItems.TERRASTEEL, "greatsword"));
		var stage = DevScene.stage.offset(0, 0, 14);
		player.teleportTo(player.serverLevel(), stage.getX() + 0.5, stage.getY(), stage.getZ() + 0.5, 0.0F, 5.0F);
		IronGolem golem = EntityType.IRON_GOLEM.spawn(player.serverLevel(), stage.offset(0, 0, 9), MobSpawnType.COMMAND);
		golem.setNoAi(true);
		golem.setYRot(180.0F);
		golemId = golem.getId();
		golemHealth = golem.getHealth();
	}

	private static int bursts(ServerPlayer player)
	{
		return player.serverLevel().getEntitiesOfClass(ManaBurstEntity.class, new AABB(player.blockPosition()).inflate(40)).size();
	}

	private static void checkBurstFlying(ServerPlayer player)
	{
		check("Terra Burst: a swing at the air sends a mana burst", bursts(player) > 0, bursts(player) + " bursts");
	}

	private static void checkTerraBurst(ServerPlayer player)
	{
		IronGolem golem = (IronGolem)player.serverLevel().getEntity(golemId);
		float dealt = golemHealth - golem.getHealth();
		double attack = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
		check("Terra Burst: hits the golem 9 blocks away", dealt > 0.0F, dealt + " (attack damage " + attack + ")");
		int damage = player.getMainHandItem().getDamageValue();
		check("Terra Burst: costs one durability without mana", damage == 1, damage);
		golem.discard();
	}

	private static void countBursts(ServerPlayer player)
	{
		player.serverLevel().getEntitiesOfClass(ManaBurstEntity.class, new AABB(player.blockPosition()).inflate(40)).forEach(b -> b.discard());
	}

	private static void unlockLexicon(ServerPlayer player)
	{
		var advancements = player.getServer().getAdvancements();
		for(String id : new String[] { "main/mana_pool_pickup_lexicon", "main/terrasteel_pickup_lexicon", "main/elf_lexicon_pickup" })
		{
			var holder = advancements.get(ResourceLocation.fromNamespaceAndPath("botania", id));
			if(holder == null)
			{
				check("lexicon advancement botania:" + id + " exists", false, "");
				continue;
			}
			for(String criterion : player.getAdvancements().getOrStartProgress(holder).getRemainingCriteria())
				player.getAdvancements().award(holder, criterion);
		}
	}
}
