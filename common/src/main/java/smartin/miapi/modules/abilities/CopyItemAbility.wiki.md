@header Copy Item Ability  
@path /data_types/abilities/copy_item

The **Copy Item Ability** allows a modular item to behave exactly like another item. When activated, it mimics the right-click behavior, animations, and usage duration of the target item.

This is useful for replicating functionality from vanilla or custom items while still using the modular item system.

---

### Behavior

- When right-clicked, the modular item performs the same actions as the target item it is mimicking.
- All interactions (e.g., block use, entity use, item finish, ticking) are delegated to the target item.
- The copied item must be specified by its item ID (`id` field).
- The copied item's animation and behavior are reflected completely, including charge time and usage result.

---

### Fields

- **`id`** *(string, resource location)*:  
  The item ID of the target item to copy. This must be a valid item registered in the game.  
  Example: `"minecraft:bow"`, `"modid:custom_tool"`

---

### Default Fields

This ability supports the common default ability fields:
- **`cooldown`**: Item cooldown after use.
- **`min_hold`**: Minimum time the item must be held before it activates.
- **`max_hold`**: Maximum time the item can be held.

---

### Example

```json
{
  "ability": "copy_item",
  "cooldown": 0,
  "min_hold": 0,
  "max_hold": 72000,
  "id": "minecraft:bow"
}
```

This configuration:
- Makes the modular item behave like a vanilla bow,
- Has no cooldown,
- Can be held for up to 72000 ticks (1 hour),
- Triggers bow behavior, including charging and firing, when used.
