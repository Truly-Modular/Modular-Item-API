@header Data Keybinds
@path /datapack/keybind
@keywords Keybind Datakeybind
Truly Modular gives datapacks the ability to add keybinds to the game.
They need to be placed under mod-id:miapi/key_binding/any-name-or-path.json
These keybinds will be send to the client on connect and the client will cache them in Truly Modulars Config.
After the first connect to the server the Keybind will be on the client until the player purges their config.
This means they cannot change keybinds until loading a world where the binding is used, 
Afterwards, the binding will always be changeable - until the config is reset.
Keybind jsons look like this

```json
{
    "category": "testing-miapi",
    "scan_code": 32,
    "entity_interaction": false,
    "block_interaction": false,
    "item_interaction": false
}
```

### category
This is the Category where you can find the binding in Minecraft's Rebind UI.

### scan_code
this is the default of the binding.

### entity_interaction
This sets if entity interactions are allowed, something like lighting a creeper on fire with a flint and steel.

### block_interaction
This sets if block interactions can be triggered, something like stripping wood.

### item_interaction
If Empty interactions (without entity or blocks) are allowed.


# Ability
To utilise new keybinds the Keybind Ability exists.
```json
{
    "keybind_ability_context":{
        "miapi:ability_id": {
            //same context as normal ability property
        }
    }
}
```
example:
```json
{
    "keybind_ability_context":{
        "miapi:test": {
            "copy_item": {
                "id":"minecraft:flint_and_steel"
            }
        }
    }
}
```
```json
{
    "category": "testing-miapi",
    "scan_code": 32,
    "entity_interaction": false
}
```
while this is placed in miapi/miapi/key_binding/test

will add a new custom keybind

```json
{
    "keybind_ability_context":{
        "miapi:test": {
            "copy_item": {
                "id":"minecraft:bow",
                "fake_item_identity": true
            }
        }
    }
}
```
