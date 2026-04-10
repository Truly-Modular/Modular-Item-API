@header Data Material - Rendering
@path /datapack/material/data/rendering_fields
## Color
its a simple rgb hex or rgba hex for a color
```json
{
    "color": "FF55FF"
}
```
This is only used as a fallback and for setting the glint behavour

## Textures
```json
{
    "textures": [
        "rough"
    ]
}
```
this is used to select potentially existing models for this material.
when a module sets a module it ends with [material.texture] in its path.
this part is potentially replaced by these variants.
they are executed in order, first entry in the list is checked for first.
        
# Color Provider
now, this is how modules with this material are colored.
on a fundamental level, there are multiple color providers:

### `grayscale_map`
```json5
{
    "color_palette": {
        "type": "grayscale_map",
        "colors": {
            "24": "2D0500",//maps brightness to color.
            "68": "4A0800",//most materials will be fully content with this
            "107": "720C00",//values in between brightness values are interpolated
            "150": "720C00",
            "190": "BB2008",
            "255": "E32008"
        }
    }
}
```

### `overlay_texture`
```json
{
    "color_palette": {
        "type": "overlay_texture",
        "atlas": "block",
        "texture": "minecraft:block/sculk"
    }
}
```
This overlays an texture and multiplies the base Brightnesses with it.
Great to add custom patterns/texturing to materials for a cool effect.

### `layered_mask`
```json5
{
    "color_palette": {
        "type": "layered_mask",
        "base": {//this is used as the foundation
            //when the mask is black this is scaled to 100%
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
            //when the mask is fully white, this completly covers the base.
            //when the mask is black,only the base renders.
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
            "texture": "minecraft:block/water_still"//works with animated textures too
            //mask textures are repeated when out of bounds
        }
    }
}
```
Now, this is where the magic really happens for complicated rendering.  
This allows you to stack and combine different other palettes to create custom effects