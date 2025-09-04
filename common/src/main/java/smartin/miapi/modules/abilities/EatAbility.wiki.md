@header Eat Ability  
@path /data_types/abilities/eat

The **Eat Ability** allows modular items to function as consumable food. When right-clicked and held, the item restores
hunger and saturation, plays eating sounds, and can apply effects similar to vanilla food items.

---

### Behavior

- Triggers the eating animation when used.
- Restores hunger and saturation after a configured delay.
- Can optionally apply potion effects when consumed.
- Can optionally be consumed like food or take durability damage instead.
- Can be set to always be edible, even at full hunger.

---

### Default Fields

This ability uses the default ability fields:

- **`cooldown`**: Sets the cooldown (in ticks) after the item is eaten.
- **`min_hold`**: Automatically derived from `eat_ticks`.
- **`max_hold`**: Not directly used.

---

### Fields

| Field          | Type                                               | Description                                                      |
|----------------|----------------------------------------------------|------------------------------------------------------------------|
| `nutrition`    | `DoubleOperationResolvable`                        | Amount of hunger restored.                                       |
| `saturation`   | `DoubleOperationResolvable`                        | Saturation modifier applied.                                     |
| `eat_ticks`    | `DoubleOperationResolvable` *(default: 32)*        | Number of ticks the item must be held to consume.                |
| `cooldown`     | `DoubleOperationResolvable` *(default: 0)*         | Cooldown applied after usage.                                    |
| `durability`   | `DoubleOperationResolvable` *(optional)*           | Damage applied to item instead of consuming it.                  |
| `alwaysEdible` | `boolean` *(optional)*                             | If true, the item can be eaten even when the player is full.     |
| `effects`      | `List<FoodProperties.PossibleEffect>` *(optional)* | List of possible effects applied when consumed. Each effect has: 

- `effect`: the mob effect
- `probability`: the chance it is applied |

---

### Example

```json
{
    "ability_context": [
        {
            "id": "addon:custom_eat",
            "type": "miapi:eat",
            "data": {
                "nutrition": 4,
                "saturation": 0.6,
                "cooldown": 60,
                "eat_ticks": 32,
                "alwaysEdible": true,
                "effects": [
                    {
                        "effect": {
                            "id": "minecraft:regeneration",
                            "amplifier": 1,
                            "duration": 100
                            
                        },
                        "probability": 1.0
                    }
                ]
            }
        }
    ]
}
```

This configuration:

- Restores 4 hunger and 0.6 saturation,
- Plays the eating animation for 32 ticks,
- Has a cooldown of 60 ticks,
- Can be eaten at full hunger,
- Has a 25% chance to give Regeneration and a 10% chance to give Speed on consumption.
