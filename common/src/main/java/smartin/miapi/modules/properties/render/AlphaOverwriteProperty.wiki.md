@header Alpha Overwrite
@path /data_types/properties/render/alpha_overwrite

A property that allows overriding the alpha (transparency) value of rendered models.
only targets standard Models.

- The alpha value is applied as a global multiplier to all rendered models
- Values range from 0.0 (fully transparent) to 1.0 (fully opaque)

Example
```json5
{
  "alpha_overwrite": 0.7
}
```
