## v2.3.3 (1.21)
### Bugfixes
- fixed module extensions not loading correctly
- fixed replacing the root module allowing for broken children to be applied
- fixed crash when server could not find crafting player
- fixed issue where when stack crafting message arrows each arrow would drop the original stack.
- fixed issue where fake enchantments are applied multiple times
- fixed accessories crashing on neoforge
### Additions
- added custom damage system to allow for different types of dynamic damage [wiki](https://truly-modular.github.io/Modular-Item-API/?branch=release%2F1.21-mojmaps&page=home%2Fdata_types%2Fproperties%2Fon_hit%2Fcustom+damage)
- added generic entity damage property to allow for custom damage boni on targets [wiki](https://truly-modular.github.io/Modular-Item-API/?branch=release%2F1.21-mojmaps&page=home%2Fdata_types%2Fproperties%2Fon_hit%2Fgeneric_entity_damage)
- added generic entity armor property to allow for generic damage reduction from targets [wiki](https://truly-modular.github.io/Modular-Item-API/?branch=release%2F1.21-mojmaps&page=home%2Fdata_types%2Fproperties%2Fon_hit%2Fgeneric_entity_armor)
### Changes
- made workbench waterloggable