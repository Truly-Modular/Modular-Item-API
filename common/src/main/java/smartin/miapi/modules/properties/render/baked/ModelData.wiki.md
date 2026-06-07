@header Model Data
@path /data_types/properties/render/model_data

The `ModelData` class represents a model configuration used by various render properties. It defines how models should be loaded with their paths, transforms, and rendering options.

---

## Basic JSON Structure

```json
{
	"path": "miapi:models/item/stone.json",
	"transform": {},
	"condition": "1",
	"color_provider": "material",
	"trim_mode": "none",
	"entity_render": false,
	"id": ""
}
```

---

## Fields

### Model Configuration

- **`path`** _(string, required)_: The path to the model JSON file. Can use `[material.texture]` as a placeholder that gets replaced with actual material variant textures (requires at least a default model).

- **`transform`** _(object, optional, default = identity)_: Transform object controlling rotation, translation, and scale.

- **`condition`** _(string, optional, default = "1")_: A condition expression that must be true for the model to render. Allows for "[material.hardness-10]" types of evaluations.

- **`color_provider`** _(string, optional, default = "material")_: The color provider to use. Options:
  - **`material`**: Uses the material color
  - **`model`**: Uses vanilla model rendering
  - **`potion`**: Uses potion color
  - **`parent`**: Uses parent module color
  - **`item.material`**: Uses item material color

- **`trim_mode`** _(string, optional, default = "none")_: The trim mode for armor items. Options:
  - **`none`**: No trim rendering
  - **`item`**: Trim rendering for items
  - **`armor_layer_one`**: First trim layer
  - **`armor_layer_two`**: Second trim layer

- **`entity_render`** _(boolean, optional, default = false)_: Whether to render the model as an entity, e.g. the model is rendered from both sides.

- **`id`** _(string, optional)_: An id for the model to be grouped/id by other behaviours.

---

## Transform Object

You can define transforms like so:

```json
"transform": {
  "rotation": {
    "x": 0.0,
    "y": 0.0,
    "z": 0.0
  },
  "translation": {
    "x": 0.0,
    "y": 0.0,
    "z": 0.0
  },
  "scale": {
    "x": 1.0,
    "y": 1.0,
    "z": 1.0
  },
  "origin": "item"
}
```

---

## Examples

### Basic Model

```json
{
	"path": "miapi:models/item/stone.json"
}
```

### Model with Transform

```json
{
	"path": "miapi:models/item/custom.json",
	"transform": {
		"rotation": {
			"x": 0.0,
			"y": 0.0,
			"z": 0.0
		},
		"translation": {
			"x": 0.0,
			"y": 0.0,
			"z": 0.0
		},
		"scale": {
			"x": 1.0,
			"y": 1.0,
			"z": 1.0
		},
		"origin": "item"
	}
}
```

### Model with Trim Mode

```json
{
	"path": "miapi:models/item/trimmed.json",
	"trim_mode": "item"
}
```

---

## Material Texture Placeholder

You can use `[material.texture]` in the path to automatically load all material texture variants:

```json
{
	"path": "[material.texture]"
}
```

This will load textures for all available materials.
