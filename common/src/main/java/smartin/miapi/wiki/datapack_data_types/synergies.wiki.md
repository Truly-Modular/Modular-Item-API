@header Synergies
@path /datapack_data_types/synergies
@keywords synergy, synergies

- a separate JSON found in miapi/synergies
- used to augment modules under certain conditions
- Examples of synergies can be found [here](https://github.com/Truly-Modular/Arsenal/tree/master/arsenal-common/src/main/resources/data/miapi/synergies).

Synergies are a system to allow the dynamic addition/changes of properties under certain conditions.
They are seperate json located in miapi:synergies/unique-path-and-name  
They apply on a module level of their conditions are meet.

# Types
### **None**
if you leave out the "type" entry you need to use a module key as a key for the inner data.  
Example:
``` json  
{
    "blade_katana": {
        "condition": {
            "type": "otherModule",
            "condition": {
                "type": "module",
                "module": "handle_polearm"
            }
        },
        "replace": {
            "displayName": "miapi.module.blade_naginata.name",
            "itemId": "miapi:modular_naginata"
        }
    }
}
```
### `tag`
This applies to modules based on a module tag. This is not a Item Tag. for more information you can visit [here](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List#module-tag-property)
``` json  
{
    "type": "tag",
    "one_handed_sword": {
        "condition": {
            "type": "otherModule",
            "condition": {
                "type": "tag",
                "tag": "one_handed_handle"
            }
        },
        "replace": {
            "better_combat_config": {
                "parent": "bettercombat:sword"
            }
        }
    }
}
```
### `all`
This will apply to ALL modules.
``` json  
{
    "type": "all",
    "all": {
        "condition": {
            "type": "true"
        },
        "merge": {
            "can_child_be_empty": false
        }
    }
}
```
### `material`
This allows you to apply additional porperties under certain conditions. Visit Materialbased Properties first and only if [Material Properties](https://github.com/Truly-Modular/Modular-Item-API/wiki/Materials#properties) dont fit your usecase use this.
``` json  
{
    "type": "material",
    "diamond": {
        "condition": {
            "type": "true"
        },
        "replace": {
            "durability": "[material.durability]* 0.5"
        }
    }
}
```
# Inner data
The inner data is always behind a key, the inner json.
### `condition`
This is Required!  
This is a [Condition Object](https://github.com/Truly-Modular/Modular-Item-API/wiki/Json-Data-Types#conditions) under the key "condition"
### `replace`
This is a property map that will remove the existing property data of the module in question and fully replace it with this. Use this with caution
### `merge`
This merges the new data with potentially existing data. This should be the default option to use.
### `remove`
This is a list of property keys to remove. All data assisioted with the listed Properties will be removed from the module.