#!/usr/bin/env python3
"""Rebuilds src/main/resources/{assets,data} for 1.21.1 from the 1.12.2 Spartan Weaponry Arcana resources.

    python3 -I tools/convert_resources.py <SpartanWeaponryArcana 1.12.2 checkout> <spartan-weaponry-1.21 checkout> src/main/resources

- textures: 1.12.2 names ("dagger_manasteel", "crossbow_manasteel_0") → Spartan Weaponry 1.21 names
  ("manasteel_dagger", "manasteel_heavy_crossbow_pulling_0")
- item models: those of the Spartan Weaponry 1.21 iron weapons with the Arcana textures (oil coating, blocking,
  throwing, pulling ...), plus generated models for the handles and poles
- recipes: 1.12.2 recipes with 1.21 ids (Botania 1.12 metadata items → Botania 1.21 items, ore dictionary → tags),
  enabled by the material's c:ingots tag (and Botania for Botania parts) and Spartan Weaponry's type_disabled condition
- tags: the weapons in Spartan Weaponry's weapon type tags (oils, crosshairs, quivers ...), the enchantable tags,
  the material trait tags; Better Combat weapon_attributes like Spartan Weaponry's
- lang: en_us from the 1.12.2 .lang plus generated weapon names; ru_ru, uk_ua, de_de, es_es, pt_br (machine
  translations; Botania's own names for its materials)
Running it again gives the same result.
"""
import json
import os
import re
import shutil
import sys

MOD = 'spartanweaponryarcana'
SW = 'spartanweaponry'

# 1.12.2 material name → 1.21 name
MATERIALS = {'manasteel': 'manasteel', 'terrasteel': 'terrasteel', 'elementium': 'elementium',
             'thaumium': 'thaumium', 'voidmetal': 'void_metal', 'dawnstone': 'dawnstone'}
BOTANIA_MATERIALS = ('manasteel', 'terrasteel', 'elementium')
# 1.12.2 weapon name → Spartan Weaponry 1.21 type
TYPES = {'dagger': 'dagger', 'longsword': 'longsword', 'katana': 'katana', 'saber': 'saber', 'rapier': 'rapier',
         'greatsword': 'greatsword', 'hammer': 'battle_hammer', 'warhammer': 'warhammer', 'spear': 'spear',
         'halberd': 'halberd', 'pike': 'pike', 'lance': 'lance', 'longbow': 'longbow', 'crossbow': 'heavy_crossbow',
         'throwing_knife': 'throwing_knife', 'throwing_axe': 'tomahawk', 'javelin': 'javelin', 'boomerang': 'boomerang',
         'battleaxe': 'battleaxe', 'mace': 'flanged_mace', 'glaive': 'glaive', 'staff': 'quarterstaff'}
# Spartan Weaponry weapon type tag of each type
TYPE_TAGS = {'dagger': 'daggers', 'longsword': 'longswords', 'katana': 'katanas', 'saber': 'sabers', 'rapier': 'rapiers',
             'greatsword': 'greatswords', 'battle_hammer': 'battle_hammers', 'warhammer': 'warhammers', 'spear': 'spears',
             'halberd': 'halberds', 'pike': 'pikes', 'lance': 'lances', 'longbow': 'longbows',
             'heavy_crossbow': 'heavy_crossbows', 'throwing_knife': 'throwing_knives', 'tomahawk': 'tomahawks',
             'javelin': 'javelins', 'boomerang': 'boomerangs', 'battleaxe': 'battleaxes', 'flanged_mace': 'flanged_maces',
             'glaive': 'glaives', 'quarterstaff': 'quarterstaves'}
THROWING = ('throwing_knife', 'tomahawk', 'javelin', 'boomerang')
RANGED = ('longbow', 'heavy_crossbow')
# 1.12.2 "spartanweaponryarcana:material" metadata
ARCANA_PARTS = ['livingwood_handle', 'livingwood_pole', 'dreamwood_handle', 'dreamwood_pole', 'dreamwood_crossbow_limb']
OLD_PARTS = ['handle_livingwood', 'pole_livingwood', 'handle_dreamwood', 'pole_dreamwood', 'crossbow_limb_dreamwood']
# Material traits (1.12.2 ToolMaterialEx properties)
TRAITS = {'manasteel': ['mana_regenerate'], 'terrasteel': ['mana_regenerate_terra', 'terra_slash'],
          'elementium': ['mana_regenerate', 'pixielated_5'], 'thaumium': [], 'void_metal': ['warped'], 'dawnstone': []}


