@header 1.20.1 Wiki
@path tutorial
@keywords 1.20.1 Wiki
Welcome to the Modular-Item-API wiki! This wiki aims to provide documentation and best practices for handling datapacks and resources using the Modular Item API.  


# Getting Started

## Datapack
All of the data for truly modular has to be stored in data/miapi
for more info about the types you can visit [this page](https://github.com/Truly-Modular/Modular-Item-API/wiki/Json-Data-Types)

## Java
latest version can be checked via the [releases](https://github.com/Truly-Modular/Modular-Item-API/releases)
We recommend the usage of any Architectury Template, depending on Architectury is also recommended since Truly Modular already depends on it the miapi_version needed is the same as the github release tag.
We also recommend the usage of yarn as we use yarn as well, but mojmaps should be fine as well

### Common
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
}
```
```js
dependencies {
    modApi("com.Truly-Modular.Modular-Item-API:Truly-Modular-miapi-common:${rootProject.miapi_version}")
}
```
### Fabric
```js
dependencies {
    modApi("com.Truly-Modular.Modular-Item-API:Truly-Modular-miapi-fabric:${rootProject.miapi_version}")
}
```
### Forge
```js
dependencies {
    modApi("com.Truly-Modular.Modular-Item-API:Truly-Modular-miapi-forge:${rootProject.miapi_version}")
}
```