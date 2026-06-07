# ChainModelProperty

A property that configures flail/chain models to be rendered as part of modular items. Creates animated chain segments with customizable physics and model assignments.

## Purpose

Allows rendering animated chain/flail models attached to modular items. Each chain can have multiple segments with independent physics properties (gravity, collision, locking) and can use different models for different segments.

## Behavior

- Chains consist of multiple segments connected in a chain
- Each segment can have its own length, collision, and lock settings
- Base segments are repeated to fill the chain length
- Override segments can customize specific segment properties
- Models are baked and rendered for each segment
- Supports rigidness and iteration settings for physics simulation

### Fields

- **`chainLength`** _(number, optional, default = 0.75)_:
  The total length of the chain in blocks.

- **`segments`** _(integer, optional, default = 8)_:
  The number of segments in the chain.

- **`iterations`** _(integer, optional, default = 200)_:
  Physics simulation iterations for smoother animation.

- **`chainRadius`** _(number, optional, default = 0.03)_:
  The radius of the chain segments.

- **`rigidness`** _(number, optional, default = 0.3)_:
  How rigid the chain is. Higher values make it stiffer.

- **`models`** _(array, optional, default = [])_:
  List of models to use for chain segments. Can be empty to use default models.

- **`baseSegments`** _(array, optional, default = [])_:
  Base segment definitions that are repeated to fill the chain. Each segment can have length, collision, lock, and model settings.

- **`overrides`** _(object, optional)_:
  Map of segment indices to override segment definitions. Allows customizing specific segments.

- **`transform`** _(object, optional)_:
  Transform matrix to apply to the entire chain.

- **`debug`** _(boolean, optional, default = false)_:
  If true, enables debug rendering for the chain use this to position the models correctly

### Chain example

```json5
{
    "flail_model": {
        "chainLength": 0.1,
        "segments": 30,
        "rigidness": 0.5,
        "chainRadius": 0.5,
        "debug": false,
        "iterations": 30,
        "transform": {
            "rotation": {
                "x": 0,
                "y": 0,
                "z": 0
            },
            "translation": {
                "x": 0.25,
                "y": 0.25,
                "z": 0.5
            },
            "scale": {
                "x": 1,
                "y": 1,
                "z": 1
            }
        },
        "baseSegments": [
            {
                "collide": true,
                "gravity": 1.25,
                "model": {
                    "path": "miapi:models/item/chain.json",
                    "color_provider": "parent",
                    "transform": {
                        "rotation": {
                            "x": 90,
                            "y": 0,
                            "z": 0
                        },
                        "translation": {
                            "x": -0.5,
                            "y": 0.25,
                            "z": 0
                        },
                        "scale": {
                            "x": 1,
                            "y": 1,
                            "z": 1
                        }
                    }
                }
            },
            {
                "collide": true,
                "gravity": 1.5,
                "model": {
                    "path": "miapi:models/item/chain.json",
                    "transform": {
                        "rotation": {
                            "x": 90,
                            "y": 90,
                            "z": 0
                        },
                        "translation": {
                            "x": -0.25,
                            "y": -0.5,
                            "z": 0
                        },
                        "scale": {
                            "x": 1,
                            "y": 1,
                            "z": 1
                        }
                    }
                }
            }
        ]
    }
}
```
