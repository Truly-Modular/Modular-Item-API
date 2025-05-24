@header Abilities
@path /data_types/abilities

Abilities are set via the Ability Property and are used to control the right click behaviour of modular items.

WIP: 
the ability system is planned to be extended (mostly arround the ability property to add more functionality)

The Ability Property ``miapi:abilities`` controls the abilities.  
The Property is a list of abilities that are tried in order.
if the first one cannot be executed (f.e. axe carve can only be executed on certain blocks) the next one is tried.
Many Abilities share common fields:  
- ``cooldown`` will set the item on cooldown after usage (often defaults to 0)
- ``min_hold`` how long the minimum hold time is before activation (often defaults to 0)
- ``max_hold`` how long right click can be held. (often defaults to an hour)

