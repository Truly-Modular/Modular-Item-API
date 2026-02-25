## v2.2.4 (1.21)
### Bugfixes
- fixed minor issues with generated materials causing stat displays to fail
- fixed generated materials not having toughness
- fixed broken items removing their module information
- fixed bug where some attributes using add_multiply_base where mistakenly discarded
- improvements to glint logic and robustness
  - added armor glint strength setting to reduce glint on armor
  - re-added glint-rendering using vanillas rendering as fallback
    - fallback is loaded if Vulkanmod is loaded to allow for full compat
    - fallback now looks much better then previously
- fixed issue where durability is rendered wrongly if other mods modify maxDamage
- fixed crossbows shooting from feet sometimes
### Changes
- changed texture recoloring logic to allow for alpha pass through from original
- improved shift+alt stat display
- improved rendering logic for more dynamic models
### Additions
- added trail rendering
  - fully data-driven model type
  - added 2 base trail models to be used
- added prototype chain/rope rendering
- added in-air model transformation for projectiles
- added in-air render animations for projectiles (spinning daggers)
- added "shot_velocity" property to adjust velocity when shot and not thrown