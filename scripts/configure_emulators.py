import xml.etree.ElementTree as ET
import subprocess
import time
import re

def configure_backend(device_id):
    print(f"Configuring {device_id}...")
    # Switch to Settings tab
    subprocess.run(['adb', '-s', device_id, 'shell', 'input', 'tap', '945', '2253'])
    time.sleep(1.0)
    
    # Dump UI
    subprocess.run(['adb', '-s', device_id, 'shell', 'uiautomator', 'dump', '/sdcard/window_dump.xml'], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    out = subprocess.check_output(['adb', '-s', device_id, 'shell', 'cat', '/sdcard/window_dump.xml']).decode('utf-8', errors='ignore')
    root = ET.fromstring(out)
    
    input_node = None
    save_node = None
    for el in root.iter():
        rid = el.attrib.get('resource-id', '')
        if 'et_setting_backend_url' in rid:
            input_node = el
        elif 'btn_setting_save_url' in rid:
            save_node = el
            
    if input_node is None or save_node is None:
        print(f"Nodes not found on {device_id}!")
        return False
        
    m = re.findall(r'\[(\d+),(\d+)\]', input_node.attrib['bounds'])
    x1, y1 = int(m[0][0]), int(m[0][1])
    x2, y2 = int(m[1][0]), int(m[1][1])
    cx, cy = (x1 + x2) // 2, (y1 + y2) // 2
    
    m_s = re.findall(r'\[(\d+),(\d+)\]', save_node.attrib['bounds'])
    sx = (int(m_s[0][0]) + int(m_s[1][0])) // 2
    sy = (int(m_s[0][1]) + int(m_s[1][1])) // 2
    
    # Tap input
    subprocess.run(['adb', '-s', device_id, 'shell', 'input', 'tap', str(cx), str(cy)])
    time.sleep(0.5)
    
    # Move end
    subprocess.run(['adb', '-s', device_id, 'shell', 'input', 'keyevent', '123'])
    time.sleep(0.2)
    for _ in range(45):
        subprocess.run(['adb', '-s', device_id, 'shell', 'input', 'keyevent', '67'])
    time.sleep(0.3)
    
    # Input local backend url
    subprocess.run(['adb', '-s', device_id, 'shell', 'input', 'text', 'http://10.0.2.2:3000'])
    time.sleep(0.5)
    
    # Tap Save & Connect
    subprocess.run(['adb', '-s', device_id, 'shell', 'input', 'tap', str(sx), str(sy)])
    time.sleep(1.5)
    
    # Verify
    subprocess.run(['adb', '-s', device_id, 'shell', 'uiautomator', 'dump', '/sdcard/window_dump.xml'], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    out2 = subprocess.check_output(['adb', '-s', device_id, 'shell', 'cat', '/sdcard/window_dump.xml']).decode('utf-8', errors='ignore')
    root2 = ET.fromstring(out2)
    for el in root2.iter():
        if 'tv_setting_server_current' in el.attrib.get('resource-id', ''):
            print(f"{device_id} current server: {el.attrib.get('text')}")
    return True

if __name__ == '__main__':
    configure_backend('emulator-5556')
    configure_backend('emulator-5554')
