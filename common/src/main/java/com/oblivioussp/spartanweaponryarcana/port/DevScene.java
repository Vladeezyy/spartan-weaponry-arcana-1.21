package com.oblivioussp.spartanweaponryarcana.port;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


import com.mojang.datafixers.util.Pair;
import com.oblivioussp.spartanweaponry.api.WeaponMaterial;
import com.oblivioussp.spartanweaponryarcana.ModSpartanWeaponryArcana;
import com.oblivioussp.spartanweaponryarcana.compat.botania.BotaniaCompat;
import com.oblivioussp.spartanweaponryarcana.init.ModCreativeTabs;
import com.oblivioussp.spartanweaponryarcana.init.ModItems;
import com.oblivioussp.spartanweaponryarcana.util.Log;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelResource;

/**
 * Development scene, inert unless the {@code spartanweaponryarcana.scene} system property is set
 * ({@code ./gradlew runScene}): creates the world {@code swa_scene}, shows every
 * item (item frame walls, creative tab, tooltips, first person), runs functional checks and logs them as
 * {@code [scene] CHECK}, saves {@code run/screenshots/scene_*.png}, then quits. 1.21.11 has no Botania (1.21.1: BotaniaScene
 * checked mana repair, Terra Burst, Pixielated and the lexicon). {@code -Dspartanweaponryarcana.scene.lang=ru_ru} switches
 * the language.
 */
public final class DevScene
{
	private static final boolean ENABLED = System.getProperty("spartanweaponryarcana.scene") != null;
	private static final String LANG = System.getProperty("spartanweaponryarcana.scene.lang", "en_us");
	private static final String WORLD = "swa_scene";
	private static final List<Pair<Integer, Consumer<Minecraft>>> STEPS = new ArrayList<>();
	private static boolean opened;
	private static int tick;
	private static int failures;
	static BlockPos stage;
	private static int warpedDamage;

