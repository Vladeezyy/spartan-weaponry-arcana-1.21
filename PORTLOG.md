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
