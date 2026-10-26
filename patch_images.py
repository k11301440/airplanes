import urllib.request
import urllib.parse
import json
import re

HEADERS = {'User-Agent': 'MilitaryIDApp/1.0 (contact@military-id-app.internal)'}

# Commonsのファイル名から直接高品質サムネイルURLとライセンス情報を引く関数
def get_file_info_from_commons(file_title):
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

# 個別に確認した正確なCommonsファイル
direct_files = {
    "ru-su30sm": "Russian Air Force, RF-81760, Sukhoi Su-30SM (51524684578).jpg",
    "ru-ak12": "5,45mm AK-12 6P70 assault rifle at Military-technical forum ARMY-2016 01.jpg",
    "ru-pkp": "PKP Pecheneg Conscript day in Moscow 2011.jpg",
    "ru-svd": "Dragunov SVD Rifle.JPEG",
    "ru-asval": "AS Val (541-03).jpg",
    "ru-fab1500umpc": "Disposal of an air-dropped bomb FAB-1500 in Selydove, 2024-03-16 (02).jpg",
    "cn-qbu88": "Qbu-88-0.jpg",
    "cn-pcl181": "PCL-181 155mm howitzer truck at Military Museum of Chinese People's Revolution (20250917).jpg",
    "cn-yj18": "YJ-18 ASCM.jpg",
    "cn-pl10": "PL-10 AAM.jpg",
    "cn-ls6": "LS-6 500-kg precision guided glide bomb.jpg",
    "cn-lt2": "LT-2 laser guided bomb.jpg"
}

with open("weapons_data.json", "r", encoding="utf-8") as f:
    weapons = json.load(f)

for item in weapons:
    item_id = item["id"]
    if item_id in direct_files:
        filename = direct_files[item_id]
        print(f"Fetching direct file for {item_id}: {filename}...")
        thumb, lic = get_file_info_from_commons(filename)
        if thumb:
            item["image_url"] = thumb
            item["license"] = lic
            print(f" -> OK: {thumb[:60]}...")
        else:
            print(f" -> FAILED: {filename}")

# それでも画像が無い場合のフォールバック検索
for item in weapons:
    if not item.get("image_url"):
        print(f"Need search for {item['id']} ({item['name_ja']})...")
        # 簡易検索
        q = item.get("name_en") or item.get("wiki_title")
        search_url = f"https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch={urllib.parse.quote(q)}&gsrnamespace=6&gsrlimit=3&prop=imageinfo&iiprop=url|extmetadata&iiurlwidth=800&format=json"
        req = urllib.request.Request(search_url, headers=HEADERS)
        try:
            with urllib.request.urlopen(req, timeout=10) as resp:
                sdata = json.loads(resp.read().decode('utf-8'))
            spages = sdata.get('query', {}).get('pages', {})
            for pid, sp in spages.items():
                title = sp.get('title')
                if any(title.lower().endswith(ext) for ext in ['.jpg', '.jpeg', '.png']):
                    sinfo = sp.get('imageinfo', [{}])[0]
                    item["image_url"] = sinfo.get('thumburl') or sinfo.get('url')
                    meta = sinfo.get('extmetadata', {})
                    lic = meta.get('LicenseShortName', {}).get('value', 'CC BY-SA')
                    author = re.sub('<[^<]+?>', '', meta.get('Artist', {}).get('value', 'Commons Contributor')).strip()[:60]
                    item["license"] = {"author": author, "license": lic, "source_url": sinfo.get('descriptionurl', '')}
                    print(f" -> Recovered via search: {title}")
                    break
        except Exception as ex:
            print(f" -> Search error: {ex}")

with open("weapons_data.json", "w", encoding="utf-8") as f:
    json.dump(weapons, f, ensure_ascii=False, indent=2)

print("Finished updating weapons_data.json!")
