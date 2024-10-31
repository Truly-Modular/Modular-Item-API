@header Complex Number
@path /data_types/complex
@keywords complex number, resolvable number, stat resolver

- complex numbers allow for resolving of complex math equations like sin or log
- allows for referencing of material related stats via `[material.JSONKEY]`
- allows for stuff like `5 + sin([material.hardness] + sqrt(9)`

## Resolvers:
### `collect`
- allows for subvarients of `add`,`max`,`min`,`average`
- the variant can all be followed by any other resolver
- Example [collect.max.material.durability]

### `material`
- "[material.someStat]"
- reference a defined stat in a material, if a stat is not defined defaults to 0
- you can see Materials for more info

### `module`
- "[module.someStat]"
- reference a defined stat in a module, if a stat is not defined defaults to 0
- module stats are defined via Properties

###  `module-material` / `material-module`
- `[module-material.durability]` `[material-module.durability]`
- this will try to first use the first option, and if no specific stat was set will fallback to the second

### `count`
- `[count.module]` -> total number of modules on the item
- `[count.submodules]` -> total number of submodules on the item
- `[count.unique_materials]` -> total number of different materials used on the item
- `[count.root_material_matches]` -> total number of material used on the whole item matching the current first module with a material
- `[count.material_matches]` -> total number of material used on the whole item matching the current modules material