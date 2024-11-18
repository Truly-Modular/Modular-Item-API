@header Creating a Module Data + Texture Pack
@path /examples/module
@keywords module , modules

## Modules

**Path:** `/datapack/module`  
**Header:** Modules

### Overview

Modules are the core of the *Truly Modular* system. They allow for the creation and customization of various in-game elements by defining specific properties and models in a structured way. A module is essentially a collection of properties that dictate the behavior, appearance, and other attributes of an item or element within the game.

Modules can be added or found in the following directory:


Each module consists of a map of properties.

### Creating Your Own Module

To create a custom module:

1. **Navigate to the Modules Directory**:

   Create the `mod-id:/miapi/modules/` directory in your Data Pack.
   Substitute `mod-id` with the id/name of your datapack, please make sure its unique, only lowercase and _ are allowed.

2. **Create a New JSON File**:

   Create a new `.json` file in the directory. You can name the file anything you like (e.g., `sword_module.json`). 
   We recommend picking something that is fairly descriptive, only lowercase and _ are allowed.

3. **Define Your Module**:

   Use the example above to define the properties of your module.  Specify properties to match your requirements.

4. **Texture Pack**
   
   If you want to use a custom model the model Property should be used (there are alternatives, but those are most advanced, see Render Properties for more info)
```json
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
]
```
- **path**: This specifies the file path to the model. In the example, the texture path is parameterized using `[material.texture]`, allowing dynamic texture selection based on material texture keys.
  default needs to exist as a substitute to `[material.texture]`, but for example netherite can then be provided as well, [armory makes heavy use of this](https://github.com/Truly-Modular/Armory/tree/master/armory-common/src/main/resources/assets/miapi/models/item/armor/model/chest_front/base)
  The only requirement is that they are under the miapi namespace. 
  We recommend for custom datapacks to use miapi/models/item/datapack-id/.... to avoid collisions.
  We fully support any Java Item/Block model, you can use [Blockbench](https://web.blockbench.net/) to make them
- **color**: This property allows for customization of the model's color using `[material.color]`. This can be left out.

- **transform**: This object defines how the model should be rotated, translated, and scaled within the game environment.

    - **rotation**: Specifies the rotation of the model in degrees for each axis (x, y, z).
    - **translation**: Moves the model along the x, y, and z axes.
    - **scale**: Adjusts the size of the model along the x, y, and z axes.

5. **Save and Test**:

   Save the file, add a .mcmeta and load it into the game to see your custom module in action. Adjust the properties as necessary to fine-tune the look and behavior.
   [Minecraft Wiki on Datapacks / mcmeta](https://minecraft.wiki/w/Data_pack)

## Conclusion

Creating a Module Resource + Texture Pack allows for deep customization in *Truly Modular*. The ability to manipulate models, colors, and transforms via a simple JSON structure makes it accessible for both beginners and advanced users. Dive into the files, experiment with the properties, and create a unique game experience tailored to your vision!


# Examples
- [example Datapack](https://github.com/Truly-Modular/Modular-Item-API/blob/release/1.21-mojmaps/common/src/main/java/smartin/miapi/wiki/examples/module/example_module_datapack.zip)
- [example Resource Pack](https://github.com/Truly-Modular/Modular-Item-API/blob/release/1.21-mojmaps/common/src/main/java/smartin/miapi/wiki/examples/module/example_module_resourcepack.zip)