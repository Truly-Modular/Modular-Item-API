@header On Damaged Effects Property
@path /data_types/properties/potion/on_damaged_effects

Applies potion effects passivly.
Works on held items.
Its Recommended to set the EquipmentSlot to Armor
Effects are applied to the killer
works from the attacker armor and curio and weapon


# PossibleEffect JSON Format
The different potion properties re-use lists of the following effects.
Same id and strength level effects will be merged and their duration added together.

```json5
{
    "passive_potion": {
        "potion": "minecraft:strength",
        //modded ones work too
        "amplifier": "0",
        //starts at 0, similar to command
        "ambient": false,
        // same as command
        "show_particles": true,
        // if the effect has particles, default true
        "show_icon": true,
        // if the icon should be visible, default true
        "probability": "1",
        //default 1, smaller then one only results in a chance of applying the effect
    }
}
```