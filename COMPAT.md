# Compatibility information

## Dedicated Compatibility
- **[Better Combat](https://modrinth.com/mod/better-combat)** has dedicated and integrated support within MIAPI.
That being said, Better Combat also sadly has a bug with NBT based compatibility. We recommend using **[Better Combat NBT Fix](https://modrinth.com/mod/better-combat-nbt-fix)** for multiplayer.
- **[Epic Fight](https://modrinth.com/mod/epic-fight)** should be fully supported well. If somebody wants to add more depth to the choices for EF, feel free to share with us.
- **[Apoli](https://github.com/apace100/origins-fabric?tab=readme-ov-file)** is the Origins power API. We have support via a property, allowing developers to use any Apoli powers on modular items.
- **[Quark](https://modrinth.com/mod/quark)** has special enchantment glint effects, and these are supported.
- **[Apotheosis](https://www.curseforge.com/minecraft/mc-mods/apotheosis)**, and its Fabric port **[Zenith](https://modrinth.com/mod/zenith)** adds Affixes to equipment, and these are supported

## Other Mod Info
- **[Better Paragliders](https://modrinth.com/mod/better-paragliders)** stamina system should work with MIAPI tools.

## Generated Material Info
Truly Modular attempts to make any mods' tool materials compatible.
This is done by scanning a mod's tools, and trying to match their stats.
Most mods should work with this system.
- **Reqirements**  
Swords and Axes need to implement as the same tool family.
- **Exceptions**  
Some mods sadly implement their tools differently or weirdly, and don't get detected because of that.
In addition, right click abilities, on-hit effects, or similar effects can't be automatically detected.
- **Quirks**
Sometimes the texturing or naming of the generated material may be odd, this is simply due to the generative nature.

## Make Your Own Compatibility  
If some compatibility is still missing, you can try your own hand at it with the **[Truly Modular Material Helper](https://truly-modular.github.io/Material-Helper/)**.
This is a web app that lets anybody make compat materials for Truly Modular, even without any coding knowledge.

## Issues
If you encounter any issues, be sure to report them to https://github.com/Truly-Modular/Modular-Item-API/issues
