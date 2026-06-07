# GuiOffsetProperty

A property that adjusts the GUI position and size of modular items. Applies translation and scaling transformations to items displayed in inventory or creative mode.

## Purpose

Allows fine-tuning the position and size of items in the GUI. Useful for creating custom item layouts, offsetting items from their default positions, or scaling items for visual effects.

## Behavior

- Offsets are applied as translations in the X and Y axes
- Size adjustments scale the item in X and Y dimensions
- Offsets are transformed by the item's local transform matrix
- Values are normalized relative to a base size of 16 pixels
- Scaling is inverse: positive size values reduce the displayed size
- Multiple properties can be merged to accumulate offsets

## JSON Structure

```json5
{
	x: 'number (optional, default = 0)',
	y: 'number (optional, default = 0)',
	sizeX: 'number (optional, default = 0)',
	sizeY: 'number (optional, default = 0)'
}
```

### Fields

- **`x`** _(number, optional, default = 0)_:
  Horizontal offset in pixels. Positive values move the item to the right.

- **`y`** _(number, optional, default = 0)_:
  Vertical offset in pixels. Positive values move the item downward.

- **`sizeX`** _(number, optional, default = 0)_:
  Horizontal size adjustment. Positive values reduce the displayed width.

- **`sizeY`** _(number, optional, default = 0)_:
  Vertical size adjustment. Positive values reduce the displayed height.

## Examples

### Offset Item Right

```json5
{
	x: 4
}
```

### Offset Item Down

```json5
{
	y: 4
}
```

### Scale Item Smaller

```json5
{
	sizeX: 4,
	sizeY: 4
}
```

### Offset and Scale

```json5
{
	x: 2,
	y: -2,
	sizeX: 2,
	sizeY: 2
}
```

### Multiple Offsets (Merge)

```json5
[
	{
		x: 2,
		y: 2
	},
	{
		sizeX: 1,
		sizeY: 1
	}
]
```
