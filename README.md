<img src="common/src/main/resources/icon.png" alt="totally modular api icon" width="256">

# Modular-Item-API 
[![](http://cf.way2muchnoise.eu/versions/904056.svg)](https://www.curseforge.com/minecraft/mc-mods/modular-item-api)
[![Modrinth](https://img.shields.io/modrinth/v/c4Uf1TKc?label=Modrinth&labelColor=%232D2D2D)](https://modrinth.com/mod/modular-item-api)

Truly Modular: miapi (Modular Item API) includes a comprehensive set of APIs, offering a wide range of features and functionalities to modders. Some of the key APIs provided by miapi include:

- Complex Module Properties: A complex Property resolving system producing predictable and easily adjustable results for Addon devs.

- Runtime Property Injections and Overrides: Seamlessly inject or override properties at runtime using the Property system to inject additional Properties to a singular Module on a singular Item.

- GUI APIs: Access various GUI APIs to create GUIs with proper order of hierarchy and children logic, to allow the creation of Complex GUIs.

- Modular Item Crafter GUI: Enjoy a fully functional modular item crafter GUI, providing players with a convenient way to craft and assemble their customized modular items.

- Synergy System between Modules: With a complex Synergy system allowing unique Synergies between modules or materials allowing to target groups of each to create intricate Synergies.

- Dozens of Default Properties: Benefit from a wide range of default properties that come bundled with miapi, saving you time and effort in defining common item attributes to be used accross addons.

- Dozens of Default Materials: Utilize a diverse selection of default materials available within miapi from vanilla, in addition to wide ranging mod support.

- Complex 3D Capabilities: By abstracting the rendering, any kind of effect can be implemented, by default this is mostly limited to block/item models for modules; entities and other special options do exist as well

Much more awaits you as you delve into the world of Truly Modular: miapi. Explore the limitless potential of modular item customization and elevate your modding projects to new heights.

# For Developers
The latest version can be checked here: https://github.com/Truly-Modular/Modular-Item-API/releases  
We recommend the usage of any Architectury Template, and depending on Architectury is also recommended since Truly Modular already depends on it.

The miapi_version needed is the same as the Github release tag.
## Repositories
Add this to every subproject.
```js
repositories {
    maven {
        url 'http://trulymodular.duckdns.org/maven'
        allowInsecureProtocol = true
    }
    maven { url 'https://maven.uuid.gg/releases' }
    maven { url 'https://maven.terraformersmc.com/' }
    maven { url 'https://maven.theillusivec4.top/' }
    maven {
        url "https://maven.jamieswhiteshirt.com/libs-release"
        content {
            includeGroup "com.jamieswhiteshirt"
        }
    }
    maven {
        name = 'GeckoLib'
        url 'https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/'
        content {
            includeGroupByRegex("software\\.bernie.*")
            includeGroup("com.eliotlash.mclib")
        }
    }
}
```
## Common
```js
dependencies {
    modApi("com.Truly-Modular.Modular-Item-API:Truly-Modular-miapi-common:${rootProject.miapi_version}-${rootProject.minecraft_version}")
}
```
## Fabric
```js
dependencies {
    modApi("com.Truly-Modular.Modular-Item-API:Truly-Modular-miapi-fabric:${rootProject.miapi_version}-${rootProject.minecraft_version}")
}
```
## Forge
```js
dependencies {
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1"))
    implementation(include("io.github.llamalad7:mixinextras-forge:0.4.1"))
    modApi("com.Truly-Modular.Modular-Item-API:Truly-Modular-miapi-forge:${rootProject.miapi_version}-${rootProject.minecraft_version}")
    forgeRuntimeLibrary(api("com.ezylang:EvalEx:3.2.0"))
}
```
