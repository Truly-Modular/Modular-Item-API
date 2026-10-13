@header Synergies
@path /datapack/synergies

Synergies are used to adjust a ModuleInstances properties based on condition.
They apply as an additional property resolver live during item resolve.
Miapi resolves properties by applying the resolvers in this order logged during initial startup:
```
[Render thread/INFO] (ModularItem API) registered resolver: miapi:miapi/module -> the base module data
[Render thread/INFO] (ModularItem API) registered resolver: miapi:miapi/module_data -> data additionally written to a module
[Render thread/INFO] (ModularItem API) registered resolver: miapi:material_property -> properties from the material
[Render thread/INFO] (ModularItem API) registered resolver: miapi:skin -> skin related properties
[Render thread/INFO] (ModularItem API) registered resolver: miapi:synergies -> from the synergy itself, can react to all the things above.
```
They can be found/added in mod-id:miapi/synergies/any-path-and-file-name.json
If the goal is to modify a module without additional conditions use Module Extensions instead.
they apply onto base module during the initial load and synergies can safely react to those changes as well.

### `type`
The Type of synergy, can either by "tag", "material", "all" or "module"
- **tag** applies to module with the tag set by the module tag property   
- **all** applies to all modules    
- **material** applies to a certain material  

### `module/material/tag mentioned above`
- data type you wish to reference

### `condition`
A complex Condition, parsed from the perspective of the target module

### `properties`
this is the key, the inner data is split into
- `replace` Replaces the property, erasing previous data
- `merge` properties to be merged into the modules property
- `remove` a list of properties to remove

### Example
```json
{
    "type": "module",
    "module": "tm_arsenal:pommel/cross",
    "condition": {
        "type": "other_module",
        "condition": {
            "type": "tag",
            "tag": "blade_large"
        }
    },
    "properties": {
        "merge": {
            "attributes": [
                {
                    "attribute": "generic.attack_speed",
                    "value": "0.05",
                    "operation": "**",
                    "slot": "mainhand"
                }
            ]
        }
    }
}
```
