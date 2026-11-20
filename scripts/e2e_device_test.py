import xml.etree.ElementTree as ET
import subprocess
import time
import json
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
    # Type formatted text (replace space with %s for adb input)
    escaped = text.replace(" ", "%s").replace("'", "\\'").replace('"', '\\"')
    adb_shell(dev, ["input", "text", escaped])
    time.sleep(0.3)
    
    # Hide keyboard if needed or find send button
    root = dump_ui(dev)
    nodes = get_nodes(root)
    send_btn = find_node(nodes, rid_contains="btn_chat_send_container") or find_node(nodes, rid_contains="btn_chat_send")
    if send_btn:
        tap_node(dev, send_btn)
    else:
        # Tap bottom right area for send button
        adb_shell(dev, ["input", "tap", "1000", "2300"])
    return True

print("=== Starting E2E Dual-Device Test ===")

# Check UI on Pixel 6
root6 = dump_ui(DEV6)
nodes6 = get_nodes(root6)
hdr6 = find_node(nodes6, rid_contains="tv_chat_header_name")
print(f"Pixel 6 chat header: {hdr6['text'] if hdr6 else 'Not found'}")

# Check UI on Pixel 7
root7 = dump_ui(DEV7)
nodes7 = get_nodes(root7)
hdr7 = find_node(nodes7, rid_contains="tv_chat_header_name")
print(f"Pixel 7 chat header: {hdr7['text'] if hdr7 else 'Not found'}")

# Test 1: Send message from Pixel 6 to Pixel 7
print("\n--- Test 1: Pixel 6 sends message to Pixel 7 ---")
msg1 = f"Hello Pixel 7! Test sync {int(time.time())}"
send_chat_text(DEV6, msg1)
print(f"Pixel 6 sent: '{msg1}'")

time.sleep(3) # Wait for poll cycle (1.5s)

# Verify message on Pixel 7
root7 = dump_ui(DEV7)
nodes7 = get_nodes(root7)
msg_found = any(msg1 in n["text"] for n in nodes7)
print(f"Message received on Pixel 7: {msg_found}")

# Test 2: Reply from Pixel 7 to Pixel 6
print("\n--- Test 2: Pixel 7 replies to Pixel 6 ---")
msg2 = f"Hi Pixel 6! Reply received {int(time.time())}"
send_chat_text(DEV7, msg2)
print(f"Pixel 7 sent: '{msg2}'")

time.sleep(3)

# Verify reply on Pixel 6
root6 = dump_ui(DEV6)
nodes6 = get_nodes(root6)
reply_found = any(msg2 in n["text"] for n in nodes6)
print(f"Reply received on Pixel 6: {reply_found}")

print(f"\nSummary: Test 1 Passed={msg_found}, Test 2 Passed={reply_found}")
