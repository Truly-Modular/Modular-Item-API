@header Creating a Skin Data + Texture Pack
@path /examples/skin
@keywords skin

This guide will walk you through setting up a skin resource and texture pack for your Minecraft mod. The resource configuration file should be named as `skin.json` and placed within your mod's directory.

The directory structure should look like this:
```mod-id/miapi/skins/module/any-name-or-path.json```


## Structure of `skin.json`

Here's an example of what your `skin.json` file should look like:

```json
{
    "path": "bident",
    "module": "arsenal:blade/trident",
    "replace": {
        "model": [
            {
                "path": "miapi:models/item/sword/blade/trident/bident/[material.texture].json",
                "color": "[material.color]",
                "transform": {
                    "rotation": {
                        "x": 0.0,
                        "y": 0.0,
                        "z": 0.0
                    },
                    "translation": {
                        "x": -5.0,
                        "y": -5.0,
                        "z": 0.0
                    },
                    "scale": {
                        "x": 1,
                        "y": 1,
                        "z": 1
                    }
                }
            }
        ],
        "gui_offset": {
            "sizeX": -6,
            "sizeY": -6
        }
    },
    "condition": {
        "type": "true"
    },
    "model": {
        "color": "FFFFFF",
        "scale": 0,
        "borderSize": 0
    }
}
```
### `path`

The `path` is used to specify the unique identifier for this skin in the UI. Using `/` in the path allows you to set up groups or subcategories for better organization. For example, `path: "skyrim/bident"` would group this skin under a "skyrim" category in the UI.
### `module`

The `module` specifies the exact module this skin will apply to. Alternatively, you can use `"tag"` with a module tag to apply the skin to multiple modules that share the specified tag. This flexibility allows you to target a single module or groups of related modules.
### `replace`

The `replace` section defines specific properties within the module that should be replaced. This might include the model path, color, transformations, and GUI offsets. There are additional options like `"merge"` and `"remove"` which allow you to combine or remove specific properties, similar to the synergy system.
### `condition`

The `condition` section utilizes the internal Condition system. Here, you can define requirements that must be met for a user to access this skin. Usually you just want to use `"type:true"` to always unlock this skin.

### `model`

The `model` refers to the background texture displayed on the button for this skin in the UI. It’s recommended to leave this section unchanged.
### `hover` (optional)

The `hover` property is an optional field that adds a tooltip when hovering over the skin in the UI. This can be a helpful place to provide information, such as credit to the skin creator or additional details about the skin.


# Examples
- [example Datapack](https://github.com/Truly-Modular/Modular-Item-API/blob/release/1.21-mojmaps/common/src/main/java/smartin/miapi/wiki/examples/skin/example_skin_datapack.zip)
- [example Resource Pack](https://github.com/Truly-Modular/Modular-Item-API/blob/release/1.21-mojmaps/common/src/main/java/smartin/miapi/wiki/examples/skin/example_skin_resourcepack.zip)