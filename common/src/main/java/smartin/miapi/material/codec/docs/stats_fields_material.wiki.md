@header Data Material - Stats
@path /datapack/material/data/stats_fields

Now, this is a bit more complicated.
Anything that isnt used a pre-used name in a material json will be attempted to be read as a number stat.
so, just by adding
```json
{
    "my_cool_stat": 5
}
```
you can do [material.my_cool_stat] within a module to retrieve this 5.  
if a stat is not set, it defaults to 0.  
### Default used Stats:
- hardness used for sword damage
- density used to offset axe from sword damage and other heavy attack weapons
- flexibility used to scale bows and other light weapons
- toughness used to scale armor toughness and similar stats
- durability base durability of tools
### Uncommon ones, used for more internal behaviour
- enchantability sets how good the enchantments are, check the minecraft wiki for more info on how this behaves
- armor_durability used to more accuratly set armor durability. is a base value multiplied later. check other materials for references
- armor_toughness directly overwrites armor toughness value
- armor_knockback_ressistance directly overwrites armor knockback ressistance
- tier used to set the rough tier of an material, used when late game materials are requested for something