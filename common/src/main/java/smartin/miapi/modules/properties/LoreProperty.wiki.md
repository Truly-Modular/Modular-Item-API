@header Lore Property
@path /data_types/properties/item_lore

The `item_lore` property adds custom lore lines to an item's tooltip.

## Fields

- `text` (`Component`) — lore text to display.
- `position` (`String`) — where the lore appears. Valid values:
    - `"top"`
    - `"bottom"`
- `priority` (`float`, default: `0.0`) — used when sorting multiple lore entries.

## Simplified Form

A lore entry may be provided as a simple text component:
in that case it defaults to being displayed at the top with priority 0
```json
{
    "item_lore": {
        "translate": "example.lore"
    }
}
```

This is equivalent to:

```json
{
    "item_lore": [
        {
            "text": {
                "translate": "example.lore"
            },
            "position": "top",
            "priority": 0.0
        }
    ]
}
```
Effectively the simplified form is just the part within the "text" portion pushed to the outside.

## Full Example

```json5
{
    "item_lore": [
        {
            // Text displayed in the tooltip
            "text": {
                "translate": "example.weapon.description"
            },
            // Appears near the top of the tooltip
            "position": "top",
            // Lower values are sorted first
            "priority": 0.0
        },
        {
            // Supports any valid Minecraft Component
            "text": {
                "text": "Forged by ancient smiths",
                "italic": true,
                "color": "gray"
            },
            // Appears after all normal tooltip information
            "position": "bottom",
            // Will be sorted after entries with lower priority
            "priority": 10.0
        },
        {
            // Another lore line
            "text": {
                "translate": "example.weapon.special"
            },
            // Also placed at the top
            "position": "top",
            // Sorted before the first entry because the priority is lower
            "priority": -5.0
        }
    ]
}
```

## Notes

- `text` uses Minecraft's standard `Component` format [Minecraft wiki](https://minecraft.wiki/w/Text_component_format).
- `position` cannot be ommitted in the normal format and will be set to `"top"` in the simplified one.
- If `priority` is omitted, it defaults to `0.0`.
- Multiple lore entries can be defined.
- Entries are sorted by `priority` using ascending order (lowest displayed first).