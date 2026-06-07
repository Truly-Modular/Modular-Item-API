# EntityModelProperty

A property that configures entity models to be rendered alongside modular items. Each entity model is transformed and positioned relative to the item.

## Purpose

Allows rendering custom entities (mobs, items, particles) as part of a modular item's visual representation. Each entity can have its own NBT data, transformations, and rendering options.

## Behavior

- Entities are created from their `EntityType` and optionally loaded with NBT data
- Each entity is transformed using the specified `Transform` matrix
- Entities can tick independently if `tick` is enabled
- Full bright rendering can be enabled for better visibility
- Spin animations can be applied via `spin` settings

## JSON Structure

```json5
{
	id: 'string (required)',
	nbt: 'object (optional)',
	transform: 'object (optional)',
	tick: 'boolean (optional, default = false)',
	fullBright: 'boolean (optional, default = false)',
	spin: 'object (optional)'
}
```

### Fields

- **`id`** _(string, required)_:
  The entity type resource location. Example: `"minecraft:enderman"`, `"minecraft:item"`

- **`nbt`** _(object, optional)_:
  NBT compound tag to load onto the entity. Used to set entity properties like name, health, equipment, etc.

- **`transform`** _(object, optional)_:
  Transform matrix to apply to the entity. Defaults to identity transform.

- **`tick`** _(boolean, optional, default = false)_:
  If true, the entity will update each tick for animations or behavior.

- **`fullBright`** _(boolean, optional, default = false)_:
  If true, the entity ignores ambient light and renders at full brightness.

- **`spin`** _(object, optional)_:
  Spin animation settings from `MaterialIcons.SpinSettings`.

## Examples

### Simple Enderman Entity

```json5
{
  "entity_model": [{
    id: 'minecraft:enderman',
    nbt: {
      Name: 'Custom Enderman',
      Health: 20
    },
    transform: {
      translation: [0, 0.5, 0],
      scale: [1, 1, 1]
    },
    tick: true,
    fullBright: true
  }]
}
```

### Item Entity with Spin

```json5
{
  "entity_model": [{
	id: 'minecraft:item',
	nbt: {
		Item: {
			id: 'minecraft:diamond_sword',
			Count: 1
		}
	},
	transform: {
		translation: [0, -0.25, 1],
		scale: [0.8, 0.8, 0.8]
	},
	spin: {
		axis: 'Y',
		speed: 3,
		enabled: true
	}
  }]
}
```
