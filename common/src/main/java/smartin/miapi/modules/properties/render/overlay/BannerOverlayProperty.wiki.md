@header Banner Overlay Property
@path /data_types/properties/render/banner_overlay

The `BannerOverlayProperty` allows you to attach banner overlays to existing models. This property uses the attached overlay system to render banner textures on items.

---

## Basic JSON Structure

```json
[
	{
		"modelTargetType": "id",
		"modelTargetInfo": ".*",
		"priority": 0.0,
		"allowOtherModules": false,
		"type": "item_nbt",
		"model": "minecraft:banner"
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

- **`type`** _(string, required)_: The source type for the banner overlay. Options:
  - **`item_nbt`**: Uses the NBT data from the item stack
  - **`module_data`**: Reads the banner from module data

- **`model`** _(string, optional, default = "banner")_: The banner model to use. Can be a banner mode like `"banner"`, `"armor_stand"`, etc.

---

## Examples

### Using Item NBT

```json
[
	{
		"modelTargetType": "id",
		"modelTargetInfo": ".*",
		"type": "item_nbt",
		"model": "minecraft:banner"
	}
]
```

### Using Module Data

```json
[
	{
		"modelTargetType": "id",
		"modelTargetInfo": ".*",
		"type": "module_data",
		"model": "miapi:custom_banner"
	}
]
```

---

## Banner Modes

Common banner modes include:

- **`banner`**: Standard banner rendering
- **`armor_stand`**: Banner on armor stand
- **`item`**: Banner as item
