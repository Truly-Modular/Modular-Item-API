@header Materials
@path /datapack_data_types/modules
@keywords material, materials

- a separate JSON found in miapi/materials
- requires a unique file name/path for each converter file
- The API offers a diverse range of materials.
- Materials are loosely defined JSON files that serve as references for module statistics.
- Materials need to be added to data/miapi/materials/anyPathOrNameFromHere.json
- Full list of materials can be found [here](https://github.com/Truly-Modular/Modular-Item-API/tree/main/common/src/main/resources/data/miapi/materials).
- In addition Materials are generated at Runtime to increase Mod-Compatibility


JsonMaterials is a file format used to define materials within the mod.
These materials can have various stats such as hardness, density, flexibility, etc.,
# Required Fields
### `key`
the unqiue id name of a Material. If you are adding materials from another mod, use modid:materialID here.
### `translation`
The language key of the material, points to the language file.
### `icon`
the Icon of the Material, can either be the path to a texture or a more complex icon, for that see [this](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/resources/data/miapi/materials/glass/black_glass.json#L4) as an example
### `color`
The Hex color of the Material. Please use RGB and NOT RGBA here.
# Optional Fields
## Module Stats
Module stats is a weird subcategory.
Any open key can be used to define a Modulestat, such as ` hardness` or ` mining level` , for [Example](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/resources/data/miapi/materials/glass/black_glass.json#L4) these can be dynamicly called from by calling [material.hardness] in a complex double as reference.

### `groups`
a list of group keys. by default `stone` `wood` `metal` `bone` `glass` `fabric` `crystal` `gemstone` `flint` `rod` `fletching` `smithing` are used
### `items`
A List of Items to create the Material, can either be "item", "tag" or "ingredient". requires also a "value" to define the value in relation to the cost of the modules.
### `properties`
This is a complex field . It has subfield sorted by key. Those keys can be referenced by Modules to search for Material Properties. Default keys include `default`  
`handheld` `tool` `blade`  `head`  `axe` `pickaxe` `hammer` `hoe` `shovel`
`armor` `helmet` `chest` `pants` `boots`

After the respective key a property map can be added to apply additional properties if this material is used on a matching Module.

# Color Palette
Color Palettes should be defined for all actual materials, and they need to be under the key `color_palette`
They can have different Types:
### `grayscale_map`
This is a Map where brigthness values of the raw texture are mapped to direct color values. [Example](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/resources/data/miapi/materials/glass/black_glass.json#L17)
### `from_material_palette_image`
This is a Map where brigthness values of the raw texture are mapped to direct color values. Unlike the grayscale_map this uses a 1x256 texture to remap instead [Example](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/resources/data/miapi/materials/stone/magma.json#L17)
### `image_generated`
This requires the json to mention a texture and this texture will be used to generate a palette similar to how generated Materials work.
```json
"color_palette": {
    "type": "image_generated",
    "atlas": "block",
    "texture": "minecraft:block/dirt"
}
```
### `image_generated_item`
This requires the json to mention a valid ItemId. This might be easier to use than the option above
```json
"color_palette": {
    "type": "image_generated_item",
    "item": "minecraft:dirt"
}
```
### `overlay_texture`
This overlays a Texture over the existing Model, allowing for cooler looking custom motives. Well repeating texures are recommended. [Example](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/resources/data/miapi/materials/stone/sculk.json#L17)

### `layered_mask`
This mask between two other Color Palettes via a Texture. Example:
```json
"color_palette": {
    "type": "layered_mask",
    "base": {
        "type": "grayscale_map",
        "colors": {
            "24": "2D0500",
            "68": "4A0800",
            "107": "720C00",
            "150": "720C00",
            "190": "BB2008",
            "255": "E32008"
        }
    },
    "layer": {
        "type": "grayscale_map",
        "colors": {
            "24": "002d00",
            "68": "005300",
            "107": "007b18",
            "150": "009529",
            "190": "00aa2c",
            "216": "17dd62",
            "255": "41f384"
        }
    },
    "mask": {
        "type": "texture",
        "atlas": "block",
        "texture": "minecraft:block/water_still"
    }
}
```



# NBT Materials
NBT Materials are Materials that are only defined by a single NBT tag.  
You need to put it under the nbt-tag "miapi_material".   
A Valid Parent needs to be defined to extract the stats from.
Another optional property "cost" exists to set the value of the Item in the Modular Workbench
All the other Json material properties can be used.

Example command:
`/give @p dirt{miapi_material:{parent:gold,flexibility:20,cost:4}} 64`