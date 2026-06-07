# BannerModelProperty

A property that configures banner patterns to be rendered on modular items. Supports multiple banner models with different types and transformations.

## Purpose

Allows rendering custom banner patterns on modular items. Each banner can be of different type (item NBT, module data, or baked model) and can be positioned and transformed independently.

## Behavior

- Supports three banner types: `item_nbt`, `module_data`, and `baked_model`
- Banner models are transformed relative to the item
- Parent banners can be anchored to the equipped slot
- Module data banners read from the module instance
- Baked models are loaded from the resource location

## JSON Structure

```json5
{
	type: 'string (required)',
	model: 'string (required)',
	modelType: 'string (optional)',
	transform: 'object (optional)'
}
```

### Fields

- **`type`** _(string, required)_:
  The banner type. Valid values:
  - `"item_nbt"` - Uses the item stack's NBT
  - `"module_data"` - Reads from module instance
  - `"baked_model"` - Uses a baked model resource location

- **`model`** _(string, required)_:
  The banner pattern resource location or pattern identifier.

- **`modelType`** _(string, optional)_:
  The model type for parent banners. Defaults to "parent".

- **`transform`** _(object, optional)_:
  Transform matrix to apply to the banner. Defaults to identity transform.

## Examples

### Item NBT Banner

```json5
{
	type: 'item_nbt',
	model: 'model',
	transform: {
		translation: [0, 0.5, 0],
		scale: [1, 1, 1]
	}
}
```

### Module Data Banner

```json5
{
	type: 'module_data',
	model: 'model',
	transform: {
		translation: [0, 0.3, 0]
	}
}
```

### Baked Model Banner

```json5
{
	type: 'baked_model',
	model: 'custom_banner_pattern',
	modelType: 'mainhand',
	transform: {
		translation: [0, 0.25, 0],
		scale: [0.9, 0.9, 0.9]
	}
}
```

### Multiple Banners

```json5
[
	{
		type: 'item_nbt',
		model: 'minecraft:banners/patterns/blue',
		transform: {
			translation: [0, 0.5, 0]
		}
	},
	{
		type: 'baked_model',
		model: 'custom_banner_overlay',
		modelType: 'offhand',
		transform: {
			translation: [-0.2, 0.5, 0]
		}
	}
]
```
