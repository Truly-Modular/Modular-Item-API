@header Overlay Texture Model Property
@path /data_types/properties/render/overlay_texture_model

The `OverlayModelProperty` allows you to define custom overlay textures for modular items. This property attaches overlay textures to models with configurable color providers.

---

## Basic JSON Structure

```json
[
	{
		"modelTargetType": "id",
		"modelTargetInfo": ".*",
		"priority": 0.0,
		"allowOtherModules": false,
		"texture": "minecraft:textures/block/stone.png",
		"colorProvider": "material"
	}
]
```

---

## Fields

### Predicate Fields

- **`modelTargetType`** _(string, required)_: The type of target to match. Options:
  - **`id`**: Matches against the model ID
  - **`path`**: Matches against the model path

- **`modelTargetInfo`** _(string, required)_: A regex pattern to match against the target type
- **`priority`** _(number, optional, default = 0.0)_: Priority for applying the overlay
- **`allowOtherModules`** _(boolean, optional, default = false)_: Whether to allow other modules to apply overlays

### Overlay Data

- **`texture`** _(string, optional)_: The texture path to use as an overlay. Can be a resource location like `"minecraft:textures/block/stone.png"`.
- **`colorProvider`** _(string, required)_: The color provider to use for the overlay. Options:
  - **`this`**: Uses the material color of the current module
  - **`other`**: Uses the color provider of the source module
  - **`material`**: Uses a specific material by name
  - **`model`**: Uses vanilla model rendering
  - **`potion`**: Uses potion color
  - **`parent`**: Uses parent module color

---

## Color Provider Details

### `this`

Uses the material color of the current module being overlaid.

### `other`

Uses the color provider of the source module that provided the base model.

### `material:<name>`

Uses a specific material by its resource location name. Example: `"material:iron"`.

---

## Examples

### Using a Texture with Material Color

```json
[
	{
		"modelTargetType": "id",
		"modelTargetInfo": ".*",
		"texture": "minecraft:textures/block/stone.png",
		"colorProvider": "material"
	}
]
```

### Using a Texture with Other Module Color

```json
[
	{
		"modelTargetType": "id",
		"modelTargetInfo": ".*",
		"texture": "minecraft:textures/block/diamond.png",
		"colorProvider": "other"
	}
]
```

### Using a Specific Material

```json
[
	{
		"modelTargetType": "id",
		"modelTargetInfo": ".*",
		"texture": "minecraft:textures/block/gold.png",
		"colorProvider": "material:gold"
	}
]
```

---

## Behavior

- The overlay texture is applied to matching models based on the predicate.
- The color provider determines how the texture is colored.
- If no texture is specified, the overlay uses the default overlay texture.
