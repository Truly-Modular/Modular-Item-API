@header Emissivity Property
@path /data_types/properties/render/emissive

The `EmissivityProperty` defines the emissive light levels for an item or block. Emissive properties affect how much light an item or block emits, which can be used to create glowing effects in the game.

---

## Basic JSON Structure

```json
{
	"sky": -1,
	"block": -1
}
```

---

## Fields

### Light Values

- **`sky`** _(integer, optional, default = -1)_: The light level emitted in the sky. A value of `-1` means no emissive light. Valid range: `0` to `15`.
- **`block`** _(integer, optional, default = -1)_: The light level emitted from the block. A value of `-1` means no emissive light. Valid range: `0` to `15`.

---

## Behavior

- These values are applied to the item or block to control its visual appearance in lighting conditions.
- Can also be used in a mcmeta for models under `"miapi_model_data"` -> `"lightValues"`.
- A value of `-1` means no emissive light is emitted.
- Values from `0` to `15` represent different light levels.

---

## Examples

### No Emissive Light

```json
{
	"sky": -1,
	"block": -1
}
```

### Glowing Effect in Sky

```json
{
	"sky": 10,
	"block": 5
}
```

This makes the item emit light level 10 in the sky and 5 in block form.
