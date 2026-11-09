@header EntityModel Property
@path /data_types/properties/render/entity

A property that configures entity models to be rendered alongside modular items. Each entity model is transformed and positioned relative to the item.


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
