# Compat information
## Dedicated Compat
These Mods have received dedicated compatibility patches/bugfixes in truly modular to ensure compatibility  
- **Better Combat**:
dedicated and integrated support  
But Better Combat sadly has a bug with NBT based Compat. We recommend using https://modrinth.com/mod/better-combat-nbt-fix for multiplayer
- **Epic Fight**: weapons are mostly supported, armor sadly is not    
- **Apoli**: is Origins power api, we have support via a property, allowing developers to use any apolipowers on modular items  
- **HT Treechop**: If HT Treechop is loaded, woodcutting axes and normal axes will synergies with it  
- **Project MMO**: light weight support so modular items are somewhat picked up by it.  
- **Quark**: Quarks Enchanting Glint is somewhat supported on modular items  
- **Apotheosis/Zenith**: Minor bugfixes for certain colliding effects  

## Generated Material Info
Truly Modular attempts to make any mods Tool Materials compatible.
This is done by scanning a Mods tools and trying to match their stats.
Most mods should work with this system.
- **Reqirements**  
A Sword and an Axe implement as the same Toolfamily.
- **Exceptions**  
Some mods sadly implement their Tools differently/weirdly and dont get detected because of that.
In addition right click abilities and on-hit effects or similar effects cant be automaticly detected sadly
- **Quirks**  
Sometimes the texturing or Naming of the Generated Material may be odd, this is simply due to the generative nature.
  
## Make your own Compat  
  
If some compat is still missing you can try to your own hand at it with the [Material Helper](https://truly-modular.github.io/Material-Helper/)
This is a Website that lets anybody make compat materials for Truly Modular, even without any coding knowledge.

## Issues
if you encounter any issues be sure to report them https://github.com/Truly-Modular/Modular-Item-API/issues
