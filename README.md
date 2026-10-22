<div align="center">

<img src="docs/images/logo.png" width="320" alt="Spartan Weaponry: Arcana logo">

# Spartan Weaponry: Arcana — NeoForge & Fabric 1.21.1

**Magic metals for the Spartan arsenal**

[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-3c8527)](https://www.minecraft.net)
[![NeoForge 21.1](https://img.shields.io/badge/NeoForge-21.1.197%2B-e0682b)](https://neoforged.net)
[![Fabric 0.16](https://img.shields.io/badge/Fabric-0.16.14%2B-dbd0b4)](https://fabricmc.net)
[![Requires Spartan Weaponry](https://img.shields.io/badge/requires-Spartan%20Weaponry%203.2.1.2%2B-8a2be2)](#-installation)
[![License GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-blue)](LICENSE)

An unofficial port of **Spartan Weaponry: Arcana** beta-1.0.3 by **ObliviousSpartan** from Minecraft 1.12.2 to
**NeoForge and Fabric 1.21.1**, as an addon of our [Spartan Weaponry 1.21.1 port](https://github.com/Vladeezyy/spartan-weaponry-1.21).

</div>

---

## ⚔️ About the mod

Arcana adds every Spartan Weaponry weapon type in the metals of magic mods, with the abilities those metals have in
their own mods.

- **132 weapons**: daggers, longswords, katanas, sabers, rapiers, greatswords, battle hammers, warhammers, spears,
  halberds, pikes, lances, longbows, heavy crossbows, throwing knives, tomahawks, javelins, boomerangs, battleaxes,
  flanged maces, glaives and quarterstaves in six materials:

  | Material | From | Ability |
  | --- | --- | --- |
  | **Manasteel** | Botania | **Mana Repair**: repairs itself with mana from Mana Tablets and other mana items in your inventory |
  | **Terrasteel** | Botania | **Mana Repair** and **Terra Burst**: a swing at the air fires a mana burst that deals your melee damage, like the Terra Blade |
  | **Elementium** | Botania | **Mana Repair** and **Pixielated**: a 5 % chance that a pixie defends you when you are hurt |
  | **Thaumium** | any mod with `c:ingots/thaumium` | — |
  | **Void Metal** | any mod with `c:ingots/void_metal` | **Warped**: repairs one durability every second |
  | **Dawnstone** | any mod with `c:ingots/dawnstone` | — |

- **Livingwood and Dreamwood handles and poles**, and a Dreamwood crossbow limb, made from Botania twigs.
- **Botania lexicon**: three entries in the Tools category of the Lexica Botania, with every recipe.
- **Recipes appear with the metal**: a material's weapons are craftable, and shown in the creative tab, JEI and EMI,
  as soon as some mod (or a data pack) adds its ingots to the `c:ingots/<material>` tag. Botania 1.21.1 gives
  Manasteel, Terrasteel and Elementium; Thaumcraft and Embers have no 1.21 versions, so Thaumium, Void Metal and
  Dawnstone wait for a mod that adds those ingots.
- **Languages**: English, Russian, Ukrainian, German, Spanish, Brazilian Portuguese (all but English are new: machine
  translations checked by hand, corrections welcome).

## 📸 Screenshots

<p align="center">
  <img src="docs/images/wall_botania.jpg" width="49%" alt="The Manasteel, Terrasteel and Elementium weapons, the Livingwood and Dreamwood parts">
  <img src="docs/images/wall_thaumcraft_embers.jpg" width="49%" alt="The Thaumium, Void Metal and Dawnstone weapons">
  <img src="docs/images/creative_tab.jpg" width="49%" alt="The creative tab with Botania and EMI">
  <img src="docs/images/tooltip.jpg" width="49%" alt="Tooltips: Mana Repair, Terra Burst, Pixielated">
  <img src="docs/images/lexicon_terra_weaponry.jpg" width="49%" alt="The Terrasteel Weaponry entry of the Lexica Botania">
  <img src="docs/images/lexicon_terra_weaponry_recipes.jpg" width="49%" alt="Recipes in the Lexica Botania">
</p>

## 📦 Installation

**NeoForge**

1. Install **NeoForge 21.1.197** or newer for Minecraft **1.21.1** (21.1.262 or newer with Botania, which needs it).
2. Install **[Spartan Weaponry 3.2.1.2+1.21.1](https://github.com/Vladeezyy/spartan-weaponry-1.21/releases)** or newer.
3. Put `spartanweaponryarcana-neoforge-1.0.3.1+1.21.1.jar` from the [releases](../../releases) into `mods`.

**Fabric**

1. Install **Fabric Loader 0.16.14** or newer for Minecraft **1.21.1** (0.17.3 or newer with Botania, which needs it).
2. Install **[Spartan Weaponry 3.2.1.2+1.21.1](https://github.com/Vladeezyy/spartan-weaponry-1.21/releases)** for
   Fabric or newer, with what it needs (Fabric API, Forge Config API Port).
3. Put `spartanweaponryarcana-fabric-1.0.3.1+1.21.1.jar` from the [releases](../../releases) into `mods`.

Both: required on the client and the server. Optional: **Botania** (Manasteel, Terrasteel, Elementium, the abilities
and the lexicon), any mod adding Thaumium, Void Metal or Dawnstone ingots, **JEI** / **EMI**.
The weapons follow Spartan Weaponry's per-type settings (`config/spartanweaponry-common.toml`: speed, damage,
disabled types).

> **Botania for 1.21.1 is not released yet.** Arcana was tested with a build of Botania's 1.21.1 porting branch.
> If a released Botania changes its API, Arcana turns its Botania features off (with a line in the log) instead of
> crashing; the weapons stay.

## 🔧 What changed in the port

Weapons, stats, textures and recipes are those of beta-1.0.3; ids follow Spartan Weaponry 1.21
(`manasteel_dagger` instead of `dagger_manasteel`). Thaumcraft research, arcane crafting and the warp of Void Metal
weapons are gone with Thaumcraft (no 1.21 version): Thaumium and Void Metal weapons are made on a crafting table.

| Original issue | Fix |
| --- | --- |
| [#5](https://github.com/ObliviousSpartan/SpartanWeaponryArcana/issues/5) Embers weapons are loaded regardless of the mod | The creative tab, JEI and EMI only show the materials some mod provides; recipes need the ingots |
| [#3](https://github.com/ObliviousSpartan/SpartanWeaponryArcana/issues/3) Swinging a Terrasteel weapon on a server kicks the player | Fixed by the new network code; tested with a client on a dedicated server |
| [#4](https://github.com/ObliviousSpartan/SpartanWeaponryArcana/issues/4) No config for the weapons | Spartan Weaponry's per-type settings apply; traits are data |
| [#1](https://github.com/ObliviousSpartan/SpartanWeaponryArcana/issues/1) Crash at start-up with a mismatched Spartan Weaponry | Checked at start-up with a clear message |

Found while porting:

- Void Metal weapons repaired faster with every extra player online, and undamaged ones got a durability of -1.
- Bows and heavy crossbows did not repair with mana (Spartan Weaponry 1.21 gives traits to melee and thrown weapons
  only); every Arcana weapon repairs now, as in 1.12.2.

The full list, with the reasons and what 1.21 changed, is in **[PORTLOG.md](PORTLOG.md)**.

## ✅ Tested

- **Functional** (34 checks, in English and Russian, with and without Botania): items, recipes by tag (and a data
  pack adding Thaumium ingots), the creative tab, damage, durability, enchanting table, Mana Repair (melee and bows),
  Warped, Pixielated, Terra Burst with real input, the lexicon entries; dedicated server with and without Botania; a
  client swinging a Terrasteel greatsword on a dedicated server.
- **Compatibility**: Sodium 0.6, Iris 1.8, Lithium, FerriteCore, ModernFix, JEI, EMI, Jade, Curios, Patchouli.
- **Fabric** (Loader 0.17.3, Fabric API 0.116.17, Forge Config API Port 21.1.6, Spartan Weaponry 3.2.1.2): the same
  scene with and without Botania (34 and 22 checks), the release jar on a dedicated Fabric server with and without
  Botania, the scene on a production client; compatibility with Sodium 0.6.13 + Iris 1.8.8 with Complementary shaders,
  Lithium, FerriteCore, ModernFix, Jade, EMI, Trinkets, Patchouli.

## ❤️ Credits

- **Spartan Weaponry: Arcana** and **Spartan Weaponry** — made by **ObliviousSpartan** ·
  [source](https://github.com/ObliviousSpartan/SpartanWeaponryArcana). All the content, art and ideas are theirs.
- **Botania** by Vazkii and contributors: the Terra Burst follows its Terra Blade.
- **Port** to NeoForge and Fabric 1.21.1 — [Vladeezyy](https://github.com/Vladeezyy).
- Bugs of **this port**: [issues](../../issues). Please don't report port bugs to the original author.

### License

Like the original, the port is licensed under the **GNU GPL 3.0 or later** ([LICENSE](LICENSE)).
Spartan Weaponry: Arcana © ObliviousSpartan; port © 2026 Vladeezyy.
Forks are welcome: they must stay open, under the same license, and credit the authors.
Please download the mod only from the official pages.

---

<details>
<summary><b>🇷🇺 Описание на русском</b></summary>

<br>

**Магические металлы для спартанского арсенала**

Неофициальный порт мода **Spartan Weaponry: Arcana** beta-1.0.3 от **ObliviousSpartan** с Minecraft 1.12.2 на
**NeoForge и Fabric 1.21.1** — аддон к нашему [порту Spartan Weaponry на 1.21.1](https://github.com/Vladeezyy/spartan-weaponry-1.21).

<details>
<summary><b>⚔️ О моде</b></summary>

Arcana добавляет все виды оружия Spartan Weaponry из металлов магических модов — со способностями, которые у этих
металлов есть в их собственных модах.

- **132 предмета**: кинжалы, полуторные мечи, катаны, сабли, рапиры, двуручные мечи, боевые молоты, клевцы, копья,
  алебарды, пики, турнирные копья, большие луки, тяжёлые арбалеты, метательные ножи, томагавки, метательные копья,
  бумеранги, боевые топоры, перначи, глефы и боевые посохи из шести материалов:

  | Материал | Откуда | Способность |
  | --- | --- | --- |
  | **Манасталь** | Botania | **Починка маной**: чинится маной из мана-планшетов и других предметов с маной в инвентаре |
  | **Террасталь** | Botania | **Починка маной** и **Терра-вспышка**: взмах в воздух выпускает вспышку маны с уроном ближнего боя, как Терра-клинок |
  | **Элементий** | Botania | **Починка маной** и **Пикси**: 5 % шанс, что пикси защитит вас, когда вас ранят |
  | **Таумиум** | любой мод с `c:ingots/thaumium` | — |
  | **Пустотный металл** | любой мод с `c:ingots/void_metal` | **Искажённое**: чинит одну единицу прочности в секунду |
  | **Рассветный камень** | любой мод с `c:ingots/dawnstone` | — |

- **Рукояти и шесты из жизнедерева и дерева мечтаний** и плечо арбалета из дерева мечтаний — из веточек Botania.
- **Лексикон Botania**: три статьи в разделе «Инструменты» со всеми рецептами.
- **Рецепты появляются вместе с металлом**: оружие материала можно скрафтить, и оно видно во вкладке творчества, JEI
  и EMI, как только какой-нибудь мод (или датапак) добавит его слитки в тег `c:ingots/<материал>`. Botania 1.21.1
  даёт манасталь, террасталь и элементий; у Thaumcraft и Embers нет версий для 1.21, поэтому таумиум, пустотный
  металл и рассветный камень ждут мода с такими слитками.
- **Языки**: английский, русский, украинский, немецкий, испанский, бразильский португальский (все, кроме английского,
  новые: машинный перевод, проверенный вручную, правки приветствуются).

</details>

<details>
<summary><b>📦 Установка</b></summary>

**NeoForge**

1. Установите **NeoForge 21.1.197** или новее для Minecraft **1.21.1** (с Botania — 21.1.262 или новее, это её
   требование).
2. Установите **[Spartan Weaponry 3.2.1.2+1.21.1](https://github.com/Vladeezyy/spartan-weaponry-1.21/releases)** или
   новее.
3. Положите `spartanweaponryarcana-neoforge-1.0.3.1+1.21.1.jar` из [релизов](../../releases) в папку `mods`.

**Fabric**

1. Установите **Fabric Loader 0.16.14** или новее для Minecraft **1.21.1** (с Botania — 0.17.3 или новее, это её
   требование).
2. Установите **[Spartan Weaponry 3.2.1.2+1.21.1](https://github.com/Vladeezyy/spartan-weaponry-1.21/releases)** для
   Fabric или новее, с тем, что ему нужно (Fabric API, Forge Config API Port).
3. Положите `spartanweaponryarcana-fabric-1.0.3.1+1.21.1.jar` из [релизов](../../releases) в папку `mods`.

Для обоих: нужен и на клиенте, и на сервере. По желанию: **Botania** (манасталь, террасталь, элементий, способности и
лексикон), любой мод со слитками таумиума, пустотного металла или рассветного камня, **JEI** / **EMI**.
На оружие действуют настройки Spartan Weaponry по видам оружия (`config/spartanweaponry-common.toml`: скорость,
урон, отключение видов).

> **Botania для 1.21.1 ещё не вышла.** Arcana проверена со сборкой ветки порта Botania на 1.21.1. Если вышедшая
> Botania изменит API, Arcana отключит свои функции Botania (с записью в логе), а не упадёт; оружие останется.

</details>

<details>
<summary><b>🔧 Что изменилось в порте</b></summary>

Оружие, характеристики, текстуры и рецепты — как в beta-1.0.3; id как в Spartan Weaponry 1.21 (`manasteel_dagger`
вместо `dagger_manasteel`). Исследования Thaumcraft, тайный верстак и «искажение» (warp) от оружия из пустотного
металла ушли вместе с Thaumcraft (версии для 1.21 нет): оружие из таумиума и пустотного металла делается на верстаке.

- **Оружие Embers загружалось без мода** ([#5](https://github.com/ObliviousSpartan/SpartanWeaponryArcana/issues/5)) —
  вкладка, JEI и EMI показывают только материалы, которые даёт какой-то мод; рецептам нужны слитки.
- **Кик с сервера при взмахе оружием из террастали** ([#3](https://github.com/ObliviousSpartan/SpartanWeaponryArcana/issues/3)) —
  исправлено новой сетевой частью; проверено клиентом на выделенном сервере.
- **Нет настроек оружия** ([#4](https://github.com/ObliviousSpartan/SpartanWeaponryArcana/issues/4)) — действуют
  настройки Spartan Weaponry по видам; особенности материалов задаются данными.
- **Краш при запуске с неподходящей Spartan Weaponry** ([#1](https://github.com/ObliviousSpartan/SpartanWeaponryArcana/issues/1)) —
  версия проверяется при запуске с понятным сообщением.

Найдено при портировании: оружие из пустотного металла чинилось тем быстрее, чем больше игроков онлайн, а целому
ставилась прочность -1; луки и тяжёлые арбалеты не чинились маной — теперь чинится всё оружие Arcana, как в 1.12.2.

Полный список с причинами — в **[PORTLOG.md](PORTLOG.md)** (на английском).

</details>

<details>
<summary><b>✅ Проверено</b></summary>

- **Функционально** (34 проверки, на английском и русском, с Botania и без): предметы, рецепты по тегам (и датапак
  со слитками таумиума), вкладка творчества, урон, прочность, стол зачарований, починка маной (оружие ближнего боя и
  луки), «Искажённое», «Пикси», терра-вспышка с настоящим вводом, статьи лексикона; выделенный сервер с Botania и без;
  клиент с двуручным мечом из террастали на выделенном сервере.
- **Совместимость**: Sodium 0.6, Iris 1.8, Lithium, FerriteCore, ModernFix, JEI, EMI, Jade, Curios, Patchouli.
- **Fabric** (Loader 0.17.3, Fabric API 0.116.17, Forge Config API Port 21.1.6, Spartan Weaponry 3.2.1.2): та же сцена
  с Botania и без (34 и 22 проверки), release jar на выделенном сервере Fabric с Botania и без, сцена на
  production-клиенте; совместимость с Sodium 0.6.13 + Iris 1.8.8 с шейдерами Complementary, Lithium, FerriteCore,
  ModernFix, Jade, EMI, Trinkets, Patchouli.

</details>

<details>
<summary><b>❤️ Авторы и лицензия</b></summary>

- **Spartan Weaponry: Arcana** и **Spartan Weaponry** — автор **ObliviousSpartan** ·
  [исходники](https://github.com/ObliviousSpartan/SpartanWeaponryArcana). Всё содержимое, графика и идеи — их.
- **Botania** — Vazkii и участники: терра-вспышка повторяет Терра-клинок.
- **Порт** на NeoForge и Fabric 1.21.1 — [Vladeezyy](https://github.com/Vladeezyy).
- Баги **этого порта**: [issues](../../issues). Пожалуйста, не пишите о багах порта автору оригинала.

Как и оригинал, порт распространяется по **GNU GPL 3.0 или новее** ([LICENSE](LICENSE)).
Spartan Weaponry: Arcana © ObliviousSpartan; порт © 2026 Vladeezyy.
Форки приветствуются: они должны оставаться открытыми, под той же лицензией и с указанием авторов.
Скачивайте мод только с официальных страниц.

</details>

</details>
