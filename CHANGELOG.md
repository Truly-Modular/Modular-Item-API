## v2.0.0 (1.21)
- ported to 1.21
  - refactored and cleaned codebase
  - rewrote the property systems
  - moved to proper component logic
- dropped legacy Forge (MinecraftForge) in favor or NeoForge

## New Features
- added blueprint logic (allowing for parts or blueprints for addons)
- added composite-materials (allowing for modular materials/alloying for addons)
- added loot-functions
- added loot-modularisation
- added material indications (you can now see how material stats affect modules)
- added full glint customisation for patreon supporters (item and module level)
- added support for dying (leather items are now dyeable)
- added the following properties: module_icon, copy_item_on_hit, pogo_ability, alpha_overwrite, components, material_component_property, copy_item_lore, color, allowed_in_loot, projectile_impact_sound, material_indication, draw_time
- improved ability system :
  - added data driven keybinds
  - added secondary ability property able to respond to those keys  
    This fixed the issue of only having one ability per item

## Compat
- added ability to copy on hit, descriptions, enchantments, components and item tags for generated materials.  
This Greatly improves compat with many mods, for example Ice and Fire, Applied Energetics and Mythic Metals
- redone the coloring and naming and stat-finding algorithms to better match expected results


## Technical Changes 
- Properties no longer support Uppercase letters, they are now using snake_case
- all data has been moved from using miapis namespace to using miapi in the path in any namespace  
f.e. miapi:modules/ -> mod-id:miapi/modules/
- most key/name systems have been replaced by using path-based Identifiers (this includes both modules and materials)  
f.e. blade_normal -> arsenal:blade/normal
- for more info check [this](https://github.com/Truly-Modular/Modular-Item-API/blob/release/1.21-mojmaps/1.21%20PORT.md)

## Snapshot 2
- fixed many server related bugs
- added loot-modularisation
- added material indications
- added icon-property