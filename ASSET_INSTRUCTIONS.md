# Asset Placement Guide

Place your bundled HTML entrypoint at `app/src/main/assets/index.html`. The Android WebView loads the file via `file:///android_asset/index.html`, so ensure the `index.html` (and any supporting local resources such as images or inline scripts/styles) live directly inside the `app/src/main/assets/` directory. Do not nest the file in subdirectories unless you update the load URL accordingly.
