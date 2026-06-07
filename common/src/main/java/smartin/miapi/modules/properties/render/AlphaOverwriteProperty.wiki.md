# AlphaOverwriteProperty

A property that allows overriding the alpha (transparency) value of rendered models.

## Purpose

Provides a simple way to control the transparency level of modular item renders. Useful for creating ghost-like effects, faded appearances, or blending items with backgrounds.

## Behavior

- The alpha value is applied as a global multiplier to all rendered models
- Values range from 0.0 (fully transparent) to 1.0 (fully opaque)
- Can be merged with other properties using standard merge rules
- Does not affect item functionality, only visual appearance

## JSON Structure

```json5
{
  "alpha_overwrite": 'number (required)'
}
```

### Fields

- **`value`** _(number, required)_:
  The alpha value to apply. Must be between 0.0 and 1.0.
  - `0.0` = fully transparent
  - `0.5` = 50% transparent
  - `1.0` = fully opaque

## Examples

### Semi-Transparent Item

```json5
{
  "alpha_overwrite": 0.7
}
```
