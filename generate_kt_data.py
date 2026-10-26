import json

with open("weapons_data.json", "r", encoding="utf-8") as f:
    weapons = json.load(f)

kt_code = """package com.example.militaryid.data

data class Weapon(
    val id: String,
    val country: String,
    val countryLabel: String,
    val category: String,
    val categoryLabel: String,
    val nameJa: String,
    val nameEn: String,
    val natoOrAlt: String,
    val type: String,
    val appearance: String,
    val features: List<String>,
    val specs: Map<String, String>,
    val imageUrl: String,
    val wikiTitle: String,
    val licenseAuthor: String,
    val licenseType: String,
    val sourceUrl: String
)

object WeaponsRepository {
    val weapons: List<Weapon> = listOf(
"""

def esc(s):
    if not s:
        return '""'
    return '"' + str(s).replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n') + '"'

items = []
for w in weapons:
    feats = ", ".join(esc(f) for f in w.get("features", []))
    specs = ", ".join(f"{esc(k)} to {esc(v)}" for k, v in w.get("specs", {}).items())
    lic = w.get("license", {})
    
    item_str = f"""        Weapon(
            id = {esc(w.get("id"))},
            country = {esc(w.get("country"))},
            countryLabel = {esc(w.get("country_label"))},
            category = {esc(w.get("category"))},
            categoryLabel = {esc(w.get("category_label"))},
            nameJa = {esc(w.get("name_ja"))},
            nameEn = {esc(w.get("name_en"))},
            natoOrAlt = {esc(w.get("nato_or_alt"))},
            type = {esc(w.get("type"))},
            appearance = {esc(w.get("appearance"))},
            features = listOf({feats}),
            specs = mapOf({specs}),
            imageUrl = {esc(w.get("image_url"))},
            wikiTitle = {esc(w.get("wiki_title"))},
            licenseAuthor = {esc(lic.get("author", "Wikimedia Commons"))},
            licenseType = {esc(lic.get("license", "CC BY-SA"))},
            sourceUrl = {esc(lic.get("source_url", "https://en.wikipedia.org/wiki/" + (w.get("wiki_title") or "")))}
        )"""
    items.append(item_str)

kt_code += ",\n".join(items)
kt_code += """
    )
}
"""

with open("android_app/app/src/main/kotlin/com/example/militaryid/data/WeaponsRepository.kt", "w", encoding="utf-8") as f:
    f.write(kt_code)

print("Generated WeaponsRepository.kt successfully with", len(weapons), "weapons!")
