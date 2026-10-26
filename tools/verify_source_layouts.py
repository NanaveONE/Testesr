#!/usr/bin/env python3
"""Offline checks that preview XML + source screen resources are actually packaged."""
from pathlib import Path
import xml.etree.ElementTree as ET
import re
root=Path(__file__).resolve().parents[1]
res=root/'app/src/main/res'
java=(root/'app/src/main/java/com/skyrus/liveeditor/MainActivity.java').read_text()
layouts=set(re.findall(r'(?<![.a-zA-Z0-9])R\.layout\.(\w+)',java))
xmls=set(re.findall(r'R\.xml\.(\w+)',java))
for folder,names in [('layout',layouts),('xml',xmls)]:
    for name in sorted(names):
        assert (res/folder/f'{name}.xml').exists(), f'missing {folder}/{name}.xml'
count=0
for p in res.rglob('*.xml'):
    ET.parse(p)
    count+=1
assert (res/'layout/main_activity.xml').read_text().find('home_root')!=-1
assert 'org.stratoemu.strato' in (res/'layout/on_screen_edit_activity.xml').read_text()
assert (root/'SOURCE-ORIGIN.txt').exists()
print(f'OK: {len(layouts)} Android layout resources, {len(xmls)} preference resources, {count} XMLs parsed.')
