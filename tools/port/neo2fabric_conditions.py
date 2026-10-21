"""Adds "fabric:load_conditions" next to "neoforge:conditions" in data JSON (both loaders read the same files).
Usage: python -I neo2fabric_conditions.py <resources dir> <mod id> [custom condition ids...]"""
import json, sys, pathlib

custom = set(sys.argv[3:])
# NeoForge "not tag_empty" -> <modid>:tag_not_empty (Fabric's fabric:tags_populated is true for an empty tag file)
tag_not_empty = sys.argv[2] + ':tag_not_empty'

def conv(c):
    t = c['type']
    if t == 'neoforge:mod_loaded':
        return {'condition': 'fabric:all_mods_loaded', 'values': [c['modid']]}
    if t == 'neoforge:not':
        inner = c['value']
        if inner['type'] == 'neoforge:tag_empty':
            return {'condition': tag_not_empty, 'tag': inner['tag']}
        return {'condition': 'fabric:not', 'value': conv(inner)}
    if t == 'neoforge:tag_empty':
        return {'condition': 'fabric:not', 'value': {'condition': tag_not_empty, 'tag': c['tag']}}
    if t == 'neoforge:and':
        return {'condition': 'fabric:and', 'values': [conv(v) for v in c['values']]}
    if t == 'neoforge:or':
        return {'condition': 'fabric:or', 'values': [conv(v) for v in c['values']]}
    if t == 'neoforge:true':
        return {'condition': 'fabric:true'}
    if t == 'neoforge:false':
        return {'condition': 'fabric:not', 'value': {'condition': 'fabric:true'}}
    if t in custom:
        out = {'condition': t}
        out.update({k: v for k, v in c.items() if k != 'type'})
        return out
    raise ValueError('unknown condition ' + t)

n = 0
for p in pathlib.Path(sys.argv[1]).rglob('*.json'):
    raw = p.read_text(encoding='utf-8')
    if '"neoforge:conditions"' not in raw:
        continue
    data = json.loads(raw)
    fab = [conv(c) for c in data['neoforge:conditions']]
    if data.get('fabric:load_conditions') == fab:
        continue
    out = {}
    for k, v in data.items():
        if k == 'fabric:load_conditions':
            continue
        out[k] = v
        if k == 'neoforge:conditions':
            out['fabric:load_conditions'] = fab
    nl = '\r\n' if '\r\n' in raw else '\n'
    p.write_text(json.dumps(out, indent=2, ensure_ascii=False).replace('\n', nl) + nl, encoding='utf-8')
    n += 1
print('converted', n)
