# Spartan Weaponry: Arcana — port log

Port of **Spartan Weaponry: Arcana beta-1.0.3** (ObliviousSpartan, GPL-3.0-or-later) from Minecraft 1.12.2 (Forge 14)
to **Minecraft 1.21.1, NeoForge 21.1** (built with 21.1.262, `neoforge` range `[21.1.197,)`), as an addon of the
**Spartan Weaponry 3.2.1 port** ([Vladeezyy/spartan-weaponry-1.21](https://github.com/Vladeezyy/spartan-weaponry-1.21),
`spartanweaponry` range `[3.2.1,)`, API version 12). Mod id unchanged.

License: the original is GPL-3.0-or-later, so the port stays GPL-3.0-or-later (`LICENSE`, also in the jar as
`LICENSE_spartanweaponryarcana`).

## Sources

| What | Where |
| --- | --- |
| 1.12.2, beta-1.0.3 (the spec) | github.com/ObliviousSpartan/SpartanWeaponryArcana, branch `master`, `3b2a04a` |
| Issues | the same repository's issue tracker (all 6 read 2026-10-15) |
| Spartan Weaponry 1.21.1 | our port, `libs/SpartanWeaponry-neoforge-3.2.1+1.21.1.jar` (its API) |
| Botania 1.21.1 | no release yet: built from its `1.21.1-porting` branch (build 457), `libs/botania/` (not in git) |

## How it was made

- **Java**: written anew on the Spartan Weaponry 1.21 API (the 1.12.2 code was 24 item classes over the 1.12.2
  Spartan Weaponry classes; the 1.21 API creates every weapon type from a material). The behaviour of the 1.12.2 weapon
  callbacks (mana repair, Terra Burst, Pixielated, Warped) was ported from `weaponproperty/` and `util/ManaBurstHelper`.
- **Resources**: `python3 -I tools/convert_resources.py <1.12.2 checkout> <Spartan Weaponry 1.21 checkout> src/main/resources`
  rebuilds everything: textures (1.12.2 ones, renamed), item models (the Spartan Weaponry 1.21 iron models with the
  Arcana textures: bows and heavy crossbows with their pull / load states, thrown and blocking models), recipes, tags,
  Better Combat weapon attributes, the six languages and the Botania lexicon entries. Never edit
  `src/main/resources/{assets,data}` by hand.
- **Tests** (long, run in the background): `tools/servertest.sh` (dev server, fresh world; `-Pwith_botania`),
  `tools/scenetest.sh` (client scene `port/DevScene` + `port/BotaniaScene`: screenshots and functional checks;
  `-Pwith_botania`, `-Pscene_lang=ru_ru`, `-Pwith_compat`), `tools/nettest.sh` (`port/NetScene`: a dev client joins a
  dev dedicated server). The scenes are inert unless their system property is set and are not in the jar.

## Changes forced by 1.21

- **Ids follow Spartan Weaponry 1.21**: `dagger_manasteel` → `manasteel_dagger`; the 1.12.2 metadata item `material`
  became `livingwood_handle`, `livingwood_pole`, `dreamwood_handle`, `dreamwood_pole`, `dreamwood_crossbow_limb`. Weapon
  types renamed as in Spartan Weaponry: hammer → battle hammer, throwing axe → tomahawk, mace → flanged mace,
  staff → quarterstaff, crossbow → heavy crossbow; `voidmetal` → `void_metal`. 1.12.2 worlds cannot be converted to
  1.21 anyway.
- **Recipes use tags**: `c:ingots/manasteel`, `terrasteel`, `elementium`, `thaumium`, `void_metal`, `dawnstone`;
  Botania twigs and wood by `c:rods/livingwood`, `c:rods/dreamwood`, `botania:livingwood_logs`, ... Each recipe has a
  `neoforge:not` + `neoforge:tag_empty` condition on its ingot tag and Spartan Weaponry's `type_disabled` condition, so
  a material's recipes appear as soon as any mod (or a data pack) adds its ingots.
- **Thaumcraft and Embers have no 1.21 versions.** Thaumium, Void Metal and Dawnstone weapons keep their 1.12.2 stats
  and textures and work with any mod that adds the ingots (`c:ingots/thaumium`, ...). Dropped with the mods: the
  Thaumcraft research entry (`research/sw_arcana.json`) and the arcane workbench gating of the Thaumium / Void recipes
  (now crafting table recipes), the warp of Void Metal weapons (`TC.WARP`). Void Metal keeps its Warped self repair.
- **Pixielated**: 1.12.2 implemented Botania's `IPixieSpawner` on the weapon; Botania 1.21 reads the
  `botania:pixie_spawn_chance` attribute, which the trait now adds to the weapon (5 %, as before).
- **Mana repair and Terra Burst** call Botania 1.21 (`ManaItemHandler.requestManaExactForTool`, `ManaBurstEntity`,
  the Terra Blade sound). The Terra Burst is a mana burst with a `terra_burst` lens item (registered only with Botania)
  carrying the attacker's attack damage, as 1.12.2 passed the weapon damage + 1.
- **Lexicon**: 1.12.2 added three Lexica Botania entries in Java; Botania 1.21's lexicon is a Patchouli book, so the
  entries are Patchouli JSON in `assets/botania/patchouli_books/lexica_botania/en_us/entries/tools/` (category Tools,
  the same unlocking advancements, the text split over two pages, then the recipes).
- **Botania integration is soft**: `compat/botania/BotaniaCompat` never touches Botania's classes;
  `BotaniaIntegration` does and is loaded only with Botania. Because Botania 1.21.1 is unreleased, a `LinkageError`
  (a changed Botania API) turns the integration off with a log line instead of crashing.

## Fixes of the original

| Issue | Fix |
| --- | --- |
| [#5](https://github.com/ObliviousSpartan/SpartanWeaponryArcana/issues/5) Embers weapons are loaded regardless of the mod | The creative tab, and so JEI / EMI, lists only the materials some mod provides ingots for (and the Livingwood / Dreamwood parts only with Botania); recipes need the ingots. The items stay registered so worlds keep them when a mod is removed. |
| [#3](https://github.com/ObliviousSpartan/SpartanWeaponryArcana/issues/3) Swinging a Terrasteel weapon on a server kicks the player ("string length 21 > 20": the 1.12.2 channel name `spartanweaponryarcana` was too long) | 1.21 payload ids have no such limit; tested with a client joining a dedicated server (`tools/nettest.sh`). |
| [#4](https://github.com/ObliviousSpartan/SpartanWeaponryArcana/issues/4) No config for the weapons | The weapons are Spartan Weaponry API weapons: Spartan Weaponry's per-type settings (`spartanweaponry-common.toml`: speed, base damage, damage multiplier, disabled types) apply to them; the material traits are data (`data/spartanweaponryarcana/tags/spartanweaponry/weapon_trait/materials/`). The materials' own numbers stay fixed, as in 1.12.2. |
| [#1](https://github.com/ObliviousSpartan/SpartanWeaponryArcana/issues/1) Crash at start-up (class missing with a mismatched Spartan Weaponry) | The mod checks the Spartan Weaponry API version at start-up (`SpartanWeaponryAPI.assertAPIVersion`) and `neoforge.mods.toml` requires Spartan Weaponry 3.2.1+. |

Found while porting:

- **Warped repaired too fast with several players**: 1.12.2 counted one global tick counter up once per player per tick,
  so with N players online Void Metal weapons repaired N times as often, at uneven moments. Now once per second of
  world time.
- **Warped set the damage of an undamaged weapon to -1** (no damage check). Only damaged weapons repair now.
- **Mana repair and Warped only ran for melee weapons** in Spartan Weaponry 1.21 (only its melee and throwing items
  have a trait container): bows and heavy crossbows did not repair. They run for every Arcana weapon in the inventory
  now, by the material's traits, as in 1.12.2.
- Ukrainian names with «зі» before «св-» (зі світанкового каменю).

## Not ported / not tested

- Thaumcraft research, arcane crafting and warp (no Thaumcraft for 1.21), Embers integration (1.12.2 had none
  besides the material).
- Botania is unreleased for 1.21.1: tested with a build of its `1.21.1-porting` branch (build 457). A released Botania
  may change its API; the integration then turns itself off instead of crashing.
- Botania needs NeoForge 21.1.262+ (its own requirement); without Botania the mod runs on 21.1.197+.

## Performance check (2026-10-17)

Checked, nothing to change: the only per-tick work is `ArcanaEvents.onPlayerTick` (server side), a pass over the
player's inventory that skips empty and undamaged stacks before it looks at anything else; Mana Repair asks Botania
for mana as the 1.12.2 weapons did every tick, Warped repairs once a second. No per-frame client code besides the
Terra Slash packet on an air swing. The oil and NBT changes of Spartan Weaponry (its performance check) apply here too.

## Multiloader: NeoForge + Fabric 1.21.1 (2026-10-21, branch `multiloader`)

- Layout as in Spartan Weaponry: `common/` (no loader code; its own `platform/Platform` and `platform/Registrar`),
  `neoforge/`, `fabric/`. Compiled against the multiloader Spartan Weaponry jars in `libs/`
  (`spartanweaponry-{common,neoforge,fabric}-3.2.1.1+1.21.1.jar`); Botania in `libs/botania/{xplat,neoforge,fabric}`
  (local builds of its 1.21.1-porting branch, not in git; the Fabric one needs Loom 1.15.5 and Fabric Loader 0.17.3).
- Fabric start: the entrypoint `spartanweaponry`, which Spartan Weaponry calls after its own start (the weapons are
  made by its API; Fabric does not order the `main` entrypoints of dependent mods).
- Fabric: the Terra Slash swing at the air is a mixin in `Minecraft.startAttack` (NeoForge LeftClickEmpty); the player
  tick is the end of the server tick; recipe conditions `fabric:load_conditions` next to the NeoForge ones (145 files,
  `tools/port/neo2fabric_conditions.py`, `not tag_empty` → Spartan Weaponry's `spartanweaponry:tag_not_empty`).
- Fix (both loaders): the Pixielated trait put its modifier on the registry's holder of `botania:pixie_spawn_chance`;
  Botania's attribute maps hold its own holder (a `Holder.Reference` subclass), and vanilla compares holders by
  identity, so on Fabric players got no pixie chance. The trait uses Botania's holder now.
- Tests: client scene 22/22 without Botania and 34/34 with it, on both loaders; multiplayer test (dedicated server +
  client, Terra Burst) 6/6 on both.

## NeoForge and Fabric release check (2026-10-22, 1.0.3.1)

- Release jar with Spartan Weaponry 3.2.1.2 on real NeoForge servers 21.1.197 and 21.1.259 and on a dedicated Fabric
  server, with and without Botania: starts clean. The client scene on a Fabric production client: 22/22 without Botania,
  34/34 with it.
- Compatibility with Botania on both loaders (Sodium 0.6.13, Iris 1.8.8 on Fabric with Complementary Reimagined r5.9.3,
  Lithium, FerriteCore, ModernFix, Jade; JEI on NeoForge, EMI on Fabric): 34/34. The first Fabric run failed "data pack:
  Thaumium repairs with the ingot" and logged 96 EMI repair errors. Both were Spartan Weaponry's repair ingredient (see its
  PORTLOG, fixed in 3.2.1.2).
- Botania has no release past the 1.21.1 porting branch. It stays optional (`type="optional"`, Fabric `suggests`).
- Later the same day: Loom's `ClientProductionRunTask` also puts the project's `remapJar` into `mods`, so the scenes
  jar and the release jar ran together (Fabric happened to load the scenes jar). The task now uses `mods.setFrom`, and the
  production runs were repeated with only the scenes jar: all checks pass. `fabric.mod.json` in the jar is valid JSON
  now (the description's `\n` had become a raw newline when Gradle expanded the template).
- Later the same day: the compatibility runs had Iris without its shader pack (`shaderPack=` was empty in the copied
  `iris.properties`). Repeated with Complementary Reimagined r5.9.3 on: 34/34 on both loaders. The Mana Repair check
  now counts server ticks: under shaders the client ran its 40 ticks while the integrated server ran 17, so the weapons
  had repaired 17 points (one per server tick, as intended).

## 1.21.11 (branch mc/1.21.11, 2026-10-22, 1.0.3.1+1.21.11)

NeoForge 21.11.48, Fabric Loader 0.19.5 + Fabric API 0.141.6, Forge Config API Port 21.11.1, Spartan Weaponry
3.2.1.2+1.21.11, Fabric Loom 1.17.21 (Gradle 9.8.1). No Parchment for 1.21.11.

- **No Botania**: Botania has no 1.21.11 version, so compat/botania (the Botania API code: mana repair, the Terra
  Burst, the pixie attribute, the burst lens item) and its scene are not on this branch. BotaniaCompat says Botania is
  absent; the rest behaves as on 1.21.1 without Botania: the Manasteel, Terrasteel and Elementium weapons are
  registered, their recipes wait for Botania, the traits have nothing to act on. The multiplayer scene checks that a
  Terrasteel swing sends no burst and the connection holds.
- Items get their ids from the registrar (Spartan Weaponry's ItemIds); looks from item definitions made by
  `tools/port/gen_item_definitions.py` (Spartan Weaponry's generator for this namespace: the oil coating as a
  condition, blocking/throwing/pull/charged/empty/ammo as Weaponry's properties).
- Recipes: string ingredients; `neoforge:not tag_empty` → Spartan Weaponry's `tag_not_empty` (NeoForge 21.11 only asks
  whether a tag exists). Enchanting: `enchantable/sword` → `enchantable/melee_weapon` and `enchantable/sweeping`.
- Item data read without a copy through Spartan Weaponry's ItemNbt (CustomData#getUnsafe is gone).
- **Verified**: client scene 22 checks on both loaders, all pass; the multiplayer scene; release jar on a real NeoForge
  21.11.48 server and a dedicated Fabric server; the scene on a Fabric production client; compatibility runs with
  Sodium 0.8.14, Iris 1.10.8, Lithium, FerriteCore, Jade (Fabric with the Complementary shaders, NeoForge without a
  shader pack: Iris 1.10.8's NeoForge shadow pass crashes on enchanted items). No stress scene (owner's call).

## 26.1.2 (branch mc/26.1.2, 2026-10-23, 1.0.3.1+26.1.2)

NeoForge 26.1.2.122, Fabric Loader 0.19.5 + Fabric API 0.155.3, Forge Config API Port 26.1.5, Spartan Weaponry
3.2.1.2+26.1.2, Java 25, Fabric Loom 1.18 (26.x has no obfuscation: no mappings, no remapped jar). No Botania, as on
1.21.11: Botania has no 26.1.2 version.

- The code changes are those of Spartan Weaponry's 26.1.2 port that reach the addon: `CreativeModeTab.Output` is
  protected in vanilla (a compile-time access transformer for the common project), Fabric API 0.155 names, the test
  scenes on the 26.1 world settings and the extracting tooltip.
- **Verified**: client scene 22 checks on both loaders, all pass; the multiplayer scene on both loaders; release jar on a
  real NeoForge 26.1.2.122 server and a dedicated Fabric server; the scene on a Fabric production client; compatibility
  runs with Sodium 0.9.2, Iris 1.11.4, Lithium 0.24.7, FerriteCore 9.0.0, Jade (+ ModernFix 5.27.24 on NeoForge): Fabric
  with the Complementary shaders, NeoForge without a shader pack (Iris 1.11.4's NeoForge shadow pass crashes on
  enchanted items). No stress scene (owner's call).

## 26.2 (branch mc/26.2, 2026-10-23, 1.0.3.1+26.2)

NeoForge 26.2.0.93, Fabric Loader 0.19.5 + Fabric API 0.161.0, Forge Config API Port 26.2.1, Spartan Weaponry
3.2.1.2+26.2, Java 25, Fabric Loom 1.18. No Botania (no 26.2 version), as on 1.21.11 and 26.1.2.

- The addon's code needed no change for 26.2; the test scenes moved to `Minecraft.gui`, and they go on past NeoForge's
  load warnings screen (the multiplayer scene presses its "Proceed" button, so the quick play connection stays).
  `bannerFile` instead of the deprecated `logoFile`.
- **Verified**: client scene 22 checks on both loaders, all pass; the multiplayer scene on both loaders (5 checks);
  release jar on a real NeoForge 26.2.0.93 server and a dedicated Fabric server; the scene on a Fabric production
  client; compatibility runs with Sodium 0.9.2, Iris 1.11.4, Lithium 0.25.3, FerriteCore 9.0.0, Jade 26.2 (Fabric with
  the Complementary shaders, NeoForge without a shader pack). No stress scene (owner's call).

## 26.3 (branch mc/26.3, 2026-10-23, 1.0.3.1+26.3)

NeoForge 26.3.0.95, Fabric Loader 0.19.5 + Fabric API 0.165.0, Forge Config API Port 26.3.1, Spartan Weaponry
3.2.1.2+26.3, Java 25, Fabric Loom 1.18. No Botania (no 26.3 version).

- The addon's code needed no change for 26.3 beyond the test scenes (SDL input, the tooltip's new argument).
