@header Blueprint Component
@path /datapack/blueprint/component

## Overview
The **Blueprint Component** attaches additional craft options to items in the crafting UI.  
It applies a custom Module instance with custom Data, allowing direct attachment of skins or submodules.



## JSON Structure

```json5
{
  "module": { 
      "id": "example:super_blade",
      "data": {
          "material": "miapi:metal/netherite"
      }
  }, //this is the moduleinstance. run /data get SelectedItem to get a decent understanding of how to structure this.
  "ingredient": true,
  "name": { "text": "Optional Custom Name" }
}
```
## Ingredient
- ### true
  in this case the Ingredient will require the source item. This only works on components.
- ### false
  in this case the module sets the ingredients
- Custom
```json
{
  "ingredient": {
    "ingredient": {
      "item": "minecraft:dirt"
    },
    "count": 1
  }
}
```
The ingredient for the recipe. Can be one of an [String] ID, a [String] tag with #, or an [JSON Array] array containing IDs.
```json
{
  "ingredient": {
    "ingredient": {
      "tag": "minecraft:stones"
    },
    "count": 1
  }
}
```
```json
{
  "ingredient": {
    "ingredient": [
      { "item": "minecraft:iron_ingot" },
      { "item": "minecraft:gold_ingot" }
    ],
    "count": 1
  }
}
```