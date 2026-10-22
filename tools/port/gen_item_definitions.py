#!/usr/bin/env python3
"""Spartan Weaponry: Arcana, the generator of Spartan Weaponry 1.21.11 (tools/port there) for this namespace.
1.21.4+ client item definitions (assets/spartanweaponryarcana/items/<id>.json) from the 1.21.1 item models and their
overrides. The overrides are evaluated as 1.21.1 did (the last override whose predicates all hold wins) for every
combination of the boolean properties, the one numeric property of an item becomes a range_dispatch:
  blocking/throwing/empty/charged -> spartanweaponry:<same> conditions, pulling -> minecraft:using_item,
  pull -> spartanweaponry:pull, arrow -> spartanweaponry:ammo.
The spartanweaponry:oil_coated_item loader (1.21.1: a coating layer tinted by the oil, see OilCoatingColours) becomes
the spartanweaponry:oiled condition: when oiled, a composite of the model and a coating model (the parent of the
variant with the coating texture as layer0, tinted by spartanweaponry:oil); not oiled, the plain model and nothing
more to draw. Tipped projectiles and the oil bottle get their layer 1 tint, the mob heads the vanilla head renderer.
The vanilla swords (assets/minecraft/models/item, oilable too) get minecraft item definitions the same way.
Run from the repository root on the 1.21.1 models; writes the items folders and the coating models, drops
loader/coating/overrides."""
import itertools, json, os

NS = 'spartanweaponryarcana'
ASSETS = 'common/src/main/resources/assets/' + NS
MODELS = os.path.join(ASSETS, 'models/item')
ITEMS = os.path.join(ASSETS, 'items')
VANILLA_MODELS = 'common/src/main/resources/assets/minecraft/models/item'
VANILLA_ITEMS = 'common/src/main/resources/assets/minecraft/items'
LOADER = 'spartanweaponry:oil_coated_item'
BOOLS = {'spartanweaponry:throwing': 'spartanweaponry:throwing', 'spartanweaponry:blocking': 'spartanweaponry:blocking',
         'spartanweaponry:empty': 'spartanweaponry:empty', 'spartanweaponry:charged': 'spartanweaponry:charged',
         'spartanweaponry:pulling': 'minecraft:using_item'}
RANGES = {'spartanweaponry:pull': 'spartanweaponry:pull', 'spartanweaponry:arrow': 'spartanweaponry:ammo'}
TIPPED = ['tipped_wooden_arrow', 'tipped_copper_arrow', 'tipped_iron_arrow', 'tipped_diamond_arrow', 'tipped_netherite_arrow',
          'tipped_bolt', 'tipped_copper_bolt', 'tipped_diamond_bolt', 'tipped_netherite_bolt']
HEADS = ['blaze_head', 'enderman_head', 'spider_head', 'cave_spider_head', 'zombified_piglin_head', 'husk_head',
         'stray_skull', 'drowned_head', 'illager_head', 'witch_head']
# ExtendedSkullBlock.Types, by item
HEAD_KIND = {'zombified_piglin_head': 'zombie_piglin', 'stray_skull': 'stray'}

def load(model_id):
    namespace, path = model_id.split(':', 1)
    folder = MODELS if namespace == NS else VANILLA_MODELS
    path = os.path.join(folder, path.split('/', 1)[1] + '.json')
    # Spartan Weaponry's own models (spartanweaponry:item/base/..._empty): plain models, nothing to read here
    return json.load(open(path)) if namespace in (NS, 'minecraft') and os.path.exists(path) else {}

