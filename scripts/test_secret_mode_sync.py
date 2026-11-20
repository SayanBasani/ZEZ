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

print("=== Starting Secret Mode Synchronization & Ephemeral Lifecycle Test ===")

# 1. Tap Mode Toggle on Pixel 6
print("\n[Step 1] Pixel 6 activates Secret Mode...")
root6 = dump_ui(DEV6)
nodes6 = get_nodes(root6)
toggle6 = find_node(nodes6, rid_contains="btn_chat_mode_toggle")
if not toggle6:
    print("Toggle button not found on Pixel 6!")
    exit(1)
tap_node(DEV6, toggle6)
time.sleep(1.0)

# Verify Pixel 6 is in Secret mode
root6 = dump_ui(DEV6)
nodes6 = get_nodes(root6)
lbl6 = find_node(nodes6, rid_contains="tv_mode_label")
print(f"Pixel 6 mode label: {lbl6['text'] if lbl6 else 'None'}")

# 2. Wait for Pixel 7 to poll and auto-synchronize to Secret Mode
print("\n[Step 2] Waiting for Pixel 7 to synchronize to Secret Mode...")
p7_secret = False
for _ in range(5):
    time.sleep(1.5)
    root7 = dump_ui(DEV7)
    nodes7 = get_nodes(root7)
    lbl7 = find_node(nodes7, rid_contains="tv_mode_label")
    if lbl7 and lbl7["text"].strip().lower() == "secret":
        p7_secret = True
        print(f"Pixel 7 auto-synchronized to: {lbl7['text']}")
        break
print(f"Pixel 7 Secret Mode Synchronized: {p7_secret}")

# 3. Pixel 6 sends secret message
print("\n[Step 3] Pixel 6 sends ephemeral secret message...")
secret_msg = f"Top-secret-whisper-{int(time.time())}"
send_chat_text(DEV6, secret_msg)
print(f"Pixel 6 sent secret: '{secret_msg}'")

time.sleep(3)

# Verify secret message on Pixel 7
root7 = dump_ui(DEV7)
nodes7 = get_nodes(root7)
secret_on_p7 = any(secret_msg in n["text"] for n in nodes7)
print(f"Pixel 7 received secret message: {secret_on_p7}")

# 4. Turn off Secret Mode from Pixel 7 (testing exit from the other side!)
print("\n[Step 4] Pixel 7 exits Secret Mode...")
root7 = dump_ui(DEV7)
nodes7 = get_nodes(root7)
exit_btn7 = find_node(nodes7, rid_contains="btn_exit_secret_mode") or find_node(nodes7, rid_contains="btn_chat_mode_toggle")
if exit_btn7:
    tap_node(DEV7, exit_btn7)
else:
    print("Exit button not found on Pixel 7, using toggle")
    tap_node(DEV7, find_node(nodes7, rid_contains="btn_chat_mode_toggle"))
time.sleep(1.0)

# Verify Pixel 7 back to Normal
root7 = dump_ui(DEV7)
nodes7 = get_nodes(root7)
lbl7 = find_node(nodes7, rid_contains="tv_mode_label")
print(f"Pixel 7 mode label after exit: {lbl7['text'] if lbl7 else 'None'}")

# 5. Wait for Pixel 6 to synchronize back to Normal Mode
print("\n[Step 5] Waiting for Pixel 6 to synchronize back to Normal Mode...")
p6_normal = False
for _ in range(5):
    time.sleep(1.5)
    root6 = dump_ui(DEV6)
    nodes6 = get_nodes(root6)
    lbl6 = find_node(nodes6, rid_contains="tv_mode_label")
    if lbl6 and lbl6["text"].strip().lower() == "normal":
        p6_normal = True
        print(f"Pixel 6 synchronized back to: {lbl6['text']}")
        break
print(f"Pixel 6 Normal Mode Restored: {p6_normal}")

# 6. Verify ephemeral guarantee: secret_msg must NOT be on screen for either user!
root6 = dump_ui(DEV6)
nodes6 = get_nodes(root6)
secret_still_on_p6 = any(secret_msg in n["text"] for n in nodes6)

root7 = dump_ui(DEV7)
nodes7 = get_nodes(root7)
secret_still_on_p7 = any(secret_msg in n["text"] for n in nodes7)

print(f"\n[Step 6] Ephemeral Verification:")
print(f"  Secret message on Pixel 6 screen (should be False): {secret_still_on_p6}")
print(f"  Secret message on Pixel 7 screen (should be False): {secret_still_on_p7}")

print(f"\nAll Secret Mode Tests Passed: {p7_secret and secret_on_p7 and p6_normal and not secret_still_on_p6 and not secret_still_on_p7}")
