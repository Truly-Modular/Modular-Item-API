@header Dynamic Trail Model
@path /data_types/properties/render/trail

The `DynamicTrailModelProperty` defines dynamic trail rendering for modules. Trails are visual effects that follow the movement of modules, creating a trail of particles or visual markers.

---

## Basic JSON Structure

```json
"trail": {
  "maxPoints": 255,
  "pointLifetime": 0.5,
  "sampleInterval": 0.0,
  "thickness": 0.02,
  "color": "#FFFFFFFF",
  "transform": {},
  "texture": "miapi:item/trail",
  "debug": false,
  "color_provider": "material",
  "display_context": [
    "THIRD_PERSON_LEFT_HAND",
    "THIRD_PERSON_RIGHT_HAND",
    "GROUND"
  ]
}
```

You may also supply an array of trail configurations:

```json
"trail": [
  {
    "maxPoints": 255,
    "pointLifetime": 0.5,
    "color": "#FF0000FF"
  },
  {
    "maxPoints": 128,
    "pointLifetime": 0.3,
    "color": "#00FF00FF"
  }
]
```

---

## Fields

### Trail Configuration

- **`maxPoints`** _(integer, optional, default = 255)_: Maximum number of trail points to store. Higher values create longer trails but use more memory.

- **`pointLifetime`** _(float, optional, default = 0.5)_: How long each trail point remains visible in seconds. Lower values create shorter, fading trails.

- **`sampleInterval`** _(float, optional, default = 0.0)_: Time in seconds between trail samples. Higher values reduce the number of trail points.

- **`thickness`** _(float, optional, default = 0.02)_: Width of the trail. Higher values create thicker trails.

- **`color`** _(string, optional)_: The base color of the trail in hex format (e.g., `"#FFFFFFFF"` for white). Can also use color provider names.

- **`texture`** _(string, optional, default = "miapi:item/trail")_: The texture to use for the trail. Can be a custom texture resource location.

- **`debug`** _(boolean, optional, default = false)_: Enable debug rendering to show trail information.

- **`color_provider`** _(string, optional, default = "material")_: The color provider to use for dynamic coloring. Options:
  - **`material`**: Uses the material color
  - **`model`**: Uses vanilla model rendering
  - **`potion`**: Uses potion color
  - **`parent`**: Uses parent module color

- **`display_context`** _(array of strings, optional)_: List of display contexts where the trail should render. Valid values:
  - **`FIRST_PERSON_LEFT_HAND`**
  - **`FIRST_PERSON_RIGHT_HAND`**
  - **`THIRD_PERSON_LEFT_HAND`**
  - **`THIRD_PERSON_RIGHT_HAND`**
  - **`GROUND`**
  - **`GUI`**
  - **`HEAD`**
  - **`FIXED`**

---

## Transform Object

You can define transforms for the trail:

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

### Simple White Trail

```json
{
	"maxPoints": 255,
	"pointLifetime": 0.5,
	"thickness": 0.02,
	"color": "#FFFFFFFF",
	"color_provider": "material"
}
```

### Red Trail with Short Lifetime

```json
{
	"maxPoints": 128,
	"pointLifetime": 0.3,
	"thickness": 0.01,
	"color": "#FF0000FF",
	"color_provider": "material"
}
```

### Multi-Color Trails

```json
[
	{
		"maxPoints": 255,
		"pointLifetime": 0.5,
		"color": "#FF0000FF",
		"color_provider": "material"
	},
	{
		"maxPoints": 128,
		"pointLifetime": 0.3,
		"color": "#00FF00FF",
		"color_provider": "material"
	}
]
```

### Trail with Custom Texture

```json
{
	"maxPoints": 255,
	"pointLifetime": 0.5,
	"texture": "miapi:item/custom_trail",
	"color": "#FFFFFFFF",
	"color_provider": "material"
}
```

### Trail with Debug Mode

```json
{
	"maxPoints": 255,
	"pointLifetime": 0.5,
	"debug": true,
	"color": "#FFFFFFFF",
	"color_provider": "material"
}
```