def save(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(data, open(path, 'w'), indent=2)
    open(path, 'a').write('\n')

coatings = {}

def coating_model(parent, texture):
    name = NS + ':item/coating_layer/' + parent.rsplit('/', 1)[1]
    if coatings.setdefault(name, (parent, texture)) != (parent, texture):
        name += '_' + texture.rsplit('/', 1)[1]
        coatings[name] = (parent, texture)
    return name

def leaf(item, model_id):
    data = load(model_id)
    out = {'type': 'minecraft:model', 'model': model_id}
    if item in TIPPED:
        out['tints'] = [{'type': 'minecraft:constant', 'value': -1}, {'type': 'spartanweaponry:potion'}]
    elif item == 'weapon_oil':
        out['tints'] = [{'type': 'minecraft:constant', 'value': -1}, {'type': 'spartanweaponry:oil'}]
    if data.get('loader') != LOADER:
        return out
    coating = {'type': 'minecraft:model', 'model': coating_model(data['parent'], data['textures']['coating']),
               'tints': [{'type': 'spartanweaponry:oil'}]}
    return {'type': 'minecraft:condition', 'property': 'spartanweaponry:oiled',
            'on_true': {'type': 'minecraft:composite', 'models': [out, coating]}, 'on_false': out}

def winner(overrides, base, values):
    model = base
    for o in overrides:
        if all(values.get(k, 0) >= v for k, v in o['predicate'].items()):
            model = o['model']
    return model

def by_range(item, overrides, base, values, prop):
    if prop is None:
        return leaf(item, winner(overrides, base, values))
    thresholds = sorted({o['predicate'][prop] for o in overrides if prop in o['predicate']} | {0.0})
    entries = [(t, winner(overrides, base, dict(values, **{prop: t}))) for t in thresholds]
    fallback = winner(overrides, base, dict(values, **{prop: -1.0}))
    if all(m == fallback for _, m in entries):
        return leaf(item, fallback)
    return {'type': 'minecraft:range_dispatch', 'property': RANGES[prop],
            'entries': [{'threshold': t, 'model': leaf(item, m)} for t, m in entries], 'fallback': leaf(item, fallback)}

def tree(item, overrides, base, bools, prop, values):
    if not bools:
        return by_range(item, overrides, base, values, prop)
    on = tree(item, overrides, base, bools[1:], prop, dict(values, **{bools[0]: 1.0}))
    off = tree(item, overrides, base, bools[1:], prop, dict(values, **{bools[0]: 0.0}))
    if on == off:
        return off
    return {'type': 'minecraft:condition', 'property': BOOLS[bools[0]], 'on_true': on, 'on_false': off}

def definition(item, namespace=NS):
    base = namespace + ':item/' + item
    if item in HEADS:
        kind = 'spartanweaponry:' + HEAD_KIND.get(item, item.rsplit('_', 1)[0])
        return {'model': {'type': 'minecraft:special', 'base': 'minecraft:item/template_skull',
                          'model': {'type': 'minecraft:head', 'kind': kind}}}
    overrides = load(base).get('overrides', [])
    keys = {k for o in overrides for k in o['predicate']}
    unknown = keys - set(BOOLS) - set(RANGES)
    assert not unknown, (item, unknown)
    ranges = [k for k in RANGES if k in keys]
    assert len(ranges) <= 1, (item, ranges)
    bools = [k for k in BOOLS if k in keys]
    return {'model': tree(item, overrides, base, bools, ranges[0] if ranges else None, {})}

def items():
    lang = json.load(open(os.path.join(ASSETS, 'lang/en_us.json')))
    ids = {k.split('.')[2] for k in lang if k.count('.') == 2 and k.split('.')[0] in ('item', 'block') and k.split('.')[1] == NS}
    # the tipped projectiles are named per potion (item.spartanweaponry.tipped_bolt.effect.<potion>)
    ids |= set(TIPPED)
    return sorted(i for i in ids if os.path.exists(os.path.join(MODELS, i + '.json')))

ids = items()
for item in ids:
    save(os.path.join(ITEMS, item + '.json'), definition(item))
vanilla = sorted(f[:-5] for f in os.listdir(VANILLA_MODELS) if f.endswith('.json')) if os.path.isdir(VANILLA_MODELS) else []
for item in vanilla:
    save(os.path.join(VANILLA_ITEMS, item + '.json'), definition(item, 'minecraft'))
for name, (parent, texture) in coatings.items():
    save(os.path.join(MODELS, name.split('/', 1)[1] + '.json'), {'parent': parent, 'textures': {'layer0': texture}})
for path in [os.path.join(d, f) for d in (MODELS, VANILLA_MODELS) if os.path.isdir(d) for f in os.listdir(d) if f.endswith('.json')]:
    data = json.load(open(path))
    changed = data.pop('overrides', None) is not None
    if data.pop('loader', None) == LOADER:
        data.get('textures', {}).pop('coating', None)
        changed = True
    if changed:
        save(path, data)
print(len(ids), 'item definitions,', len(vanilla), 'vanilla,', len(coatings), 'coating models')
