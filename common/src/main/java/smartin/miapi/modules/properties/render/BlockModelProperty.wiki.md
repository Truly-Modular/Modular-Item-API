# BlockModelProperty

A property that configures block models to be rendered as part of modular items. Each block model can have its own NBT state, transformations, and spin animations.

## Purpose

Allows rendering custom blocks (with their states and NBT) as part of a modular item's visual representation. Useful for creating item frames with blocks, decorative blocks, or functional block representations.

## Behavior

- Blocks are loaded from their `ResourceLocation`
- NBT can be used to set block states via block state codec
- Spin animations can be applied to rotating blocks
- Transform matrices position and scale the blocks
- Blocks are rendered using `BlockRenderModel`

## JSON Structure

```json5
{
	id: 'string (required)',
	nbt: 'object (optional)',
	transform: 'object (optional)',
	spin: 'object (optional)'
}
```

### Fields

- **`id`** _(string, required)_:
  The block resource location. Example: `"minecraft:stone"`, `"minecraft:torch"`

- **`nbt`** _(object, optional)_:
  NBT compound tag used to set block states. Parsed using block state codec.

- **`transform`** _(object, optional)_:
  Transform matrix to apply to the block. Defaults to identity transform.

- **`spin`** _(object, optional)_:
  Spin animation settings from `MaterialIcons.SpinSettings`.

## Examples

### Simple Block

```json5
{
  "block_model": {
    id: 'minecraft:stone',
    transform: {
      translation: [0, 0.5, 0],
      scale: [1, 1, 1]
    }
  }
}
```

### Block with NBT State

```json5
{
  "block_model": {
      id: 'minecraft:torch',
      nbt: {
          Face: 'east',
          Lit: true
      },
      transform: {
          translation: [
              0.5,
              0.5,
              0
          ]
      }
  }
}
```

### Rotating Block

```json5
{
  "block_model": {
      id: 'minecraft:lantern',
      nbt: {
          Lit: true
      },
      transform: {
          translation: [
              0,
              0.5,
              0
          ],
        scale: [10,10,10]
      },
      spin: {
          axis: 'Y',
          speed: 3,
          enabled: true
      }
  }
}
```

### Multiple Blocks

```json5
{
    "block_model": [
        {
            id: 'minecraft:stone',
            transform: {
                translation: [
                    0,
                    0.5,
                    0
                ]
            }
        },
        {
            id: 'minecraft:torch',
            nbt: {
                Face: 'north'
            },
            transform: {
                translation: [
                    -0.5,
                    0.5,
                    0
                ]
            }
        },
        {
            id: 'minecraft:torch',
            nbt: {
                Face: 'south'
            },
            transform: {
                translation: [
                    0.5,
                    0.5,
                    0
                ]
            }
        }
    ]
}
```
