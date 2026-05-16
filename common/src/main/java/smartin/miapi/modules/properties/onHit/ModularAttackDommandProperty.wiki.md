@header Damage Command Property  
@path /data_types/properties/damage_command

The **Damage Command Property** executes one or more server commands when an attack event occurs.  
Commands can trigger from melee weapon hits, direct melee attacks, or ranged attacks.

---

### Behavior

- Commands execute automatically when the configured attack condition is met.
- Multiple command contexts may be defined.
- Each context can target different attack types and execution behavior.
- Commands execute in the order they are defined.

---

### Fields

- **`command`** *(string or list of strings)*:  
  The command or commands to execute.  
  Example: `"say hit"` or `["say hit", "effect give @p strength 5 1"]`

- **`onMeleeWeapon`** *(boolean, optional, default = false)*:  
  Triggers when the modular weapon successfully hurts an enemy.

- **`onMeleeAttack`** *(boolean, optional, default = false)*:  
  Triggers on direct melee damage events.

- **`onRangedAttack`** *(boolean, optional, default = false)*:  
  Triggers on indirect or ranged damage events.

- **`onBefore`** *(boolean, optional, default = false)*:  
  If the commands are run before or after applying damage

- **`runAt`** *(string, optional, default = `"DEFENDER"`)*:  
  Determines where the command executes.  
  Values:
    - `"DEFENDER"`
    - `"ATTACKER"`
    - `"NONE"`

- **`runAs`** *(string, optional, default = `"NONE"`)*:  
  Determines who executes the command.  
  Values:
    - `"DEFENDER"`
    - `"ATTACKER"`
    - `"NONE"`    
    
    usually you would want the server executing the command.

---

### Example

```json5
{
    "damage_command": [
        {
            "command": [
                //these will be executed in order
                "say Direct melee hit!",
                "effect give @s minecraft:strength 5 1"
            ],
            //will run from all worn gear on a melee attack.
            "onMeleeAttack": true,
            //run at attacker to give potion to attacker
            "runAt": "ATTACKER",
            "runAs": "ATTACKER"
        },
        {
            //this will run on all defenders of the ranged attack.
            //this is run before the entity would be dead.
            "command": "say Ranged attack landed!",
            "onRangedAttack": true,
            "runAt": "DEFENDER",
            "runAs": "NONE"
        }
    ]
}
```