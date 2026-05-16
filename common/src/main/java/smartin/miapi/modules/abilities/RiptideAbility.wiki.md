@header Riptide Ability  
@path /data_types/abilities/riptide


### Example

```json5
{
    "ability_context": [
        {
            "id": "addon:custom_riptide",
            "type": "miapi:riptide",
            "data": {
                "cooldown": 20,
                //time in ticks before this can be used again.
                //the item will get vanillas cooldown system applied
                "min_use": 10,
                //windup min time in ticks
                //defaults to 20 if not set
                "spin_duration_base": 0,
                //spin time, enchantments add to this (riptide)
                //defaults to 0
                "riptide_strength": 3,
                //strength, each level fo enchantment gives one. 0 makes u not move
                //default 0
                "require_fluid": true,
                //if the player touching a fluid is required, 
                //default true
                "custom_fluid": "minecraft:lava",
                //allows for custom fluid tag
                //if not set defaults to water (minecraft:water and not setting is different, not setting it will also allow for rain)
                //default unset
                "custom_sound": "minecraft:item.armor.equip_diamond",
                //allows for custom sound, by default players riptide sounds
            }
        }
    ]
}
```
