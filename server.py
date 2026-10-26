import http.server
import socketserver
import webbrowser
import os
import sys
import socket

PORT = 8080
os.chdir(os.path.dirname(os.path.abspath(__file__)))

Handler = http.server.SimpleHTTPRequestHandler
Handler.extensions_map.update({
    ".js": "application/javascript",
    ".json": "application/json",
    ".css": "text/css",
    ".html": "text/html",
    ".apk": "application/vnd.android.package-archive"
})

def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "localhost"

def run():
    # ポートの再利用を許可 (SO_REUSEADDR)
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("", PORT), Handler) as httpd:
        local_ip = get_local_ip()
        local_url = f"http://localhost:{PORT}/index.html"
        mobile_url = f"http://{local_ip}:{PORT}/index.html"
        apk_url = f"http://{local_ip}:{PORT}/military-id-app.apk"
        
        print("=" * 65)
        print("  兵器識別暗記アプリ サーバー起動中")
        print("=" * 65)
        print(f"  [PCブラウザ]       {local_url}")
        print(f"  [スマホブラウザ]    {mobile_url}")
        print(f"  [Android APK直接DL] {apk_url}")
        print("-" * 65)
        print("  ※スマホをPCと同じWi-Fiに接続してアクセスしてください。")
        print("  ※PC画面右上の『スマホ接続アイコン』からQRコードも出せます。")
        print("=" * 65)
        print("  終了するには Ctrl+C を押してください。\n")
        
        try:
            webbrowser.open(local_url)
        except Exception:
            pass
            
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\nサーバーを停止しました。")

if __name__ == "__main__":
    run()
