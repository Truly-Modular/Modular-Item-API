@header ThrowingAbility  
@path /data_types/ability/throwing_ability

## ThrowingAbility

This ability allows the player to **throw the item like a Trident**, launching it as a projectile entity with configurable parameters for:

- Minimum hold time before the throw activates
- Cooldown after throwing
- Projectile attributes like speed, accuracy, and damage derived from item attributes

### Features

- Activated by holding and releasing right-click (use action).
- Throws the item as a projectile entity (`ItemProjectileEntity`) when the hold time exceeds the minimum.
- Applies damage, speed, and accuracy based on the item's configured attributes.

### JSON Context Fields

- `min_hold_time` — Minimum ticks the use button must be held before the item is thrown.
- `cooldown` — Cooldown duration in ticks after throwing the item.

### Usage

- Player starts using (holding right-click) the item.
- On release after minimum hold, the item is thrown as a projectile with configured properties.
- The thrown item deals damage and can be picked up.

### Additional Notes

- Projectile piercing level control is currently a TODO.
- The projectile's speed and accuracy are influenced by the item's modular attributes if present.
