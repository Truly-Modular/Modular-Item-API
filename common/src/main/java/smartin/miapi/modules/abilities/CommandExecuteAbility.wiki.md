@header Command Execute Ability  
@path /data_types/abilities/command_execute

The **Command Execute Ability** allows a modular item to run one or more server commands when used.  
This makes it easy to trigger effects, broadcasts, or other scripted behavior directly from the item.

---

### Behavior

- When held and released after a minimum duration, the configured commands are executed.
- Commands can be run **as the player** or as the **server**.
- Execution can be positioned **at the player** or globally at the default server source.
- Cooldowns and vanilla animations are supported.

---

### Fields

- **`command`** *(list of strings)*:  
  The commands to execute. Each entry is run in order.  
  Example: `"say Hello"`, `"effect give @p minecraft:strength 10 1"`

- **`asPlayer`** *(boolean, optional, default = false)*:  
  If true, the commands execute as the player. If false, they run as the server.

- **`atPlayer`** *(boolean, optional, default = true)*:  
  If true, the execution position is set to the player’s location.

- **`minHold`** *(number, optional, default = 0)*:  
  Minimum time (in ticks) the item must be held before releasing to trigger.

- **`cooldown`** *(number, optional, default = 20)*:  
  Time (in ticks) before the ability can be used again.

- **`userAnim`** *(string, optional, default = "BOW")*:  
  The usage animation shown while holding.  
  Anims: `"NONE"`, `"EAT"`, `"DRINK"`, `"BLOCK"`, `"BOW"`, `"SPEAR"`, `"CROSSBOW"`, `"SPYGLASS"`, `"TOOT_HORN"`, `"BRUSH"`


---

### Example

```json
{
    "ability_context": [
        {
            "id": "addon:custom_command",
            "type": "miapi:command",
            "data": {
                "command": [
                    "say You used the command ability!",
                    "effect give @p minecraft:strength 10 1"
                ],
                "asPlayer": true,
                "atPlayer": true,
                "minHold": 30,
                "cooldown": 60,
                "userAnim": "BOW"
            }
        }
    ]
}
```
