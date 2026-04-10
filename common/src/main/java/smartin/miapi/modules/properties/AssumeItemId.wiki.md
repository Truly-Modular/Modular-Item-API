@header Assume Item ID
@path /data_types/properties/assume_item_id

example:

```json5
{
    "assume_item_id": [
        "minecraft:diamond_sword"
    ]
}
```

This property assumes the identity of an item.  
This is really fucking dangerous and should be tested on every item its being used rigorously.  
This can randomly convert the item into an assumed identity, cause weird behaviours in all kinds of ways.