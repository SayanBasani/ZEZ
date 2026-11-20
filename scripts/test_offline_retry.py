import xml.etree.ElementTree as ET
import subprocess
import time
import re

DEV6 = "emulator-5554"
DEV7 = "emulator-5556"

def adb_shell(dev, cmd):
    res = subprocess.run(["adb", "-s", dev, "shell"] + cmd, capture_output=True, text=True)
    return res.stdout.strip()

def dump_ui(dev):
    subprocess.run(["adb", "-s", dev, "shell", "uiautomator", "dump", "/sdcard/dump.xml"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    out = subprocess.check_output(["adb", "-s", dev, "shell", "cat", "/sdcard/dump.xml"]).decode("utf-8", errors="ignore")
    return ET.fromstring(out)

def get_nodes(root):
    nodes = []
    for el in root.iter():
        nodes.append({
            "rid": el.attrib.get("resource-id", ""),
            "text": el.attrib.get("text", ""),
            "bounds": el.attrib.get("bounds", ""),
            "desc": el.attrib.get("content-desc", "")
        })
    return nodes

def find_node(nodes, rid_contains=None, text_contains=None):
    for n in nodes:
        if rid_contains and rid_contains in n["rid"]:
            return n
        if text_contains and text_contains in n["text"]:
            return n
    return None

def tap_node(dev, node):
    m = re.findall(r'\[(\d+),(\d+)\]', node["bounds"])
    x = (int(m[0][0]) + int(m[1][0])) // 2
    y = (int(m[0][1]) + int(m[1][1])) // 2
    adb_shell(dev, ["input", "tap", str(x), str(y)])

def send_chat_text(dev, text):
    root = dump_ui(dev)
    nodes = get_nodes(root)
    inp = find_node(nodes, rid_contains="et_chat_message_input")
    if not inp:
        print(f"[{dev}] Input box not found!")
        return False
    tap_node(dev, inp)
    time.sleep(0.3)
    escaped = text.replace(" ", "%s").replace("'", "\\'").replace('"', '\\"')
    adb_shell(dev, ["input", "text", escaped])
    time.sleep(0.3)
    
    root = dump_ui(dev)
    nodes = get_nodes(root)
    send_btn = find_node(nodes, rid_contains="btn_chat_send_container") or find_node(nodes, rid_contains="btn_chat_send")
    if send_btn:
        tap_node(dev, send_btn)
    else:
        adb_shell(dev, ["input", "tap", "1000", "2300"])
    return True

print("=== Starting Offline Send & Automatic Retry Test ===")

# Step 1: Disable mobile data & wifi on Pixel 6
print("\n[Step 1] Disabling internet on Pixel 6...")
adb_shell(DEV6, ["svc", "data", "disable"])
adb_shell(DEV6, ["svc", "wifi", "disable"])
time.sleep(1.0)

# Step 2: Send message while offline
offline_msg = f"Offline-retry-test-{int(time.time())}"
print(f"\n[Step 2] Sending message on Pixel 6 while OFFLINE: '{offline_msg}'")
send_chat_text(DEV6, offline_msg)
time.sleep(2.0)

# Verify message is on Pixel 6 screen in pending state
root6 = dump_ui(DEV6)
nodes6 = get_nodes(root6)
msg_on_p6 = any(offline_msg in n["text"] for n in nodes6)
print(f"Message rendered in pending queue on Pixel 6: {msg_on_p6}")

# Verify message is NOT yet on Pixel 7
root7 = dump_ui(DEV7)
nodes7 = get_nodes(root7)
msg_on_p7_before = any(offline_msg in n["text"] for n in nodes7)
print(f"Message received on Pixel 7 while offline (should be False): {msg_on_p7_before}")

# Step 3: Re-enable internet on Pixel 6
print("\n[Step 3] Re-enabling internet on Pixel 6...")
adb_shell(DEV6, ["svc", "data", "enable"])
adb_shell(DEV6, ["svc", "wifi", "enable"])

# Step 4: Wait for reconnect and auto-retry drain
print("\n[Step 4] Waiting for reconnection and automatic retry...")
delivered = False
for i in range(8):
    time.sleep(1.5)
    root7 = dump_ui(DEV7)
    nodes7 = get_nodes(root7)
    if any(offline_msg in n["text"] for n in nodes7):
        delivered = True
        print(f"Message auto-retried and DELIVERED to Pixel 7 after {(i+1)*1.5}s!")
        break

print(f"\nOffline Auto-Retry Test Result: {delivered and not msg_on_p7_before}")
