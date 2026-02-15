@header Dynamic Trail Model
@path /data_types/properties/render/trail
This property defines **dynamic trail rendering** for a module.
The JSON value can be **a single object** or **a list of objects**.

---

## Basic Structure

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
  "display_contest": [
    "THIRD_PERSON_LEFT_HAND",
    "THIRD_PERSON_RIGHT_HAND",
    "GROUND"
  ]
}
```

You may also supply an array:

```json
"trail": [ { ... }, { ... } ]
```

---

## Fields

### Rendering Behavior

| Field            | Type  | Default | Description                 |
| ---------------- | ----- | ------- | --------------------------- |
| `maxPoints`      | int   | `255`   | Maximum stored trail points |
| `pointLifetime`  | float | `0.5`   | Seconds each point remains  |
| `sampleInterval` | float | `0.0`   | Time between trail samples  |
| `thickness`      | float | `0.02`  | Trail width                 |

---

### Visuals

| Field            | Type        | Default            | Description                                  |
|------------------|-------------|--------------------|----------------------------------------------|
| `color`          | color       | white              | Base trail color                             |
| `texture`        | resource id | `miapi:item/trail` | Trail texture                                |
| `color_provider` | string      | `"material"`       | Dynamic color source, same as Model Property |
| `transform`      | object      | identity           | Model transform applied to trail             |

---

### Debug

| Field   | Type    | Default | Description            |
| ------- | ------- | ------- | ---------------------- |
| `debug` | boolean | `false` | Shows debug trail info |

---

### Display Context Filter

Controls where the trail renders.

| Field             | Type | Default                     |
|-------------------| ---- | --------------------------- |
| `display_context` | list | third-person hands + ground |

Valid values:

* `FIRST_PERSON_LEFT_HAND`
* `FIRST_PERSON_RIGHT_HAND`
* `THIRD_PERSON_LEFT_HAND`
* `THIRD_PERSON_RIGHT_HAND`
* `GROUND`
* `GUI`
* `HEAD`
* `FIXED`

---
