@header Riptide Ability  
@path /data_types/abilities/riptide

The **Riptide Ability** propels the player through the air when used near water or rain, replicating vanilla riptide behavior on modular items.

---

### Behavior

- Activates on right-click and hold; releases after the minimum hold time.
- Requires the player to be touching water, rain, or a configured custom fluid.
- Launches the player with configurable strength, evaluated against the Riptide enchantment level.
- Plays a spin animation for a configurable duration after launch.
- Applies a cooldown after each use.
- Only executes on the server side.

---

### Fields

| Field                | Type                                        | Description                                                                             |
|----------------------|---------------------------------------------|-----------------------------------------------------------------------------------------|
| `cooldown`           | `DoubleOperationResolvable` *(default: 20)* | Ticks before the ability can be used again.                                             |
| `min_use`            | `DoubleOperationResolvable` *(default: 10)* | Minimum ticks the item must be held before it activates.                                |
| `spin_duration_base` | `DoubleOperationResolvable` *(default: 20)* | Base spin animation duration in ticks. Each Riptide enchantment level adds to this.     |
| `riptide_strength`   | `DoubleOperationResolvable` *(optional)*    | Launch strength. Evaluated against the vanilla Riptide enchantment strength; defaults to that value (0 without an enchant). |
| `require_fluid`      | `boolean` *(default: true)*                 | If true, the player must be touching a fluid to activate.                               |
| `custom_fluid`       | `string` *(optional)*                       | Fluid tag ID to require instead of water. Omitting this also allows activation in rain. |
| `custom_sound`       | `SoundEvent` *(optional)*                   | Sound played on launch. Defaults to the vanilla riptide sound.                          |

---

### Example

```json
{
    "ability_context": [
        {
            "id": "addon:custom_riptide",
            "type": "miapi:riptide",
            "data": {
                "cooldown": 20,
                "min_use": 10,
                "spin_duration_base": 0,
                "riptide_strength": 3,
                "require_fluid": true,
                "custom_fluid": "minecraft:lava",
                "custom_sound": "minecraft:item.armor.equip_diamond"
            }
        }
    ]
}
```

This configuration:
- Sets a 20-tick cooldown after each use,
- Requires a 10-tick hold before launching,
- Disables the spin animation,
- Sets a fixed launch strength of 3,
- Requires the player to be touching a fluid,
- Requires lava specifically instead of water or rain,
- Plays the diamond armor equip sound on launch.
