import os
import re
import glob

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Find the parent LinearLayout and replace #000 with @drawable/level1_bg
    content = re.sub(
        r'(<LinearLayout[^>]*?android:id="@+id/parent"[^>]*?)android:background="#000"',
        r'\1android:background="@drawable/level1_bg"',
        content,
        flags=re.DOTALL
    )

    # Find the ConstraintLayout and remove @drawable/level1_bg
    content = re.sub(
        r'(<androidx\.constraintlayout\.widget\.ConstraintLayout[^>]*?)android:background="@drawable/level1_bg"',
        r'\1',
        content,
        flags=re.DOTALL
    )

    with open(filepath, 'w') as f:
        f.write(content)

for root, _, files in os.walk('app/src/main/res'):
    for file in files:
        if file in ['activity_levelx_layout.xml', 'activity_sequence.xml']:
            process_file(os.path.join(root, file))

print("Done")
