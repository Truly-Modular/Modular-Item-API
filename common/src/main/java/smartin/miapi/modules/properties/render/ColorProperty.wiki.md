@header Color Property
@path /data_types/properties/render/color

The `ColorProperty` allows you to set a custom color for modular items. This property acts as a tint rather then overwriting material coloring.

---

## Basic JSON Structure

```json
"color": "#FF0000"
```

---

## Fields

### Color Configuration

- **`color`** _(string, required)_: The color value in hex format (e.g., `"#FF0000"` for red). The value can be a hex color string like `"#RRGGBB"` or `"#RRGGBBAA"` for alpha.

---

## Behavior

- The color is applied to items that support dyeing.
- If the material cannot be dyed, the color has no effect.
- If the item already has a dyed color, this property can override it.

---

## Examples

### Red Color

```json
{
	"color": "#FF0000"
}
```

### RGBA Color with Alpha

```json
{
	"color": "#FF000080"
}
```

This sets a semi-transparent red color (20% opacity).
