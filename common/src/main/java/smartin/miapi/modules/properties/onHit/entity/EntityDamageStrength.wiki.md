@header Entity Damage Property
@path /data_types/properties/on_hit/generic_entity_damage
A property to add conditional damage based on the attacker entity
works on melee weapons.

Examples 
```json5
{
    "entity_damage": {
        // Each key is a unique ID (ResourceLocation)
        // strength will be merged based on key and each key will get a unique stat display in the UI
        "miapi:undead_bonus": {
            // List of entity targets
            "entities": [
                "#minecraft:undead",
                // Tag (recommended for groups)
                "minecraft:zombie"
                // Direct entity ID
            ],
            // Strength applied when hitting matching entities
            // Can be a simple number...
            "strength": "5",
            // Optional display name (Minecraft text component)
            "name": {
                "text": "Bonus vs Undead"
            }
        }
    }
}
```
```json5
{
    "entity_damage": {
        "miapi:boss_bonus": {
            "entities": [
                "minecraft:ender_dragon"
                // Single entity target
            ],
            // If omitted, defaults to 0
            // Can also be a complex object depending on DoubleOperationResolvable
            "strength": "[material.hardness]"

            // No name → fallback name is generated from entity type
        }

        // Multiple entries stack:
        // If an entity matches multiple entries,
        // their strength values are added together
    }
}
```

## Notes

* `entities`: list of entity IDs or `#tags`
* `strength`: number or resolvable object (default = `0`)
* `name`: optional UI label
* All matching entries **add together**
* Empty or missing `entities` → no effect
