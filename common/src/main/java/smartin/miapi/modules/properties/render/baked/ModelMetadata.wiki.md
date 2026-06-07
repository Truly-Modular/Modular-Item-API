@header Model Metadata
@path /data_types/properties/render/model_metadata

The `ModelMetadata` class handles custom `.mcmeta` data for models. It defines color providers and light values that are read from the `miapi_model_data` section of model metadata files.

---

## Basic JSON Structure

```json
{
	"modelProvider": "material",
	"lightValues": {
		"sky": -1,
		"block": -1
	}
}
```

---

## Fields

### Metadata Configuration

- **`modelProvider`** _(string, optional)_: The color provider to use for the model. Must be a valid color provider name. Valid options:
  - **`material`**: Uses material color
  - **`model`**: Uses vanilla model rendering
  - **`potion`**: Uses potion color
  - **`parent`**: Uses parent module color
  - **`item.material`**: Uses item material color

- **`lightValues`** _(object, optional)_: Light values for the model. Structure:
  - **`sky`** _(integer, optional, default = -1)_: Light level emitted in the sky
  - **`block`** _(integer, optional, default = -1)_: Light level emitted from the block

---

## Behavior

- This metadata is read from `.mcmeta` files in the `miapi_model_data` section.
- The `modelProvider` determines how the model is colored.
- The `lightValues` control the emissive light levels.
- A value of `-1` means no emissive light is emitted.

---

## Examples

### No Custom Metadata

```json
{}
```

### With Color Provider

```json
{
	"modelProvider": "material"
}
```

### With Light Values

```json
{
	"lightValues": {
		"sky": 10,
		"block": 5
	}
}
```

### With Both

```json
{
	"modelProvider": "material",
	"lightValues": {
		"sky": 10,
		"block": 5
	}
}
```