def ingredient(i):
    """1.12.2 ingredient → 1.21 ingredient"""
    if isinstance(i, list):
        assert len(i) == 1, i
        return ingredient(i[0])
    if i.get('type') == 'forge:ore_dict':
        ore = i['ore']
        m = re.match(r'ingot(Manasteel|Terrasteel|ElvenElementium|Thaumium|Void|Dawnstone)$', ore)
        if m:
            name = {'ElvenElementium': 'elementium', 'Void': 'void_metal'}.get(m.group(1), m.group(1).lower())
            return {'tag': 'c:ingots/' + name}
        return {'tag': {'leather': 'c:leathers', 'string': 'c:strings', 'stickWood': 'c:rods/wooden',
                        'logWood': 'minecraft:logs', 'plankWood': 'minecraft:planks'}[ore]}
    item, data = i['item'], i.get('data')
    if item == 'botania:manaresource':
        return {'item': {3: 'botania:livingwood_twig', 13: 'botania:dreamwood_twig', 16: 'botania:mana_string'}[data]}
    if item in ('botania:livingwood', 'botania:dreamwood'):
        wood = item.split(':')[1]
        return {'tag': 'botania:%s_logs' % wood} if data == 0 else {'item': 'botania:%s_planks' % wood}
    if item == 'botania:livingwoodbow':
        return {'item': 'botania:livingwood_bow'}
    if item == 'minecraft:wool':
        return {'tag': 'minecraft:wool'}
    if item == 'minecraft:bow':
        return {'item': 'minecraft:bow'}
    if item == SW + ':material':
        return {'item': SW + ':' + ['handle', 'pole'][data]}
    if item == MOD + ':material':
        return {'item': MOD + ':' + ARCANA_PARTS[data]}
    raise ValueError(i)


def uses_botania(recipe):
    return 'botania:' in json.dumps(recipe) or any(ingredient_is_botania_part(v) for v in recipe.get('key', {}).values())


def ingredient_is_botania_part(i):
    return isinstance(i, dict) and i.get('item') == MOD + ':material'


def new_id(old):
    """1.12.2 item / recipe name → 1.21 name"""
    if old in OLD_PARTS:
        return ARCANA_PARTS[OLD_PARTS.index(old)]
    # the 1.12.2 textures of the staves are named "quarterstaff_*", the items "staff_*"
    for t_old, t_new in sorted(dict(TYPES, quarterstaff='quarterstaff').items(), key=lambda kv: -len(kv[0])):
        if old.startswith(t_old + '_'):
            mat = old[len(t_old) + 1:]
            if mat in MATERIALS:
                return MATERIALS[mat] + '_' + t_new
    return None


def recipe(old_name, d):
    out = {'type': d['type']}
    conditions = []
    result = d['result']
    name = new_id(result['item'].split(':')[1]) if result['item'] != MOD + ':material' else ARCANA_PARTS[result['data']]
    weapon_type = next((t for t in TYPES.values() if name.endswith('_' + t)), None)
    material = name[:-len(weapon_type) - 1] if weapon_type else None
    if weapon_type:
        conditions.append({'type': SW + ':type_disabled', 'disabled': [weapon_type]})
    if uses_botania(d):
        conditions.append({'type': 'neoforge:mod_loaded', 'modid': 'botania'})
    if material:
        conditions.append({'type': 'neoforge:not', 'value': {'type': 'neoforge:tag_empty', 'tag': 'c:ingots/' + material}})
    if conditions:
        out['neoforge:conditions'] = conditions
    if 'pattern' in d:
        out['category'] = 'equipment' if weapon_type else 'misc'
        out['pattern'] = d['pattern']
        out['key'] = {k: ingredient(v) for k, v in d['key'].items()}
    else:
        out['category'] = 'misc'
        out['ingredients'] = [ingredient(i) for i in d['ingredients']]
    res = {'id': MOD + ':' + name}
    if result.get('count', 1) != 1:
        res['count'] = result['count']
    out['result'] = res
    return name, out