	static
	{
		boolean botania = BotaniaCompat.isLoaded();
		at(40, mc -> server(mc, DevScene::setup));
		at(45, mc -> setHudHidden(mc, true));
		at(60, mc -> server(mc, DevScene::buildStage));
		at(160, mc -> server(mc, p -> look(p, wall(0))));
		at(220, mc -> shot(mc, "wall_botania"));
		at(225, mc -> server(mc, p -> look(p, wall(1))));
		at(285, mc -> shot(mc, "wall_thaumcraft_embers"));
		// Creative tab and tooltips
		at(290, mc -> {
			setHudHidden(mc, false);
			mc.gui.toastManager().clear();
			mc.gui.setScreen(new CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), true));
		});
		at(300, mc -> selectTab(mc, ModCreativeTabs.ARCANA.get()));
		at(320, mc -> shot(mc, "creative_tab"));
		at(325, mc -> mc.gui.setScreen(new TooltipScreen(List.of(weapon(ModItems.TERRASTEEL, "greatsword"), weapon(ModItems.ELEMENTIUM, "katana")))));
		at(332, mc -> shot(mc, "tooltip"));
		at(335, mc -> mc.gui.setScreen(new TooltipScreen(List.of(weapon(ModItems.MANASTEEL, "heavy_crossbow"), weapon(ModItems.VOIDMETAL, "longsword")))));
		at(342, mc -> shot(mc, "tooltip_2"));
		at(345, mc -> { mc.gui.setScreen(null); setHudHidden(mc, true); });
		// First person
		at(350, mc -> server(mc, DevScene::firstPerson));
		at(375, mc -> setHudHidden(mc, false));
		at(380, mc -> shot(mc, "first_person"));
		at(382, mc -> setHudHidden(mc, true));
		// Server side checks
		at(385, mc -> server(mc, DevScene::checkRegistry));
		at(390, mc -> server(mc, DevScene::checkRecipes));
		at(395, mc -> server(mc, DevScene::checkTab));
		at(400, mc -> server(mc, DevScene::checkStats));
		at(405, mc -> server(mc, DevScene::checkEnchanting));
		// Warped (Void Metal): one durability back per second
		at(410, mc -> server(mc, DevScene::prepareWarped));
		at(470, mc -> server(mc, DevScene::checkWarped));
		// A data pack adds an ingot to c:ingots/thaumium: the Thaumium recipes and tab entries appear (issue #5)
		at(475, mc -> server(mc, DevScene::thaumiumDataPack));
		at(560, mc -> server(mc, DevScene::checkThaumiumDataPack));
		int end = 565;
		at(end, mc -> {
			Log.info("[scene] DONE, failed checks: " + failures);
			mc.stop();
		});
	}

	static void at(int t, Consumer<Minecraft> action)
	{
		STEPS.add(Pair.of(t, action));
	}

	// The loader modules call this after every client tick (port/*SceneHooks)
	public static void onTick()
	{
		if(!ENABLED)
			return;
		Minecraft mc = Minecraft.getInstance();
		if(!opened && mc.gui.screen() instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen)
		{
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.gui.setScreen(new TitleScreen());
			return;
		}
		// The language switch reloads the resources; the world is created after that, not during it
		if(!opened && mc.gui.overlay() != null)
			return;
		// NeoForge 26.2: load warnings (e.g. another mod's deprecated logoFile) open LoadingErrorScreen before the menu
		if(!opened && mc.gui.screen() != null && mc.gui.screen().getClass().getSimpleName().equals("LoadingErrorScreen"))
		{
			Log.info("[scene] NeoForge load warnings screen, going on to the main menu");
			mc.gui.setScreen(new TitleScreen());
			return;
		}
		if(!opened && mc.gui.screen() instanceof TitleScreen)
		{
			if(!mc.options.languageCode.equals(LANG))
			{
				mc.options.languageCode = LANG;
				mc.getLanguageManager().setSelected(LANG);
				mc.options.save();
				mc.reloadResourcePacks();
				return;
			}
			opened = true;
			mc.getWindow().setWindowed(1600, 900);
			LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, new LevelSettings.DifficultySettings(Difficulty.NORMAL, false, false), true, WorldDataConfiguration.DEFAULT);
			mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(1234L, false, false), WorldPresets::createNormalWorldDimensions, new TitleScreen());
			return;
		}
		if(mc.player == null || mc.getSingleplayerServer() == null)
			return;
		mc.options.pauseOnLostFocus = false;
		mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
		mc.gui.toastManager().clear();
		if(mc.gui.screen() instanceof PauseScreen)
			mc.gui.setScreen(null);
		tick++;
		for(Pair<Integer, Consumer<Minecraft>> step : STEPS)
		{
			if(step.getFirst() == tick)
			{
				Log.info("[scene] tick " + tick);
				try
				{
					step.getSecond().accept(mc);
				}
				catch(RuntimeException | LinkageError e)
				{
					check("step at tick " + tick + " ran", false, e);
					Log.error(e);
				}
			}
		}
	}

	static void server(Minecraft mc, Consumer<ServerPlayer> action)
	{
		MinecraftServer server = mc.getSingleplayerServer();
		server.execute(() ->
		{
			try
			{
				action.accept(server.getPlayerList().getPlayer(mc.player.getUUID()));
			}
			catch(RuntimeException | LinkageError e)
			{
				check("server step ran", false, e);
				Log.error(e);
			}
		});
	}

	static void check(String name, boolean ok, Object detail)
	{
		if(!ok)
			failures++;
		Log.info("[scene] CHECK " + (ok ? "OK   " : "FAIL ") + name + " (" + detail + ")");
	}

	static void shot(Minecraft mc, String name)
	{
		Screenshot.grab(mc.gameDirectory, "scene_" + name + ".png", mc.gameRenderer.mainRenderTarget(), 1, c -> Log.info("[scene] " + c.getString()));
	}

	private static void selectTab(Minecraft mc, CreativeModeTab tab)
	{
		if(mc.gui.screen() instanceof CreativeModeInventoryScreen screen)
		{
			try
			{
				// Fabric API pages the tabs: a tab on another page is not selected without switching to its page first
				// (FabricCreativeInventoryScreen, by reflection: the common code does not see Fabric API)
				try
				{
					int page = (int)screen.getClass().getMethod("getPage", CreativeModeTab.class).invoke(screen, tab);
					screen.getClass().getMethod("switchToPage", int.class).invoke(screen, page);
				}
				catch(NoSuchMethodException e)
				{
					// NeoForge: its own pages follow the selected tab
				}
				// The private selectTab(CreativeModeTab), found by its signature: its name differs on a production (intermediary) client
				var select = java.util.Arrays.stream(CreativeModeInventoryScreen.class.getDeclaredMethods()).filter(m -> m.getReturnType() == void.class && java.util.Arrays.equals(m.getParameterTypes(), new Class<?>[] { net.minecraft.world.item.CreativeModeTab.class })).findFirst().orElseThrow(NoSuchMethodException::new);
				select.setAccessible(true);
				select.invoke(screen, tab);
			}
			catch(ReflectiveOperationException e)
			{
				Log.error("[scene] cannot select the creative tab: " + e);
			}
		}
	}

	static ItemStack weapon(WeaponMaterial material, String type)
	{
		return new ItemStack(BuiltInRegistries.ITEM.getValue(id(material.getMaterialName() + "_" + type)));
	}

	/** 1.21.2+: a recipe is looked up by its key */
	static net.minecraft.resources.ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> recipe(String path)
	{
		return net.minecraft.resources.ResourceKey.create(Registries.RECIPE, id(path));
	}

	static Identifier id(String path)
	{
		return Identifier.fromNamespaceAndPath(ModSpartanWeaponryArcana.ID, path);
	}

	// ------------------------------------------------------------------------------------------------------ stage

	private static void setup(ServerPlayer player)
	{
		MinecraftServer server = player.level().getServer();
		player.setGameMode(GameType.CREATIVE);
		server.setDifficulty(Difficulty.NORMAL, true);
		server.overworld().getGameRules().set(GameRules.SPAWN_MOBS, false, server);
		server.overworld().getGameRules().set(GameRules.ADVANCE_TIME, false, server);
		server.overworld().getGameRules().set(GameRules.ADVANCE_WEATHER, false, server);
		server.overworld().getGameRules().set(GameRules.MOB_GRIEFING, false, server);
		player.level().getServer().getCommands().performPrefixedCommand(player.level().getServer().createCommandSourceStack().withSuppressedOutput(), "time set 6000");
		player.level().getServer().getCommands().performPrefixedCommand(player.level().getServer().createCommandSourceStack().withSuppressedOutput(), "weather clear");
	}

	/** Item frame wall n (0 Botania materials and parts, 1 Thaumcraft and Embers materials): its centre */
	private static BlockPos wall(int n)
	{
		return stage.offset(-30 + n * 60, 0, 0);
	}

	private static void look(ServerPlayer player, BlockPos wall)
	{
		player.teleportTo(player.level(), wall.getX() + 0.5, wall.getY() + 2, wall.getZ() - 13.5, java.util.Set.of(), 0.0F, 0.0F, true);
	}

	private static void buildStage(ServerPlayer player)
	{
		ServerLevel level = player.level();
		BlockPos spawn = level.getRespawnData().pos();
		int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
		// In the air, above the terrain (no overhang shading the walls)
		stage = new BlockPos(spawn.getX(), Math.max(y + 40, 140), spawn.getZ());
		for(int dx = -60; dx <= 60; dx++)
			for(int dz = -20; dz <= 30; dz++)
			{
				level.setBlockAndUpdate(stage.offset(dx, -1, dz), Blocks.POLISHED_ANDESITE.defaultBlockState());
				for(int dy = 0; dy < 12; dy++)
					level.setBlockAndUpdate(stage.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
			}
		List<ItemStack> botania = new ArrayList<>();
		ModItems.CRAFTING_MATERIALS.forEach(i -> botania.add(new ItemStack(i.get())));
		for(WeaponMaterial material : List.of(ModItems.MANASTEEL, ModItems.TERRASTEEL, ModItems.ELEMENTIUM))
			ModItems.WEAPONS.get(material).forEach(i -> botania.add(new ItemStack(i.get())));
		List<ItemStack> others = new ArrayList<>();
		for(WeaponMaterial material : List.of(ModItems.THAUMIUM, ModItems.VOIDMETAL, ModItems.DAWNSTONE))
			ModItems.WEAPONS.get(material).forEach(i -> others.add(new ItemStack(i.get())));
		// One row per material (the parts before the first one)
		frames(level, wall(0), botania, 22, 5);
		frames(level, wall(1), others, 22, 0);
		player.teleportTo(level, stage.getX() + 0.5, stage.getY(), stage.getZ() + 0.5, java.util.Set.of(), 0.0F, 0.0F, true);
	}

	private static void frames(ServerLevel level, BlockPos centre, List<ItemStack> items, int columns, int firstRow)
	{
		int rows = (items.size() - firstRow + columns - 1) / columns + (firstRow > 0 ? 1 : 0);
		for(int i = 0; i < items.size(); i++)
		{
			int col, line;
			if(i < firstRow)
			{
				col = (columns - firstRow) / 2 + i;
				line = 0;
			}
			else
			{
				col = (i - firstRow) % columns;
				line = (i - firstRow) / columns + (firstRow > 0 ? 1 : 0);
			}
			BlockPos framePos = centre.offset(columns / 2 - col, rows - line, 0);
			level.setBlockAndUpdate(framePos.south(), Blocks.SPRUCE_PLANKS.defaultBlockState());
			ItemFrame frame = new ItemFrame(level, framePos, Direction.NORTH);
			frame.setItem(items.get(i), false);
			level.addFreshEntity(frame);
		}
	}

	private static void firstPerson(ServerPlayer player)
	{
		player.setItemSlot(EquipmentSlot.MAINHAND, weapon(ModItems.TERRASTEEL, "greatsword"));
		player.setItemSlot(EquipmentSlot.OFFHAND, weapon(ModItems.ELEMENTIUM, "dagger"));
		player.teleportTo(player.level(), stage.getX() + 0.5, stage.getY(), stage.getZ() + 5.5, java.util.Set.of(), 0.0F, 10.0F, true);
	}

	// ------------------------------------------------------------------------------------------------------ checks

	private static void checkRegistry(ServerPlayer player)
	{
		long items = BuiltInRegistries.ITEM.keySet().stream().filter(k -> k.getNamespace().equals(ModSpartanWeaponryArcana.ID)).count();
		long expected = 6 * 22 + 5 + (BotaniaCompat.isLoaded() ? 1 : 0);
		check("items: 132 weapons, 5 parts" + (BotaniaCompat.isLoaded() ? ", the Terra Burst lens" : ""), items == expected, items + " of " + expected);
		long traits = player.level().registryAccess().lookupOrThrow(com.oblivioussp.spartanweaponry.api.WeaponTraits.REGISTRY_KEY).keySet().stream()
				.filter(k -> k.getNamespace().equals(ModSpartanWeaponryArcana.ID)).count();
		check("traits: 5", traits == 5, traits);
		// Every weapon has a model (no missing texture), checked on the client side by the item frame screenshots;
		// here: every weapon has a name
		long unnamed = ModItems.WEAPONS.values().stream().flatMap(List::stream).filter(i -> !net.minecraft.locale.Language.getInstance().has(i.get().getDescriptionId())).count();
		check("every weapon has an English name", unnamed == 0, unnamed + " without");
	}

	private static void checkRecipes(ServerPlayer player)
	{
		var recipes = player.level().recipeAccess();
		boolean botania = BotaniaCompat.isLoaded();
		long ours = recipes.getRecipes().stream().filter(r -> r.id().identifier().getNamespace().equals(ModSpartanWeaponryArcana.ID)).count();
		// Botania: 3 materials × 22 weapons + handles and poles (from string and from Mana String) + crossbow limb
		check("recipes loaded: " + (botania ? "the Botania ones" : "none (no material)"), botania ? ours >= 66 + 5 : ours == 0, ours + " recipes");
		check("Manasteel recipes " + (botania ? "present" : "skipped"), recipes.byKey(recipe("manasteel_longsword")).isPresent() == botania, "");
		check("Livingwood handle recipe " + (botania ? "present" : "skipped"), recipes.byKey(recipe("livingwood_handle_from_string")).isPresent() == botania, "");
		check("Thaumium recipes skipped (no c:ingots/thaumium)", recipes.byKey(recipe("thaumium_longsword")).isEmpty(), "");
		check("Dawnstone recipes skipped (no c:ingots/dawnstone)", recipes.byKey(recipe("dawnstone_longsword")).isEmpty(), "");
	}

	private static List<ItemStack> tabItems(ServerLevel level)
	{
		CreativeModeTab tab = ModCreativeTabs.ARCANA.get();
		tab.buildContents(new CreativeModeTab.ItemDisplayParameters(level.enabledFeatures(), false, level.registryAccess()));
		return new ArrayList<>(tab.getDisplayItems());
	}

	private static boolean tabHas(List<ItemStack> tab, WeaponMaterial material)
	{
		return tab.stream().anyMatch(s -> BuiltInRegistries.ITEM.getKey(s.getItem()).getPath().startsWith(material.getMaterialName() + "_"));
	}

	private static void checkTab(ServerPlayer player)
	{
		List<ItemStack> tab = tabItems(player.level());
		boolean botania = BotaniaCompat.isLoaded();
		check("tab: " + (botania ? "Botania weapons and parts" : "empty without the materials (issue #5)"), botania ? tab.size() == 66 + 5 : tab.isEmpty(), tab.size() + " items");
		check("tab: no Thaumium / Void Metal / Dawnstone weapons", !tabHas(tab, ModItems.THAUMIUM) && !tabHas(tab, ModItems.VOIDMETAL) && !tabHas(tab, ModItems.DAWNSTONE), "");
	}

	private static void checkStats(ServerPlayer player)
	{
		// Attack damage: the type's base + material damage × the type's multiplier (greatsword 1.5)
		ItemStack terrasteel = weapon(ModItems.TERRASTEEL, "greatsword");
		ItemStack iron = new ItemStack(com.oblivioussp.spartanweaponry.init.ModItems.GREATSWORDS.iron.get());
		double terraDamage = attackDamage(terrasteel), ironDamage = attackDamage(iron);
		check("Terrasteel greatsword: 1.5 × the material damage difference to iron (3.0 vs 2.0)", Math.abs(terraDamage - ironDamage - 1.5) < 0.01, terraDamage + " vs " + ironDamage);
		check("durability: Terrasteel 2300, Void Metal 150", terrasteel.getMaxDamage() == 2300 && weapon(ModItems.VOIDMETAL, "longsword").getMaxDamage() == 150,
				terrasteel.getMaxDamage() + ", " + weapon(ModItems.VOIDMETAL, "longsword").getMaxDamage());
		check("Manasteel repairs with c:ingots/manasteel", ModItems.MANASTEEL.getRepairTag().location().toString().equals("c:ingots/manasteel"), ModItems.MANASTEEL.getRepairTag());
	}

	private static double attackDamage(ItemStack stack)
	{
		double[] value = { 1.0 };
		stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
			if(attribute.is(Attributes.ATTACK_DAMAGE))
				value[0] += modifier.amount();
		});
		return value[0];
	}

	private static void checkEnchanting(ServerPlayer player)
	{
		var possible = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.IN_ENCHANTING_TABLE).orElseThrow();
		List<String> sword = offered(weapon(ModItems.MANASTEEL, "longsword"), possible);
		check("enchanting table: Manasteel longsword", sword.contains("minecraft:sharpness") && sword.contains("minecraft:unbreaking"), sword);
		List<String> bow = offered(weapon(ModItems.TERRASTEEL, "longbow"), possible);
		check("enchanting table: Terrasteel longbow", bow.contains("minecraft:power"), bow);
		List<String> crossbow = offered(weapon(ModItems.ELEMENTIUM, "heavy_crossbow"), possible);
		check("enchanting table: Elementium heavy crossbow", crossbow.contains("spartanweaponry:sharpshooter"), crossbow);
		List<String> knife = offered(weapon(ModItems.DAWNSTONE, "throwing_knife"), possible);
		check("enchanting table: Dawnstone throwing knife", knife.contains("spartanweaponry:propel"), knife);
	}

	private static List<String> offered(ItemStack stack, net.minecraft.core.HolderSet.Named<net.minecraft.world.item.enchantment.Enchantment> possible)
	{
		List<EnchantmentInstance> available = EnchantmentHelper.getAvailableEnchantmentResults(30, stack, possible.stream());
		return available.stream().map(e -> e.enchantment().unwrapKey().get().identifier().toString()).distinct().sorted().toList();
	}

	private static void prepareWarped(ServerPlayer player)
	{
		player.getInventory().clearContent();
		ItemStack voidSword = weapon(ModItems.VOIDMETAL, "longsword");
		voidSword.setDamageValue(50);
		player.getInventory().setItem(5, voidSword);
		// A damaged Manasteel weapon without mana: no repair
		ItemStack manasteel = weapon(ModItems.MANASTEEL, "longsword");
		manasteel.setDamageValue(50);
		player.getInventory().setItem(6, manasteel);
		warpedDamage = 50;
	}

	private static void checkWarped(ServerPlayer player)
	{
		int damage = player.getInventory().getItem(5).getDamageValue();
		// 60 ticks: three repairs (2 to 4 with the timing)
		check("Warped: Void Metal repairs about one durability per second", warpedDamage - damage >= 2 && warpedDamage - damage <= 4, warpedDamage + " → " + damage);
		int manasteel = player.getInventory().getItem(6).getDamageValue();
		check("Mana Repair without a mana item: no repair", manasteel == 50, manasteel);
		player.getInventory().clearContent();
	}

	private static void thaumiumDataPack(ServerPlayer player)
	{
		MinecraftServer server = player.level().getServer();
		Path pack = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve("thaumium_test");
		try
		{
			Files.createDirectories(pack.resolve("data/c/tags/item/ingots"));
			Files.writeString(pack.resolve("pack.mcmeta"), "{\"pack\":{\"pack_format\":48,\"description\":\"scene: copper as thaumium\"}}");
			Files.writeString(pack.resolve("data/c/tags/item/ingots/thaumium.json"), "{\"values\":[\"minecraft:copper_ingot\"]}");
		}
		catch(IOException e)
		{
			check("test data pack written", false, e);
			return;
		}
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "reload");
	}

	private static void checkThaumiumDataPack(ServerPlayer player)
	{
		var recipes = player.level().recipeAccess();
		var longsword = recipes.byKey(recipe("thaumium_longsword"));
		check("data pack: Thaumium recipes appear with an ingot in c:ingots/thaumium", longsword.isPresent()
				&& longsword.get().value().placementInfo().ingredients().stream().anyMatch(i -> i.test(new ItemStack(Items.COPPER_INGOT))), longsword.map(r -> r.id()));
		List<ItemStack> tab = tabItems(player.level());
		check("data pack: Thaumium weapons in the tab", tabHas(tab, ModItems.THAUMIUM) && !tabHas(tab, ModItems.DAWNSTONE), tab.size() + " items");
		ItemStack sword = weapon(ModItems.THAUMIUM, "longsword");
		check("data pack: Thaumium repairs with the ingot", sword.has(net.minecraft.core.component.DataComponents.REPAIRABLE) && sword.get(net.minecraft.core.component.DataComponents.REPAIRABLE).isValidRepairItem(new ItemStack(Items.COPPER_INGOT)), "");
	}

	/** Shows the tooltips of some items side by side */
	static class TooltipScreen extends Screen
	{
		private final List<ItemStack> stacks;

		TooltipScreen(List<ItemStack> stacks)
		{
			super(Component.empty());
			this.stacks = stacks;
		}

		@Override
		public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
		{
			graphics.fill(0, 0, width, height, 0xFF2B2B33);
			for(int i = 0; i < stacks.size(); i++)
			{
				ItemStack stack = stacks.get(i);
				List<Component> lines = Screen.getTooltipFromItem(Minecraft.getInstance(), stack);
				// 1.21.6+: one tooltip per frame is deferred; these are drawn at once, as vanilla's tooltip
				List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> components = new java.util.ArrayList<>();
				lines.forEach(line -> components.add(net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent.create(line.getVisualOrderText())));
				graphics.tooltip(font, components, i * width / stacks.size() + 4, 16, net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE, null, false);
				for(Component line : lines)
					if(line.getString().contains(ModSpartanWeaponryArcana.ID + "."))
						Log.info("[scene] CHECK FAIL untranslated tooltip line (" + line.getString() + ")");
			}
		}
	}

	static Item item(String id)
	{
		return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
	}

	/** 26.2: the HUD is hidden through Minecraft.gui.hud, which only toggles */
	private static void setHudHidden(net.minecraft.client.Minecraft mc, boolean hidden)
	{
		if(mc.gui.hud.isHidden() != hidden)
			mc.gui.hud.toggle();
	}
}
