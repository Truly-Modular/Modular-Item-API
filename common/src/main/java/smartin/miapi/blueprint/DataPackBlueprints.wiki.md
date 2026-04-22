@header Blueprint Data
@path /datapack/blueprint/data

## Overview
Datapack Blueprints add crafting options to the base UI.  
These Options are always allowed when the base module is allowed.  
Further fine tuning can be achieved with the CraftingConditions Property  
These Jsons need to be placed in /data/{mod-id}/miapi/blueprint/{...}.json  
```json5
{
  "module": {
    "key": "example:super_blade",
    "data": {
      "material": "miapi:metal/netherite",
      "properties": {
        "crafting_condition": {
          "selectAble": {
            "type": "advancement",
            "advancement": "minecraft:end/elytra"
          }
        }
      }
    }
  },
  "ingredient": true,
  "name": { "text": "Optional Custom Name" }
}
```

---

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
- ### false
  in this case the module sets the ingredients, this allows for base material crafting.
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