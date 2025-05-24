@header Render Properties 
@path /data_types/properties/render

Truly Modulars rendering is a quite complex topic with many properties interacting in different ways with it.

## Model Properties
Overall, each module can have any number and type of modules.   
Different Properties control different Types of Models.
- ``model`` the basic Resourcepack controlled JsonModels.
This property has advanced support for trims, color Providers as well as entity mode, allowing the model to be rendered from both sides.
- ``banner`` Renders a banner, has different modi for different ways of rendering a banner
- ``entity_model`` allows the rendering of an entitity, used for the end crystal 
- ``block_model`` renders a block

## Color Providers
Color providers is how truly modular refers to the thing deciding final color of a model.
This is quite a complex process with many steps, but its split into only a handfull of providers:
``material``,   
``model``(does not change color),   
``potion``(searches on item for potion color),   
``parent`` uses parent material,  
``item.material`` uses the current item as material   

## Material Color Palettes and their relations.
Materials can leverage even more complex ways to render with their own render controllers.
### grayscale_map
The process simply recolors a gray-scale texture.
The default solution is a simple map from brightness -> color using the grayscale_map
```
    "color_palette": {
        "type": "grayscale_map",
        "colors": {
            "24": "7A7D6A",
            "68": "C1BD9F",
            "107": "C7C3A5",
            "150": "D9D6BE",
            "190": "E5E2CF",
            "216": "F9F8EA",
            "255": "FCFCFA"
        },
        "filler": "interpolate"
    },
```
available filler are ``interpolate``,``current_to_last``,``last_to_current``,``current_last_shared``
### overlay_texture
overlays an texture, but respects underlaying aplha values. Great for highlights. (sculk uses this)
```
    "color_palette": {
        "type": "overlay_texture",
        "atlas": "block",
        "texture": "minecraft:block/sculk"
    }
```
### image_generated and image_generated_item
leverages the palette generation for generated materials,
either
```
    "color_palette": {
        "type": "image_generated",
        "atlas": "block",
        "texture": "minecraft:block/dirt"
    }
```
or
```
    "color_palette": {
        "type": "image_generated_item",
        "item": "minecraft:dirt"
    }
```
### from_material_palette_image
this uses a handmade 1x256 texture to read the values instead.
Magma uses this and its good for animated materials
```
    "color_palette": {
        "type": "from_material_palette_image",
        "location": "miapi:miapi_materials/stone_magma"
    },
```
### layered_mask
by far the most complex palette, since it allows you to layer *any* other 2 palettes with a mask texture (they can be animated too)
Crying Obsidian uses this for example,
```
    "color_palette": {
        "type": "layered_mask",
        "base": {
            "type": "grayscale_map",
            "colors": {
                "24": "000001",
                "68": "06030b",
                "107": "100c1c",
                "150": "271e3d",
                "255": "3b2754"
            }
        },
        "layer": {
            "type": "overlay_texture",
            "atlas": "block",
            "texture": "minecraft:block/crying_obsidian"
        },
        "mask": {
            "type": "texture",
            "atlas": "block",
            "texture": "minecraft:block/crying_obsidian"
        }
    },
```
it can use the same texture as a mask and layer since the non-black highlights 100% matchup with the purple areas in obsidian.


## Additional Properties
Ontop of these Properties many other properties interact with the rendering process in different ways, like the
- ``gui_offset`` setting the icon size so the item is set correctly.
- ``module_icon`` if set overwrites the icon rendering with a custom solution
- ``model_transform`` allows the setting of the transform (how the item is held essentially)
- ``alpha_overwrite`` allows for additional transparency control, can be used to let modules appear transparent
- ``color`` controls the modules individual die color, but no ingame mechanic exists to let players set per-module-die
- ``glint_settings`` controls the glint settings of the individual module or the default for the item.