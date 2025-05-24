@header ToolAbility
@path /data_types/abilities/tool_abilities

`ToolAbility` is a generic base for module abilities that mimic **vanilla Minecraft tool behaviors** such as:

- **Axe stripping**
- **Hoe tilling**
- **Shovel path flattening**
- **Copper deoxidizing and waxing removal**

These abilities correspond to the typical tool interactions with blocks and follow the logic of **Forge ToolActions**, allowing tool modules to behave as expected in-game.

### Implementations

- **`axe_ability`**  
  Simulates an axe:
    - Strips logs
    - Scrapes oxidation from copper
    - Removes wax from copper blocks

- **`hoe_ability`**  
  Simulates a hoe:
    - Tills soil blocks (e.g., dirt → farmland)

- **`shovel_ability`**  
  Simulates a shovel:
    - Flattens dirt to create path blocks
    - Extinguishes lit campfires

Each of these abilities automatically plays the appropriate vanilla sound and optionally consumes tool durability.

> 📘 These actions follow the standard Minecraft behavior and integrate naturally with block interactions and animations.
