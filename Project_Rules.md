PROJECT GOVERNANCE: RULES & GUIDELINES
​Project Name: NEON STACK UNDERGROUND (Android Edition)
Target Platform: Android System WebView (APK)
Strict Mode: ENABLED
​1. THE PRIME DIRECTIVE: "ANDROID-NATIVE FIRST"
​This is not a website. It is a local HTML5 application running inside a Java/Kotlin WebView.
Rule 1.1: The layout must be Liquid & Locked. No scrolling is permitted on the X or Y axis. The app must fit 100% of the screen height (100vh) and width (100vw).
Rule 1.2: The "Notch" and "Safe Area" are non-negotiable. All CSS containers must use:
padding-top: env(safe-area-inset-top);
padding-right: env(safe-area-inset-right);
padding-bottom: env(safe-area-inset-bottom);
padding-left: env(safe-area-inset-left);

Rule 1.3: Context Menus are Forbidden. You must disable the default long-press context menu on Android:
body { -webkit-touch-callout: none; user-select: none; }

2. CODING STANDARDS & ARCHITECTURE
​Rule 2.1: Single-File Purity. The output must be one index.html file.
​CSS must be in <style> tags.
​JS must be in <script> tags.
​Reasoning: Android WebView.loadUrl("file:///android_asset/index.html") is most stable when loading a single resource.
​Rule 2.2: Zero External Dependencies.
​No Google Fonts (Use standard sans-serif or monospace).
​No FontAwesome/Icon Packs (Draw icons on Canvas or use SVG paths).
​No Audio Files (Use Web Audio API oscillators).
​Reasoning: The app must work perfectly in "Airplane Mode."
​Rule 2.3: The "Game Loop" Mandate.
​You must use requestAnimationFrame for rendering.
​You must never use setInterval for animation, only for logic timers (game clock).
​Optimization: Garbage collection must be minimized. Do not declare new variables inside the loop; reuse global objects.
​3. INTERACTION & UX GUIDELINES
​Rule 3.1: The "Fat Finger" Rule. * Every interactive element (columns, buttons, toggles) must have a minimum touch target of 48x48 logical pixels, even if the visual graphic is smaller.
​Rule 3.2: Latency Elimination.
​You must include <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">.
​CSS must include touch-action: manipulation to remove the 300ms tap delay on Android.
​Rule 3.3: Feedback Loops.
​Visual: Every touch must trigger a visual state change (active/pressed state) immediately (0ms delay).
​Haptic (Simulated): The screen must "shake" (CSS transform translate) on heavy impacts (chip drops).
​4. CRITICAL LOGIC CHECKS (The "Pre-Flight" Audit)
​Before outputting ANY code, the AI must internally verify the following checklist. If any are "No," the code is rejected.
​Diagonal Win Check: Does the algorithm check both Top-Left to Bottom-Right AND Bottom-Left to Top-Right?
​Gravity Logic: If a chip is dropped in Column 3, does it stack on top of the existing chips, or does it clip through?
​The "Full Column" Glitch: Does the game prevent the player from dropping a chip into a column that is already full? (Must show "COLUMN FULL" error state).
​Race Conditions: If the player taps rapidly, does the game queue the moves or block input until the animation finishes? (It must block input via an isDropping flag).
​5. VISUAL AESTHETICS & THEME RULES
​Rule 5.1: The "Glow" Standard.
​Neon elements are not just colored; they must emit light. Use box-shadow with multiple layers:
​box-shadow: 0 0 5px #color, 0 0 10px #color, 0 0 20px #color;
​Rule 5.2: Canvas Rendering.
​The Grid and Chips must be rendered on HTML5 <canvas>.
​Do not use DOM elements (<div>) for the chips, as 42 DOM elements animating simultaneously will cause frame drops on low-end Android devices.
​6. DEBUGGING & ERROR HANDLING
​Rule 6.1: Silent Failures.
​The app must never crash to a white screen. Wrap the initialization in a try...catch block.
​If AudioContext is blocked (autoplay policy), show a "TAP TO START" overlay to unlock audio.
​Rule 6.2: No alert() Boxes.
​Never use native browser alerts. They look unprofessional. Build a custom HTML/CSS modal overlay for "Game Over" or "Win" messages.
​END OF RULES. CONFIRM UNDERSTANDING BEFORE PROCEEDING.