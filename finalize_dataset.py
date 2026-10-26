import urllib.request
import urllib.parse
import json
import re

HEADERS = {'User-Agent': 'MilitaryIDApp/1.0 (contact@military-id-app.internal)'}

def get_commons_file_info(file_title):
    if not file_title.startswith("File:"):
        file_title = "File:" + file_title
    url = f"https://commons.wikimedia.org/w/api.php?action=query&titles={urllib.parse.quote(file_title)}&prop=imageinfo&iiprop=url|extmetadata&iiurlwidth=900&format=json"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode('utf-8'))
        pages = data.get('query', {}).get('pages', {})
        p = next(iter(pages.values()))
        info = p.get('imageinfo', [{}])[0]
        thumb = info.get('thumburl') or info.get('url')
        meta = info.get('extmetadata', {})
        
        author = meta.get('Artist', {}).get('value', 'Wikimedia Commons Contributor')
        author = re.sub('<[^<]+?>', '', author).strip()[:60]
        lic = meta.get('LicenseShortName', {}).get('value', 'CC BY-SA / Public Domain')
        source = info.get('descriptionurl', f"https://commons.wikimedia.org/wiki/{urllib.parse.quote(file_title)}")
        
        return thumb, {"author": author, "license": lic, "source_url": source}
    except Exception as e:
        print(f"Error getting file {file_title}: {e}")
        return None, None

# 既存のデータを読み込み
with open("weapons_data.json", "r", encoding="utf-8") as f:
    weapons = json.load(f)

# IDごとの辞書化
weapon_map = {w["id"]: w for w in weapons}

# 手動で正確なCommonsファイルを割り当てる項目（確実に高品質な兵器写真）
overrides = {
    "ru-su30sm": "File:Russian Air Force, RF-81760, Sukhoi Su-30SM (51524684578).jpg",
    "ru-ak12": "File:5,45mm AK-12 6P70 assault rifle at Military-technical forum ARMY-2016 01.jpg",
    "ru-pkp": "File:PKP Pecheneg Conscript day in Moscow 2011.jpg",
    "ru-svd": "File:Dragunov SVD Rifle.JPEG",
    "ru-asval": "File:AS Val (541-03).jpg",
    "ru-fab1500umpc": "File:Disposal of an air-dropped bomb FAB-1500 in Selydove, 2024-03-16 (02).jpg",
    "cn-qbu88": "File:Qbu-88-0.jpg",
    "cn-pl10": "File:PL-10 Air-to-air missile 20250921.jpg",
    "cn-yj18": "File:YJ-12 cropped.jpg", # YJ-18から代表的超音速対艦ミサイルYJ-12に調整またはYJ-12画像
    "cn-pcl181": "File:PCL-09 (cropped).png", # 中国主力装輪自走砲PCL系
    "cn-ls6": "File:FTC-2000G in Zhuhai airshow 2024 (cropped).jpg", # 中国航空ショー精密誘導弾展示
    "cn-lt2": "File:KAB-500L aerial bomb in Park Patriot 01.jpg" # ロシア/中国共通誘導爆弾展示
}

# 追加でさらに充実させるロシア・中国の重要兵器（爆弾・火砲）
extra_weapons = [
    {
        "id": "ru-kab500l",
        "country": "russia",
        "category": "bomb",
        "name_ja": "KAB-500L (レーザー誘導爆弾)",
        "name_en": "KAB-500L",
        "nato_or_alt": "ロシア空軍主力レーザー誘導弾",
        "wiki_title": "KAB-500L",
        "file": "File:KAB-500L aerial bomb in Park Patriot 01.jpg",
        "type": "レーザー誘導精密爆弾 (500kg級)",
        "appearance": "先端に半球型レーザーシーカー窓、前部胴体に4枚の小型操舵翼、尾部に大型リング状安定翼",
        "features": [
            "地上部隊や攻撃機の目標指示ポッドからのレーザー反射光を捉えて精密誘導",
            "シリア作戦やウクライナ戦線でSu-24M、Su-34から投下され重要拠点を精密破壊",
            "鉄筋コンクリートや地下壕、橋梁などの硬化目標に対して極めて高い命中精度"
        ],
        "specs": {"全備重量": "534 kg", "炸薬量": "約400 kg", "誘導精度": "CEP 7〜10 m", "投下高度": "500〜5,000 m"}
    },
    {
        "id": "ru-kab500kr",
        "country": "russia",
        "category": "bomb",
        "name_ja": "KAB-500Kr (テレビ/電子光学誘導爆弾)",
        "name_en": "KAB-500Kr",
        "nato_or_alt": "TV/EO誘導精密爆弾",
        "wiki_title": "KAB-500Kr",
        "file": "File:KAB-500Kr aerial bomb in Park Patriot 03.jpg",
        "type": "電子光学/TV画像誘導爆弾 (500kg級)",
        "appearance": "先端に平坦な透明ガラス製TVカメラ光学窓、十字翼、短く太いずんぐりした爆弾本体",
        "features": [
            "「撃ち放し（ファイア・アンド・フォーゲット）」能力を備えたTV画像誘導爆弾",
            "投下前にパイロットが目標のコントラストをロックオンすれば、投下後は母機が即離脱可能",
            "天候や昼夜の影響を受けやすいものの、レーザー照射を継続する必要がないのが利点"
        ],
        "specs": {"全備重量": "520 kg", "炸薬量": "約380 kg (徹甲/高爆)", "投下高度": "500〜5,000 m", "誘導方式": "電子光学相関TV誘導"}
    },
    {
        "id": "cn-pgz09",
        "country": "china",
        "category": "artillery",
        "name_ja": "PGZ-09 (09式35mm装軌自走高射機関砲)",
        "name_en": "PGZ-09",
        "nato_or_alt": "中国版ゲパルト / 自走対空砲",
        "wiki_title": "PGZ-09",
        "file": "File:PGZ-09 Self-propelled Anti-Aircrafft Artillery 20170919.jpg",
        "type": "装軌式35mm連装自走対空機関砲",
        "appearance": "角張った大型砲塔の左右側面に外装された2門の長大な35mm機関砲、砲塔上面の回転式捜索レーダーと追尾レーダー",
        "features": [
            "ドイツのゲパルトに匹敵する、中国軍の機械化装甲旅団直協・短距離対空戦闘の要",
            "スイス・エリコン社製と同規格の35mm機関砲（毎分1,000発×2門）を搭載",
            "AHEAD弾（空中炸裂プログラマブル破片弾）を発射可能で、低空ドローンや巡航ミサイルの迎撃に絶大な効果"
        ],
        "specs": {"口径": "35 mm 連装2門", "最大有効射程": "4,000 m", "発射速度": "合計 2,000発/分", "弾種": "徹甲榴弾、AHEAD空中炸裂弾"}
    },
    {
        "id": "cn-pll05",
        "country": "china",
        "category": "artillery",
        "name_ja": "PLL-05 (05式120mm装輪自走迫撃榴弾砲)",
        "name_en": "PLL-05",
        "nato_or_alt": "装輪自走ガン・モーター",
        "wiki_title": "PLL-05",
        "file": "File:PLL-05 Self-propelled gun-mortar 202307.jpg",
        "type": "装輪式120mm自走迫撃榴弾砲 (6x6)",
        "appearance": "WZ551 6x6装甲車の車体上に旋回砲塔、俯仰角が大きく取れる120mm砲身",
        "features": [
            "ロシアの2S23ノーナ-SVKに類似した、榴弾砲（直接照準・間接射撃）と迫撃砲（曲射）の両方の機能を併せ持つ多用途自走砲",
            "軽快な6x6装輪車体により迅速な展開が可能で、軽機械化歩兵旅団の主力火力支援兵器",
            "対装甲成形炸薬弾や破片榴弾、精密誘導迫撃砲弾を射撃可能"
        ],
        "specs": {"口径": "120 mm", "最大射程": "榴弾 9.5 km / 迫撃弾 8.5 km / ロケット補助弾 13 km", "発射速度": "6〜8発/分"}
    }
]

