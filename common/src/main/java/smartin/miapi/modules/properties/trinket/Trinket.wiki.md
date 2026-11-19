@header Modular Trinket
@keywords Trinket,Curio,Accessories
@path /data_types/properties/trinket

Truly Modular refers to curio/accessories as "Trinkets".
Its a term used to describe either
Modular Curio/Accessories are quite simple to create.
```json
{
    "trinket_slots": [
        "belt"
    ],
    "attributes": [
        {
            "attribute": "miapi:generic.attack_range",
            "value": "20",
            "operation": "+",
            "slot": "trinket"
        },
        {
            "attribute": "minecraft:generic.movement_speed",
            "value": "0.5",
            "operation": "+",
            "slot": "trinket"
        }
    ]
}
```
Slots in `trinket_slots` are applied to curio or accessories, whatever is currently loaded.
if neither is loaded, this does nothing.
To add slots to a player, one should check Curio or Accessories repsectivly, there is no unified way todo so.

To add trinket-only attributes, the slot needs to be set to "trinket" instead, it will auto-adapt them from the Trinket Slots then.
