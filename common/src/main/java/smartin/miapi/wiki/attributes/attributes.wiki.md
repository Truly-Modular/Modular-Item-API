@header Attributes
@path /attributes
@keywords attribute, attributes


Miapi Implements a couple of custom attributes.
Some of them will only work on Modular Items, so using them outside of Modular Items is not recommended.

# Melee/basic Attributes

### Range/Reach attributes
- `miapi:generic.reach` and `miapi:generic.attack_range` exist as cross loader range and reach attributes. these attributes dont actually exist, but will be either resolved to `forge:block_reach` or `reach-entity-attributes:reach` for fabric

### Mining Speed Attributes
- `miapi:generic.mining_speed.pickaxe`, `miapi:generic.mining_speed.axe`, `miapi:generic.mining_speed.shovel`, `miapi:generic.mining_speed.hoe` exist to easier adjust the miningspeed of modular items. This might be reworked later into Double Properties
- Only works on Modular Items

### Backstab attribute
- `miapi:generic.back_stab` This attributes increases Damage on Backstab of Melee Attacks. +2 Will increase the damage by 2, +100% will double the Damage dealt

### Armor Crushing attribute
- `miapi:generic.armor_crushing` This attributes increases Durability Damage done to armor. It is currently unused

### Shield Break attribute
- `miapi:generic.shield_break` This Attributes value is the amount of seconds a Shield gets disabled when hit.

### Item Use Speed attribute
- `miapi:generic.player_item_use_speed` This Attributes controls how fast the Player is moving while using an Item(right clicking)
- this will be reworked soon! dont use this at the moment

### Magic Damage attribute
- `miapi:generic.magic_damage` This Attributes deals additional magic Damage on hit.

### Critical Damage attribute
- `miapi:generic.crit_damage` This Attributes incrases Critical Hit Damage. +2 will increase the Damage by 2, +100% will move the the Damage on Criticals from 150% to 250%.

### Critical Chance attribute
- `miapi:generic.crit_chance` This attribute sets the chance for a Random Critical hit.


# Projectile Attributes
### Bow Draw Time attribute
- `miapi:generic.bow_draw_time` controls the Bow Draw time, is in Ticks
- works on Bows/Crossbows
- Only works on Modular Items
### Projectile Damage attribute
- `miapi:generic.projectile_damage` controls the base Damage of Projectiles, most projectiles also use their current speed in total Damage calculation
- Works on Projectiles
- Only works on Modular Items
### Projectile Speed attribute
- `miapi:generic.projectile_speed` controls the Projectiles Speed.
- Works on Bows/Crossbows and Projectiles
- Only works on Modular Items
### Projectile Accuracy attribute
- `miapi:generic.projectile_accuracy` controls the Accuracy of the Projectile
- Works on Bows/Crossbows and Projectiles
- Only works on Modular Items
### Projectile Piercing attribute
- `miapi:generic.projectile_piercing` allos Projectiles to pierce one entity like the Piercing Enchantmant
- Works on Projectiles
- Only works on Modular Items
### Critical Multiplier attribute
- `miapi:generic.projectile_crit_multiplier` by default arrows do 1.5x damage when Critical. Arrows are critical if the Bow was fully drawn or shot by Crossbow.
- Works on Projectiles
- Only works on Modular Items

# Armor Attribtues

### Ressistance attribute
- `miapi:generic.resistance` the value of this attribute is % of blank damage removed. so a value of 80 will result in a 80% damage reduction.
  This attributes main use is as a internal way to implement the blockings damage reduction.

### Swim Speed
- `miapi:generic.swim_speed` On forge this resolved to `forge:generic.swim_speed`

### Projectile Armor attribute
- `miapi:generic.projectile_armor` Works like Normal Armor, but only works against Projectiles. This is used by Crafted Chainmail Armor

### Shielding Armor attribute
- `miapi:generic.projectile_armor` Works similar to Absorption, but only works against physical Attacks and regenerates while out of Combat

## Elytra Attributes

### Elytra Turn Effiency attribute
- `miapi:generic.elytra_turn_efficiency` Controls how much speed an elytra looses while turning. Higher loses less speed

### Elytra Glide Effiency attribute
- `miapi:generic.elytra_glide_efficiency` Controls how much speed an elytra looses while not turning. Higher loses less speed



