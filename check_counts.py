import json
weapons = json.load(open('weapons_data.json', encoding='utf-8'))
counts = {}
for w in weapons:
    key = f"{w['country_label']} x {w['category_label']}"
    counts[key] = counts.get(key, 0) + 1
for k, v in sorted(counts.items()):
    print(f"{k}: {v}件")
print(f"合計: {len(weapons)}件")
