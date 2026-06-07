@header Attached Model Property
@path /data_types/properties/render/attached_model

The `AttachedModelProperty` is an abstract base class for data-driven model attachments. It provides a framework for attaching custom models to existing models based on predicates and conditions.

---

## Basic JSON Structure

```json
[
	{
		"modelTargetType": "id",
		"modelTargetInfo": ".*",
		"priority": 0.0,
		"allowOtherModules": false,
		"data": {
			// Custom data specific to the attachment type
		}
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
- **`priority`** _(number, optional, default = 0.0)_: Priority for applying the attachment
- **`allowOtherModules`** _(boolean, optional, default = false)_: Whether to allow other modules to apply attachments

### Custom Data

- **`data`** _(object, required)_: Custom data specific to the attachment type. The structure depends on the specific attachment implementation.

---

## Behavior

- This is an abstract base class used by specific attachment implementations like `BannerOverlayProperty` and `OverlayModelProperty`.
- Attachments are evaluated based on their predicates.
- The `priority` field determines the order in which attachments are applied.
- Attachments can preload resources like models or textures before rendering.

---

## Examples

### Banner Overlay Attachment

```json
[
	{
		"modelTargetType": "id",
		"modelTargetInfo": ".*",
		"priority": 0.0,
		"allowOtherModules": false,
		"data": {
			"type": "item_nbt",
			"model": "minecraft:banner"
		}
	}
]
```

### Overlay Texture Attachment

```json
[
	{
		"modelTargetType": "id",
		"modelTargetInfo": ".*",
		"priority": 0.0,
		"allowOtherModules": false,
		"data": {
			"texture": "minecraft:textures/block/stone.png",
			"colorProvider": "material"
		}
	}
]
```
