@header Data Keybinds
@path /datapack/keybind
@keywords Keybind Datakeybind
Truly Modular gives datapacks the ability to add keybinds to the game.
They need to be placed under mod-id:miapi/key_binding/any-name-or-path.json
These keybinds will be send to the client on connect and the client will cache them in Truly Modulars Config.
After the first connect to the server the Keybind will be on the client until the client purges their config.
Keybind jsons look like this

```json
{
    "category": "testing-miapi",
    "scan_code": 32,
    "entity_interaction": false
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
