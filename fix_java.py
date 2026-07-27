import os
import re

for root, _, files in os.walk('app/src/main/java'):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            
            # Remove calls to setNavigationUiConditionally()
            new_content = re.sub(r'^[ \t]*setNavigationUiConditionally\(\);[ \t]*\n', '', content, flags=re.MULTILINE)
            
            if new_content != content:
                with open(filepath, 'w') as f:
                    f.write(new_content)
                print(f"Fixed {file}")

print("Done")
