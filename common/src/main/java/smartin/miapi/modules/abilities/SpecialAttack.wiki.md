@header SpecialAttackAbility  
@path /data_types/ability/special_attack

## SpecialAttackAbility

This ability provides a **stronger, charged attack** than the normal left click. It mimics vanilla attack mechanics with configurable parameters for:

- Attack **range**
- Default **sweeping** effect
- Damage **scale factor**
- Minimum hold time before attack triggers
- Cooldown between uses
- Custom particle effects on attack

The attack uses a spear-like use animation and applies damage, sweeping, and cooldowns accordingly.

> ⚠️ Note: This ability is planned to be rewritten in an upcoming ability system rework.

### Features

- Allows charging an attack by holding the use button for a configurable minimum time.
- Performs a raycast to detect entities within the configured range.
- Applies scaled damage and optional sweeping damage.
- Plays configured particle effects on the server side.
- Applies cooldown on the item after use.
- Fully configurable via JSON context, supporting merging and default values.

### JSON Context Fields

- `damage` — Damage multiplier applied to the player’s attack damage.
- `sweeping` — Sweeping attack strength.
- `range` — Maximum reach of the attack raycast.
- `min_hold` — Minimum ticks the use button must be held before the attack executes.
- `cooldown` — Cooldown duration in ticks after attack.
- `title` & `description` — UI components for lore/tooltip display.
- `particleEffect` — List of particles spawned when the attack hits.

### Usage

- Activated by right-clicking and holding the item.
- Releases a heavy attack on release after minimum hold time.
- Integrates with the player’s existing attack attributes and cooldown system.
