@header Model Transformation Property
@path /data_types/properties/render/model_transform

The `ModelTransformationProperty` allows you to define custom model transformations for items in different display contexts. This property controls how items are rendered in the GUI, head, and various third-person/first-person views.

---

## Basic JSON Structure

```json
{
	"gui": {},
	"head": {},
	"fixed": {},
	"ground": {},
	"firstperson_lefthand": {},
	"firstperson_righthand": {},
	"thirdperson_lefthand": {},
	"thirdperson_righthand": {},
	"overwrite": true,
	"fix_left": false
}
```

---

## Fields

### Display Context Transforms

- **`gui`** _(object, optional)_: Transform for GUI display context.
- **`head`** _(object, optional)_: Transform for head display context.
- **`fixed`** _(object, optional)_: Transform for fixed display context.
- **`ground`** _(object, optional)_: Transform for ground display context.
- **`firstperson_lefthand`** _(object, optional)_: Transform for first-person left hand view.
- **`firstperson_righthand`** _(object, optional)_: Transform for first-person right hand view.
- **`thirdperson_lefthand`** _(object, optional)_: Transform for third-person left hand view.
- **`thirdperson_righthand`** _(object, optional)_: Transform for third-person right hand view.

### Behavior Options

- **`overwrite`** _(boolean, optional, default = true)_: Whether to overwrite existing transforms.
- **`fix_left`** _(boolean, optional, default = false)_: Whether to fix left-handed rendering.

---

## Transform Object

Each display context transform uses the standard transform structure:

```json
{
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

### GUI Transform Only

```json
{
	"gui": {
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

### Multiple Display Contexts

```json
{
	"gui": {},
	"thirdperson_lefthand": {
		"rotation": {
			"x": 0.0,
			"y": 0.0,
			"z": 0.0
		},
		"translation": {
			"x": 0.5,
			"y": 0.5,
			"z": 0.5
		},
		"scale": {
			"x": 1.0,
			"y": 1.0,
			"z": 1.0
		},
		"origin": "item"
	},
	"thirdperson_righthand": {
		"rotation": {
			"x": 0.0,
			"y": 0.0,
			"z": 0.0
		},
		"translation": {
			"x": 0.5,
			"y": 0.5,
			"z": 0.5
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

### With Overwrite Disabled

```json
{
	"gui": {},
	"overwrite": false
}
```
