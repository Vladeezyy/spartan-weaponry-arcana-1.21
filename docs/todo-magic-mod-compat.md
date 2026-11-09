# To do: weapons from other magic mods

Status: not started. This branch only holds the plan.

## Request
CurseForge comments on this project, by dogboy_cole:
- about 2026-10-25: are there plans for compatibility with other magic mods? Vladeezyy answered on 2026-11-08: yes, and asked
  which mods.
- 2026-11-08: the list below. Nobody has answered it yet.
Link: https://www.curseforge.com/minecraft/mc-mods/spartan-weaponry-arcana-unofficial-port/comments

What they suggested, mod by mod:
- **Ars Nouveau**: Source Gem weapons in the Spartan weapon types that work like Ars Nouveau's enchanter's gear.
- **Occultism**: Iesnium weapons that deal extra damage to summoned spirits (players farm drops from unbound spirits).
- **Nature's Aura**: it has several metals. The commenter does not know what they do.
- **Goety**: weapons from its cursed metals, with effects taken from that mod.
- **Spectrum**: it has many materials that could give weapons unusual properties.
- Mentioned with less detail: **Malum**, **Blood Magic / Neo Vitae**, **Gobber**.

## Before starting
- Check which of these mods have a release for every version we ship (1.21.1, 1.21.11, 26.1.2, 26.2, 26.3; NeoForge and
  Fabric). Look up the material item ids and tags (`c:ingots/<material>`, gems), and each mod's license.
- Choose the first batch with the user. Ars Nouveau and Occultism have the clearest ideas.
- Follow the existing pattern: a material per mod through Spartan Weaponry's API, recipes behind
  `not tag_empty` conditions, soft integration that stays off when the mod is missing (as with Botania), and
  lang in all six languages.
- Effects such as extra damage against spirits must not cost anything per tick. Run them on a hit event and look up an
  entity tag, not a class check over all mobs.
