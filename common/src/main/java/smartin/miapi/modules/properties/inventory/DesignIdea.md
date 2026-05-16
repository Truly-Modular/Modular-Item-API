## Inventory Type
declared in
mod-id/miapi/inventory-type

is a set of features.
```json
{
    "miapi:gui_visible": true,
    "miapi:fill_behaviour": "NONE"
}
```

together with 2 properties
```json
{
    "worn_inventory_features": {
        "mymod:my_custom_inv": {
            "miapi:auto_pickup": true
        }
    }
}
```
and
```json
{
    "only_this_item_inventory_features": {
        "mymod:my_custom_inv": {
            "miapi:auto_pickup": true
        } 
    }
}
```

in java api request then a list of inventory with features sets.
each new feature can then be implemented unintrusivly by searching the 
inventories on a player.