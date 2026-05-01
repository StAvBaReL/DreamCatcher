import os
import re
dir_path = r'app/src/main/java/com/colman/dreamcatcher/view'
for filename in os.listdir(dir_path):
    if filename.endswith('Fragment.kt'):
        filepath = os.path.join(dir_path, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        # Step 1: Remove _binding and use inding
        content = re.sub(r'private var _binding:\s*(Fragment\w+Binding)\s*\?\s*=\s*null\s*\n\s*private val binding get\(\)\s*=\s*_binding!!', r'private var binding: \1? = null', content)
        # Step 2: Change _binding to binding
        content = content.replace('_binding = ', 'binding = ')
        content = content.replace('_binding?.', 'binding?.')
        # Step 3: Replace binding. with binding?.
        # Need negative lookaround to not replace binding = 
        content = re.sub(r'\bbinding\.(?!\?)', r'binding?.', content)
        # Step 4: Fix onCreateView return View safe call
        # "return binding?.root" returns View?, but signature might say View.
        # But wait, Fragment signature: un onCreateView(..., Bundle?): View {
        # if they return inding?.root ?: View(...), it's messy. Let's just use !!... wait, rule says NOT to use !!!
        # If we use inding?.root as View it uses s instead of !!, but better inding?.root ?: super.onCreateView(...) 
        # Actually, since it's instantiated immediately before:
        # inding = ...inflate(...)
        # eturn binding?.root as View or we can change onCreateView return type to View? as permitted by Fragment contract.
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
