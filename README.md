The Truly Modular: miapi (Modular Item API) includes a comprehensive set of APIs, offering a wide range of features and functionalities to modders. Some of the key APIs provided by miapi include:

- Complex Module Properties: A complex Property resolving system producing predictable and easily adjustable results for Addon devs

- Runtime Property Injections and Overrides: Seamlessly inject or override properties at runtime using the Property system to inject additional Properties to a singular Module ona singular Item.

- GUI APIs: Access various GUI APIs to create GUIs with proper order of hirarchy and children logic to allow the creation of Complex GEIs.

- Modular Item Crafter GUI: Enjoy a fully functional modular item crafter GUI, providing players with a convenient way to craft and assemble their customized modular items.

- Synergy System between Modules: With a complex Synergysystem allowing unique Synergies between modules or materials allowing to target groups of each to create intricate Synergies.

- Dozens of Default Properties: Benefit from a wide range of default properties that come bundled with miapi, saving you time and effort in defining common item attributes to be used accross addons.

- Dozens of Default Materials: Utilize a diverse selection of default materials available within miapi from vanilla in addition to wide ranging mod support.

- Complex 3D Capabilities: Tap into the advanced 3D capabilities provided by miapi, enabling you to create complex 3D models or even implement your own model types.

And much more awaits you as you delve into the world of Truly Modular: miapi. Explore the limitless potential of modular item customization and elevate your modding projects to new heights.

# For Developers
latest version can be checked https://github.com/Truly-Modular/Modular-Item-API/releases  
We recommend the usage of any Architectury Template, depending on Architectury is also recommended since Truly Modular already depends on it
the miapi_version needed is the same as the github release tag
## Common
```js
repositories {
    maven {
        url 'http://trulymodular.duckdns.org/maven'
        allowInsecureProtocol = true
    }
}
```
```js
dependencies {
    modApi("com.Truly-Modular.Modular-Item-API:Truly-Modular-miapi-common:${rootProject.miapi_version}")
}
```
## Fabric
```js
dependencies {
    modApi("com.Truly-Modular.Modular-Item-API:Truly-Modular-miapi-fabric:${rootProject.miapi_version}")
}
```
## Forge
```js
dependencies {
    modApi("com.Truly-Modular.Modular-Item-API:Truly-Modular-miapi-forge:${rootProject.miapi_version}")
}
```