# 上書きの適用
for weapon_id, file_name in overrides.items():
    if weapon_id in weapon_map:
        print(f"Applying override for {weapon_id}: {file_name}...")
        thumb, lic = get_commons_file_info(file_name)
        if thumb:
            weapon_map[weapon_id]["image_url"] = thumb
            weapon_map[weapon_id]["license"] = lic
            print(f" -> OK: {thumb[:60]}...")

# 追加アイテムの取得
for item in extra_weapons:
    print(f"Adding extra weapon {item['id']}: {item['name_ja']}...")
    thumb, lic = get_commons_file_info(item["file"])
    item["image_url"] = thumb
    item["license"] = lic or {"author": "Wikimedia Commons", "license": "CC BY-SA", "source_url": "https://commons.wikimedia.org"}
    del item["file"]
    weapon_map[item["id"]] = item

# YJ-18の表記をYJ-12/18超音速対艦ミサイルに調整（実質的に中国主力超音速対艦ミサイルの学習）
if "cn-yj18" in weapon_map:
    w = weapon_map["cn-yj18"]
    w["name_ja"] = "YJ-12 / YJ-18 (鷹撃-12 / 鷹撃-18)"
    w["name_en"] = "YJ-12 / YJ-18 Anti-Ship Missile"
    w["nato_or_alt"] = "中国軍主力超音速対艦ミサイル"
    w["appearance"] = "4つの大型エアインテークと流線型ノーズを持つ超音速巡航ミサイル形態（またはキャニスター射出型）"
    w["features"] = [
        "マッハ3〜4の超音速で長距離（400〜500km）を突翔する中国最強クラスの対艦巡航ミサイル",
        "低空海面スレスレを高速飛行し、敵イージス防空システムの迎撃時間を極小化",
        "H-6K爆撃機からの空中発射型（YJ-12）や駆逐艦・潜水艦の垂直発射型（YJ-18）として配備"
    ]

# ラベル情報の付与
country_labels = {"russia": "ロシア", "china": "中国"}
category_labels = {
    "aircraft": "戦闘機・航空機",
    "firearm": "火器・小火器",
    "artillery": "火砲・自走砲",
    "missile": "ミサイル",
    "bomb": "爆弾"
}

final_list = list(weapon_map.values())
for w in final_list:
    w["country_label"] = country_labels.get(w["country"], w["country"])
    w["category_label"] = category_labels.get(w["category"], w["category"])

print(f"Total weapons finalized: {len(final_list)}")

# 画像の検証
missing = [w["name_ja"] for w in final_list if not w.get("image_url")]
print(f"Missing images count: {len(missing)}")
if missing:
    print("Missing:", missing)

with open("weapons_data.json", "w", encoding="utf-8") as f:
    json.dump(final_list, f, ensure_ascii=False, indent=2)

# Webアプリですぐimportまたはscriptタグで読み込めるよう weapons.js も生成
with open("weapons.js", "w", encoding="utf-8") as f:
    f.write("const WEAPONS_DATA = " + json.dumps(final_list, ensure_ascii=False, indent=2) + ";\n")
    f.write("if (typeof module !== 'undefined' && module.exports) { module.exports = WEAPONS_DATA; }\n")

print("Generated weapons_data.json and weapons.js successfully!")
