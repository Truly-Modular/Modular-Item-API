## Active things before release
- fix dispenser behaviour
- Spikes still don't reflect dmg
- Twin throwing knife balancing

### Custom damage system:
- custom inv timers per used ID
- applies custom damage from type
- on melee attack, respect attacker attack cooldown
- should apply on ranged from shooting weapon and arrow and if toggled from armor
Example Data
```json5
{
    "custom_damage": {
        "my_addon:lighting_melee": { // the id for inv timers and CD and merging logic
            "type": "minecraft:lightning_bolt",
            "on_ranged_attack": false,
            "on_melee_attack": true,
            "respect_attack_cooldown": true,//whether the players attack cooldown should scale this damage too.
            "defender_cooldown": "1000",//default 0
            "attacker_cooldown": "100",//default 0
            "amount": "10", //damage amount
            "tooltip": "my_addon.custom.lighting.tooltip", //tooltip lang, amount, defender and attacker cd
            "header": "CoolName",//name in the stat preview
            "description": "Nope"//description in the stat preview
        }
    }
}
```
the name, tooltip and description are lang keys -> you can use %s to refer to amount, defender and attacker cd.


food storage pouch eat key?