def dump(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write('\n')


def load(path):
    with open(path, encoding='utf-8') as f:
        return json.load(f)


def read_lang(path):
    out = {}
    with open(path, encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith('#') and '=' in line:
                k, v = line.split('=', 1)
                out[k.strip()] = v.strip()
    return out


# ---------------------------------------------------------------------------------------------------- names
# material: en, ru (genitive "из ..."), uk (genitive "з ..."), de (compound prefix), es ("de ..."), pt ("de ...")
MAT_NAMES = {
    'manasteel': ('Manasteel', 'манастали', 'манасталі', 'Manastahl', 'manacero', 'Aço-Mana'),
    'terrasteel': ('Terrasteel', 'террастали', 'терасталі', 'Terrastahl', 'terracero', 'Aço-Terra'),
    'elementium': ('Elementium', 'элементия', 'елементію', 'Elementium', 'elementio', 'Elementium'),
    'thaumium': ('Thaumium', 'таумиума', 'тауміуму', 'Thaumium', 'taumio', 'Taumio'),
    'void_metal': ('Void', 'пустотного металла', 'порожнечного металу', 'Leerenmetall', 'metal del vacío', 'Metal do Vazio'),
    'dawnstone': ('Dawnstone', 'рассветного камня', 'світанкового каменю', 'Morgenstein', 'piedra del alba', 'Pedra da Aurora'),
}
# type: en (from Spartan Weaponry's "%s Dagger"), ru, uk, de (compound part), es, pt
TYPE_NAMES = {
    'dagger': ('Dagger', 'Кинжал', 'Кинджал', 'dolch', 'Daga', 'Adaga'),
    'longsword': ('Longsword', 'Полуторный меч', 'Півтораручний меч', 'langschwert', 'Espada larga', 'Espada Longa'),
    'katana': ('Katana', 'Катана', 'Катана', 'katana', 'Katana', 'Katana'),
    'saber': ('Saber', 'Сабля', 'Шабля', 'säbel', 'Sable', 'Sabre'),
    'rapier': ('Rapier', 'Рапира', 'Рапіра', 'rapier', 'Estoque', 'Rapieira'),
    'greatsword': ('Greatsword', 'Двуручный меч', 'Дворучний меч', 'zweihänder', 'Mandoble', 'Montante'),
    'battle_hammer': ('Battle Hammer', 'Боевой молот', 'Бойовий молот', 'streithammer', 'Martillo de batalla', 'Martelo de Batalha'),
    'warhammer': ('Warhammer', 'Клевец', 'Клевець', 'kriegshammer', 'Martillo de guerra', 'Martelo de Guerra'),
    'spear': ('Spear', 'Копьё', 'Спис', 'speer', 'Lanza', 'Lança'),
    'halberd': ('Halberd', 'Алебарда', 'Алебарда', 'hellebarde', 'Alabarda', 'Alabarda'),
    'pike': ('Pike', 'Пика', 'Піка', 'pike', 'Pica', 'Pique'),
    'lance': ('Lance', 'Турнирное копьё', 'Турнірний спис', 'lanze', 'Lanza de justa', 'Lança de Justa'),
    'longbow': ('Longbow', 'Большой лук', 'Довгий лук', 'langbogen', 'Arco largo', 'Arco Longo'),
    'heavy_crossbow': ('Heavy Crossbow', 'Крупный арбалет', 'Важкий арбалет', 'armbrust', 'Ballesta pesada', 'Balestra Larga'),
    'throwing_knife': ('Throwing Knife', 'Метательный нож', 'Метальний ніж', 'wurfmesser', 'Cuchillo arrojadizo', 'Faca de Arremesso'),
    'tomahawk': ('Tomahawk', 'Томагавк', 'Томагавк', 'tomahawk', 'Tomahawk', 'Machadinha'),
    'javelin': ('Javelin', 'Метательное копьё', 'Метальний спис', 'wurfspeer', 'Jabalina', 'Dardo de Arremesso'),
    'boomerang': ('Boomerang', 'Бумеранг', 'Бумеранг', 'bumerang', 'Bumerán', 'Bumerangue'),
    'battleaxe': ('Battleaxe', 'Боевой топор', 'Бойова сокира', 'streitaxt', 'Hacha de batalla', 'Machado de Batalha'),
    'flanged_mace': ('Flanged Mace', 'Пернач', 'Пернач', 'streitkolben', 'Maza con pestañas', 'Maça Flangeada'),
    'glaive': ('Glaive', 'Глефа', 'Глефа', 'glefe', 'Guja', 'Glaive'),
    'quarterstaff': ('Quarterstaff', 'Боевой посох', 'Бойовий посох', 'kampfstab', 'Bastón', 'Bastão'),
}
LANGS = ['en_us', 'ru_ru', 'uk_ua', 'de_de', 'es_es', 'pt_br']


def weapon_name(lang, material, weapon_type):
    i = LANGS.index(lang)
    m, t = MAT_NAMES[material][i], TYPE_NAMES[weapon_type][i]
    if lang == 'en_us':
        if weapon_type in RANGED:
            return '%s-Strengthened %s' % (m, t)
        return '%s %s' % (m, t)
    if lang == 'ru_ru':
        return '%s из %s' % (t, m)
    if lang == 'uk_ua':
        return '%s з %s' % (t, m)
    if lang == 'de_de':
        compound = m + t if not m.endswith('metall') else m + '-' + t.capitalize()
        if weapon_type == 'heavy_crossbow':
            return 'Schwere %s-Armbrust' % m
        if weapon_type == 'longbow':
            return '%sverstärkter Langbogen' % m
        return compound[0].upper() + compound[1:]
    return '%s de %s' % (t, m)


# other strings: key → (en, ru, uk, de, es, pt); en from 1.12.2 where it existed
STRINGS = {
    'itemGroup.%s.arcana' % MOD: ('Spartan Weaponry Arcana', 'Спартанское вооружение: Аркана', 'Spartan Weaponry: Аркана',
                                  'Spartan Weaponry: Arcana', 'Spartan Weaponry: Arcana', 'Spartan Weaponry: Arcana'),
    'item.%s.livingwood_handle' % MOD: ('Livingwood Handle', 'Рукоять из жизнедерева', 'Руків\'я з життєдрева', 'Lebeholzgriff',
                                        'Mango de madera viviente', 'Cabo de Madeira Viva'),
    'item.%s.livingwood_pole' % MOD: ('Livingwood Pole', 'Шест из жизнедерева', 'Держално з життєдрева', 'Lebeholzschaft',
                                      'Asta de madera viviente', 'Haste de Madeira Viva'),
    'item.%s.dreamwood_handle' % MOD: ('Dreamwood Handle', 'Рукоять из дерева мечтаний', 'Руків\'я з мрійдрева', 'Traumholzgriff',
                                       'Mango de madera onírica', 'Cabo de Madeira dos Sonhos'),
    'item.%s.dreamwood_pole' % MOD: ('Dreamwood Pole', 'Шест из дерева мечтаний', 'Держално з мрійдрева', 'Traumholzschaft',
                                     'Asta de madera onírica', 'Haste de Madeira dos Sonhos'),
    'item.%s.dreamwood_crossbow_limb' % MOD: ('Dreamwood Crossbow Limb', 'Плечо арбалета из дерева мечтаний',
                                              'Плече арбалета з мрійдрева', 'Traumholz-Armbrustbogen',
                                              'Pala de ballesta de madera onírica', 'Braço de Balestra de Madeira dos Sonhos'),
    'item.%s.terra_burst' % MOD: ('Terra Burst', 'Терра-вспышка', 'Терра-спалах', 'Terra-Stoß', 'Ráfaga terra', 'Rajada Terra'),
    'tooltip.%s.trait.mana_regenerate' % MOD: ('Mana Repair', 'Починка маной', 'Ремонт маною', 'Mana-Reparatur',
                                               'Reparación con maná', 'Reparo com Mana'),
    'tooltip.%s.trait.mana_regenerate.desc' % MOD: (
        'Repairs the weapon using Botania mana from Mana Tablets',
        'Чинит оружие маной Botania из мана-планшетов',
        'Ремонтує зброю маною Botania з мана-планшетів',
        'Repariert die Waffe mit Botania-Mana aus Manatafeln',
        'Repara el arma con el maná de Botania de las tablillas de maná',
        'Repara a arma usando mana do Botania de Tabuletas de Mana'),
    'tooltip.%s.trait.terra_slash' % MOD: ('Terra Burst', 'Терра-вспышка', 'Терра-спалах', 'Terra-Stoß', 'Ráfaga terra', 'Rajada Terra'),
    'tooltip.%s.trait.terra_slash.desc' % MOD: (
        'Unleashes a damage dealing Mana Burst when swinging the weapon while fully charged. Does not work with Throwing or Ranged Weapons.',
        'Выпускает наносящую урон вспышку маны при взмахе полностью заряженным оружием. Не работает с метательным и стрелковым оружием.',
        'Випускає спалах мани, що завдає шкоди, при замаху повністю зарядженою зброєю. Не діє з метальною та стрілецькою зброєю.',
        'Entfesselt beim Schwingen der voll aufgeladenen Waffe einen schadenden Manastoß. Funktioniert nicht mit Wurf- oder Fernkampfwaffen.',
        'Lanza una ráfaga de maná que hace daño al blandir el arma con la carga completa. No funciona con armas arrojadizas ni a distancia.',
        'Libera uma Rajada de Mana que causa dano ao balançar a arma totalmente carregada. Não funciona com armas de arremesso ou à distância.'),
    'tooltip.%s.trait.pixielated' % MOD: ('Pixielated', 'Пикси', 'Піксі', 'Pixiehaft', 'Pixielado', 'Pixielado'),
    'tooltip.%s.trait.pixielated.desc' % MOD: (
        'Spawns a Pixie when hit by a mob (%s%% chance)',
        'Призывает пикси, когда моб наносит удар (шанс %s%%)',
        'Викликає піксі, коли моб завдає удару (шанс %s%%)',
        'Beschwört eine Pixie, wenn ein Mob dich trifft (%s%% Chance)',
        'Invoca un hada cuando te golpea una criatura (%s%% de probabilidad)',
        'Invoca uma Fada ao ser atingido por uma criatura (%s%% de chance)'),
    'tooltip.%s.trait.warped' % MOD: ('Warped', 'Искажённое', 'Викривлене', 'Verzerrt', 'Deformado', 'Distorcido'),
    'tooltip.%s.trait.warped.desc' % MOD: (
        'This weapon slowly repairs itself',
        'Это оружие медленно чинит само себя',
        'Ця зброя повільно ремонтує сама себе',
        'Diese Waffe repariert sich langsam selbst',
        'Esta arma se repara lentamente sola',
        'Esta arma se repara lentamente sozinha'),
}
# material names (Spartan Weaponry: material.<modid>.<name>, used in tooltips)
for mat, names in MAT_NAMES.items():
    nominative = {'manasteel': ('Манасталь', 'Манасталь'), 'terrasteel': ('Террасталь', 'Терасталь'),
                  'elementium': ('Элементий', 'Елементій'), 'thaumium': ('Таумиум', 'Тауміум'),
                  'void_metal': ('Пустотный металл', 'Порожнечний метал'), 'dawnstone': ('Рассветный камень', 'Світанковий камінь')}[mat]
    STRINGS['material.%s.%s' % (MOD, mat)] = (names[0] if mat != 'void_metal' else 'Void Metal', nominative[0], nominative[1],
                                             names[3], names[4][0].upper() + names[4][1:], names[5])


# Botania lexicon (Patchouli book "botania:lexica_botania", category Tools): the three 1.12.2 entries
LEXICON = {
    'manasteel': ('mana_weaponry', 'botania:main/mana_pool_pickup_lexicon', ['livingwood_handle', 'livingwood_pole']),
    'terrasteel': ('terra_weaponry', 'botania:main/terrasteel_pickup_lexicon', []),
    'elementium': ('elementium_weaponry', 'botania:main/elf_lexicon_pickup', ['dreamwood_handle', 'dreamwood_pole', 'dreamwood_crossbow_limb']),
}
# key → (en, ru, uk, de, es, pt)
LEXICON_STRINGS = {
    'mana_weaponry': ('Manasteel Weaponry', 'Оружие из манастали', 'Зброя з манасталі', 'Manastahlwaffen', 'Armas de manacero', 'Armas de Aço-Mana'),
    'mana_weaponry.text': (
        'With the ability to make more specialized weapons using mundane materials, it makes sense that $(item)Manasteel$(0) would also be suitable to use for these weapons. Using $(item)Manasteel$(0) also allows these specialist weapons to regenerate using mana from $(item)Mana Tablets$(0) or $(item)Mana Pools$(0).$(br2)Making these weapons require $(item)Livingwood Handles$(0) or $(item)Livingwood Poles$(0).',
        'Раз особое оружие можно делать из обычных материалов, то и $(item)манасталь$(0) для него подойдёт. Оружие из $(item)манастали$(0) к тому же чинится маной из $(item)мана-планшетов$(0) или $(item)бассейнов маны$(0).$(br2)Для него нужны $(item)рукояти$(0) или $(item)шесты из жизнедерева$(0).',
        'Якщо особливу зброю можна робити зі звичайних матеріалів, то й $(item)манасталь$(0) для неї згодиться. Зброя з $(item)манасталі$(0) ще й ремонтується маною з $(item)мана-планшетів$(0) або $(item)басейнів мани$(0).$(br2)Для неї потрібні $(item)руків\'я$(0) або $(item)держална з життєдрева$(0).',
        'Wenn sich besondere Waffen aus gewöhnlichen Materialien fertigen lassen, eignet sich auch $(item)Manastahl$(0) dafür. Waffen aus $(item)Manastahl$(0) reparieren sich außerdem mit Mana aus $(item)Manatafeln$(0) oder $(item)Manabecken$(0).$(br2)Dafür braucht man $(item)Lebeholzgriffe$(0) oder $(item)Lebeholzschäfte$(0).',
        'Si se pueden hacer armas especiales con materiales comunes, el $(item)manacero$(0) también sirve. Las armas de $(item)manacero$(0) además se reparan con el maná de las $(item)tablillas de maná$(0) o de las $(item)piscinas de maná$(0).$(br2)Necesitan $(item)mangos$(0) o $(item)astas de madera viviente$(0).',
        'Se armas especiais podem ser feitas com materiais comuns, o $(item)Aço-Mana$(0) também serve. Armas de $(item)Aço-Mana$(0) ainda se regeneram com mana de $(item)Tabuletas de Mana$(0) ou $(item)Piscinas de Mana$(0).$(br2)Elas precisam de $(item)Cabos$(0) ou $(item)Hastes de Madeira Viva$(0).'),
    'terra_weaponry': ('Terrasteel Weaponry', 'Оружие из террастали', 'Зброя з терасталі', 'Terrastahlwaffen', 'Armas de terracero', 'Armas de Aço-Terra'),
    'terra_weaponry.text': (
        'Just like specialized weapons made with $(item)Manasteel$(0), the same weapons can be made using $(item)Terrasteel$(0), which also have the same ability to regenerate using Mana using $(item)Mana Tablets$(0) or $(item)Mana Pools$(0).$(br2)Additionally, just like the $(item)Terra Blade$(0), the weapon can send out a $(thing)Mana Burst$(0), which does damage equal to a melee attack. However, this doesn\'t work with Throwing or Ranged Weapons.',
        'Как и из $(item)манастали$(0), то же оружие можно сделать из $(item)террастали$(0), и оно тоже чинится маной из $(item)мана-планшетов$(0) или $(item)бассейнов маны$(0).$(br2)Кроме того, как и $(item)Терра-клинок$(0), оно выпускает $(thing)вспышку маны$(0), наносящую урон как удар в ближнем бою. С метательным и стрелковым оружием это не работает.',
        'Як і з $(item)манасталі$(0), ту саму зброю можна зробити з $(item)терасталі$(0), і вона теж ремонтується маною з $(item)мана-планшетів$(0) або $(item)басейнів мани$(0).$(br2)Крім того, як і $(item)Терра-клинок$(0), вона випускає $(thing)спалах мани$(0), що завдає шкоди як удар у ближньому бою. З метальною та стрілецькою зброєю це не діє.',
        'Wie aus $(item)Manastahl$(0) lassen sich dieselben Waffen aus $(item)Terrastahl$(0) fertigen; auch sie reparieren sich mit Mana aus $(item)Manatafeln$(0) oder $(item)Manabecken$(0).$(br2)Außerdem senden sie wie die $(item)Terraklinge$(0) einen $(thing)Manastoß$(0) aus, der so viel Schaden wie ein Nahkampfangriff macht. Mit Wurf- oder Fernkampfwaffen geht das nicht.',
        'Igual que con el $(item)manacero$(0), las mismas armas se pueden hacer con $(item)terracero$(0), y también se reparan con el maná de las $(item)tablillas de maná$(0) o de las $(item)piscinas de maná$(0).$(br2)Además, como la $(item)Hoja terra$(0), lanzan una $(thing)ráfaga de maná$(0) que hace tanto daño como un ataque cuerpo a cuerpo. No funciona con armas arrojadizas ni a distancia.',
        'Assim como com o $(item)Aço-Mana$(0), as mesmas armas podem ser feitas com $(item)Aço-Terra$(0), que também se regeneram com mana de $(item)Tabuletas de Mana$(0) ou $(item)Piscinas de Mana$(0).$(br2)Além disso, como a $(item)Lâmina Terra$(0), elas disparam uma $(thing)Rajada de Mana$(0) que causa o dano de um ataque corpo a corpo. Não funciona com armas de arremesso ou à distância.'),
    'elementium_weaponry': ('Elementium Weaponry', 'Оружие из элементия', 'Зброя з елементію', 'Elementiumwaffen', 'Armas de elementio', 'Armas de Elementium'),
    'elementium_weaponry.text': (
        'Making use of the exotic material $(item)Elementium$(0) from $(thing)Alfheim$(0), it is now possible to make the same specialized weaponry that can be made from $(item)Manasteel$(0) and $(item)Terrasteel$(0).$(br2)This time however, $(item)Elementium$(0) weapons require $(item)Dreamwood Handles$(0) and $(item)Dreamwood Poles$(0).',
        'Из экзотического $(item)элементия$(0) из $(thing)Альфхейма$(0) теперь можно сделать то же особое оружие, что и из $(item)манастали$(0) и $(item)террастали$(0).$(br2)Но для оружия из $(item)элементия$(0) нужны $(item)рукояти$(0) и $(item)шесты из дерева мечтаний$(0).',
        'З екзотичного $(item)елементію$(0) з $(thing)Альфгейму$(0) тепер можна зробити ту саму особливу зброю, що й з $(item)манасталі$(0) та $(item)терасталі$(0).$(br2)Але для зброї з $(item)елементію$(0) потрібні $(item)руків\'я$(0) та $(item)держална з мрійдрева$(0).',
        'Aus dem exotischen $(item)Elementium$(0) aus $(thing)Alfheim$(0) lassen sich nun dieselben besonderen Waffen fertigen wie aus $(item)Manastahl$(0) und $(item)Terrastahl$(0).$(br2)Waffen aus $(item)Elementium$(0) brauchen allerdings $(item)Traumholzgriffe$(0) und $(item)Traumholzschäfte$(0).',
        'Con el exótico $(item)elementio$(0) de $(thing)Alfheim$(0) ya se pueden hacer las mismas armas especiales que con $(item)manacero$(0) y $(item)terracero$(0).$(br2)Pero las armas de $(item)elementio$(0) necesitan $(item)mangos$(0) y $(item)astas de madera onírica$(0).',
        'Com o exótico $(item)Elementium$(0) de $(thing)Alfheim$(0), agora é possível fazer as mesmas armas especiais feitas de $(item)Aço-Mana$(0) e $(item)Aço-Terra$(0).$(br2)Desta vez, porém, armas de $(item)Elementium$(0) precisam de $(item)Cabos$(0) e $(item)Hastes de Madeira dos Sonhos$(0).'),
}


def lexicon(out):
    """Patchouli entries (assets/botania/patchouli_books/lexica_botania/en_us/entries/tools): a text page, then the
    recipes two by two (1.12.2: one recipe per page)"""
    for mat, (entry, advancement, parts) in LEXICON.items():
        recipes = []
        for part in parts:
            if part.endswith('_handle') or part.endswith('_pole'):
                wood, kind = part.split('_')
                recipes.append('%s:%s_%s_from_string' % (MOD, wood, kind))
            else:
                recipes.append('%s:%s' % (MOD, part))
        recipes += ['%s:%s_%s' % (MOD, mat, t) for t in TYPES.values()]
        pages = [{'type': 'text', 'text': '%s.lexicon.%s.text' % (MOD, entry)}]
        for i in range(0, len(recipes), 2):
            page = {'type': 'crafting', 'recipe': recipes[i]}
            if i + 1 < len(recipes):
                page['recipe2'] = recipes[i + 1]
            pages.append(page)
        dump(os.path.join(out, 'assets', 'botania', 'patchouli_books', 'lexica_botania', 'en_us', 'entries', 'tools', '%s_%s.json' % (MOD, entry)),
             {'name': '%s.lexicon.%s' % (MOD, entry), 'category': 'botania:tools', 'icon': '%s:%s_greatsword' % (MOD, mat),
              'advancement': advancement, 'pages': pages})
    for key, values in LEXICON_STRINGS.items():
        STRINGS['%s.lexicon.%s' % (MOD, key)] = values


def main(arcana, weaponry, out):
    src = os.path.join(arcana, 'src/main/resources/assets', MOD)
    sw_res = os.path.join(weaponry, 'src/main/resources')
    for sub in ('assets', 'data'):
        shutil.rmtree(os.path.join(out, sub), ignore_errors=True)
    assets = os.path.join(out, 'assets', MOD)
    data = os.path.join(out, 'data')

    # --- textures
    tex_out = os.path.join(assets, 'textures', 'item')
    os.makedirs(tex_out, exist_ok=True)
    for f in sorted(os.listdir(os.path.join(src, 'textures', 'items'))):
        stem = f[:-4]
        m = re.match(r'(crossbow|longbow)_([a-z]+)(?:_(\d|loaded|standby))?$', stem)
        if m:
            kind, mat, state = m.groups()
            prefix = MATERIALS[mat] + '_' + TYPES[kind]
            if state is None:
                state = 'standby'
            elif state.isdigit():
                state = 'pulling_' + state
            name = prefix + '_' + state
        else:
            name = new_id(stem)
        assert name, f
        shutil.copyfile(os.path.join(src, 'textures', 'items', f), os.path.join(tex_out, name + '.png'))

    # --- item models: Spartan Weaponry's iron models with these textures
    sw_models = os.path.join(sw_res, 'assets', SW, 'models', 'item')
    model_out = os.path.join(assets, 'models', 'item')
    os.makedirs(model_out, exist_ok=True)
    for mat in MATERIALS.values():
        for t in TYPES.values():
            for f in sorted(os.listdir(sw_models)):
                if f.startswith('iron_' + t + '.') or f.startswith('iron_' + t + '_'):
                    if f.startswith('iron_' + t + '_') and not re.match(r'iron_%s_(blocking|throwing|pulling_\d|loaded|firing)\.json$' % t, f):
                        continue
                    text = open(os.path.join(sw_models, f), encoding='utf-8').read()
                    text = text.replace('"%s:item/iron_' % SW, '"%s:item/%s_' % (MOD, mat))
                    with open(os.path.join(model_out, mat + f[len('iron'):]), 'w', encoding='utf-8') as o:
                        o.write(text)
    for part in ARCANA_PARTS:
        dump(os.path.join(model_out, part + '.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': '%s:item/%s' % (MOD, part)}})
    dump(os.path.join(model_out, 'terra_burst.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': '%s:item/terrasteel_dagger' % MOD}})

    # --- recipes
    for f in sorted(os.listdir(os.path.join(src, 'recipes'))):
        if f.startswith('_'):
            continue
        name, r = recipe(f[:-5], load(os.path.join(src, 'recipes', f)))
        stem = f[:-5]
        m = re.match(r'(handle|pole)_(livingwood|dreamwood)_(leather|string|wool)$', stem)
        recipe_name = '%s_%s_from_%s' % (m.group(2), m.group(1), m.group(3)) if m else name
        dump(os.path.join(data, MOD, 'recipe', recipe_name + '.json'), r)

    # --- tags
    weapons = {t: [] for t in TYPES.values()}
    for mat in MATERIALS.values():
        for t in TYPES.values():
            weapons[t].append('%s:%s_%s' % (MOD, mat, t))
    for t, items in weapons.items():
        dump(os.path.join(data, SW, 'tags', 'item', TYPE_TAGS[t] + '.json'), {'values': items})
    melee = [i for t, items in weapons.items() if t not in THROWING + RANGED for i in items]
    throwing = [i for t in THROWING for i in weapons[t]]
    ranged = [i for t in RANGED for i in weapons[t]]
    ench = os.path.join(data, 'minecraft', 'tags', 'item', 'enchantable')
    dump(os.path.join(ench, 'sword.json'), {'values': melee})
    dump(os.path.join(ench, 'sharp_weapon.json'), {'values': melee})
    dump(os.path.join(ench, 'bow.json'), {'values': weapons['longbow']})
    dump(os.path.join(ench, 'crossbow.json'), {'values': weapons['heavy_crossbow']})
    dump(os.path.join(ench, 'durability.json'), {'values': melee + throwing + ranged})
    sw_ench = os.path.join(data, SW, 'tags', 'item', 'enchantable')
    dump(os.path.join(sw_ench, 'throwing_weapon.json'), {'values': throwing})
    dump(os.path.join(sw_ench, 'heavy_crossbow.json'), {'values': weapons['heavy_crossbow']})
    dump(os.path.join(sw_ench, 'boomerang.json'), {'values': weapons['boomerang']})
    dump(os.path.join(data, 'c', 'tags', 'item', 'tools', 'melee_weapon.json'), {'values': melee + throwing})
    dump(os.path.join(data, 'c', 'tags', 'item', 'tools', 'ranged_weapon.json'), {'values': ranged})
    for mat, traits in TRAITS.items():
        dump(os.path.join(data, MOD, 'tags', SW, 'weapon_traits', 'materials', mat + '.json'),
             {'values': ['%s:%s' % (MOD, t) for t in traits]})
    # Better Combat (Spartan Weaponry ships the same for its weapons)
    for t, items in weapons.items():
        for item in items:
            dump(os.path.join(data, MOD, 'weapon_attributes', item.split(':')[1] + '.json'), {'parent': '%s:base/%s' % (SW, t)})

    lexicon(out)

    # --- lang
    old = read_lang(os.path.join(src, 'lang', 'en_us.lang'))
    for i, lang in enumerate(LANGS):
        d = {}
        for mat in MATERIALS.values():
            for t in TYPES.values():
                d['item.%s.%s_%s' % (MOD, mat, t)] = weapon_name(lang, mat, t)
        for key, values in STRINGS.items():
            d[key] = values[i]
        dump(os.path.join(assets, 'lang', lang + '.json'), d)
    # the 1.12.2 English strings that are kept (checks that nothing of them got lost)
    en = load(os.path.join(assets, 'lang', 'en_us.json'))
    for k in ('item.spartanweaponryarcana:handle_livingwood.name', 'tooltip.spartanweaponryarcana:terra_slash.desc'):
        assert old[k] in en.values(), k


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2], sys.argv[3])
