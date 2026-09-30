## v2.3.10 (1.21)

### Additions
- added compat for LambDynamicLights
- added new Keybind to show stats on modular item (default is S)
- added new Keybind to show stats to compare to currently equiped item (default is C)
- added new Commands to show/compare stats on modular items
    - /miapi stat mainhand -> prints mainhand stats into chat
    - /miapi stat compareHands -> compares stats from mainhand to offhand
    - /miapi stat compareHandsOnlyDiff -> compares mainhand to offhand but prints only differences.

### Changes
- changed developer mode to support ALL, ADMIN and NONE
- rewrote internal stat api
    - groups now exist
    - emojis are mostly added by groups instead of the stats now, helping users understand the usage of a stat faster
    - existing groups are Melee, MeleeModifier, Projectile Weapon, Projectile, Throw, Enchantments On-Hit(applies melee and ranges), Mining, Armor and misc

### Fixes
- fixed bug where material effects could apply twice
- fixed bug where Potion effects would not correctly merge
- fixed bug where explosion range and max damage where accidentally swapped