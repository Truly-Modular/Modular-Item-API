@header Potion Properties
@path /data_types/properties/potion

Potion effects


# PossibleEffect JSON Format
The different potion properties re-use lists of the following effects.
Same id and strength level effects will be merged and their duration added together.

```json5
{
    "potion": "minecraft:strength",
    //modded ones work too
    "amplifier": "0",
    //starts at 0, similar to command
    "duration": "0",
    //starts at 0, similar to command
    "ambient": false,
    // same as command
    "show_particles": true,
    // if the effect has particles, default true
    "show_icon": true,
    // if the icon should be visible, default true
    "probability": "1",
    //default 1, smaller then one only results in a chance of applying the effect
    "target_self": true,
    //if the target is the origin too - depends on context
    "equipment_slot": "any",
}
```