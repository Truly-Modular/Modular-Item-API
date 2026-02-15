@header Projectile Render Animations
@path /data_types/properties/projectile/anim

Projectile render animations allow items thrown as modular projectiles to apply **data-driven visual transforms** during rendering.

These animations are purely client-side and affect only how the projectile item is displayed — they do **not** modify physics or gameplay behavior.

## JSON Structure

All projectile animations use a **type-dispatch format**.

### Basic format

```json
{
  "projectile_animation": {
    "type": "miapi:simple_rotation",
    "axis": "VELOCITY",
    "degrees_per_tick": 20,
    "offset": 0
  }
}
```

---

## Built-in Animations

---

# Simple Rotation Animation

**Registry ID:**

```
miapi:simple_rotation
```

This animation applies a constant spin to the rendered projectile.

It is intended for:

* Thrown knives
* Axes
* or Rotating magical projectiles

---

## Fields

### `axis` (required)

Specifies the axis the projectile rotates around.

| Value      | Behavior                                |
| ---------- | --------------------------------------- |
| `X`        | Rotates around local X axis             |
| `Y`        | Rotates around local Y axis             |
| `Z`        | Rotates around local Z axis             |
| `VELOCITY` | Rotates around current motion direction |

Example:

```json
"axis": "VELOCITY"
```

This is the most common setting for thrown weapons.

---

### `degrees_per_tick` (optional)

Default: `20`

Defines how fast the item spins.
Spin speed also is scaled with projectile velocity

Units:

```
degrees per game tick
```

Higher values = faster rotation.

Example:

```json
"degrees_per_tick": 40
```

---

### `offset` (optional)

Default: `0`

Adds a static rotation offset in degrees.

Useful for:

* Adjusting model alignment
* Randomizing spin phase
* Preventing identical visuals across projectiles

Example:

```json
"offset": 15
```

### Heavy Axe

Slow heavy rotation.

```json
{
  "projectile_animation": {
    "type": "miapi:simple_rotation",
    "axis": "VELOCITY",
    "degrees_per_tick": 12
  }
}
```

## Performance Notes

This animation is extremely lightweight:

* Stateless
* No allocations per frame
* Uses only a single quaternion transform

It is safe to use on large numbers of projectiles.

---

## Extending the System (Java)

To add custom animations:

1. Implement `RenderAnimation`
2. Provide a `MapCodec`
3. Register in `ProjectileRenderAnimation.REGISTRY`

Your animation will automatically become usable in JSON.

---
