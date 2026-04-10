@header Data Material - Default stuff
@path /datapack/material/data/default_fields

### `translation`
```json5
{
    "translation": "SuperDirt! "
    //leave space after to not directly collide with module name.
}
```
can be usd so materials can have names without a custom resourcepack.
### `groups`
```json
{
    "groups": [
        "fabric",
        "metal",
        "my_custom_group"
    ]
}
```
Modules check for groups to see what materials are allowed to be used for crafting

## `icon`
```json
{
    "icon": {
        "type": "item",
        "item": "minecraft:sculk"
    }
}
```
Icons are rendered in the ui as a short-hand for the material.
we *heavily* recommend the usage of the item type
Alternatively, direct texture can be used too
```json
{
    "icon": "textures/item/kelp.png"
}
```