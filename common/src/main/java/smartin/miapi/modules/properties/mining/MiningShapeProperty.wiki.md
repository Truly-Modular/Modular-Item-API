@header Mining Shape Property
@path /data_types/properties/mining/shape


The MiningLevelProperty manages the mining capabilities of tools, determining their effectiveness based on various rules and configurations.
These configurations include block-specific mining speeds, block blacklists, and conditions for correct tool usage.
  
By default, the mining level is influenced by the material properties associated with the tool.
Custom rules can be defined to adjust mining speeds and tool compatibilities dynamically.

Example Json:

```json
{
    "mining_shape": [
        {
            "condition": {
                "type": "block_tag",
                "tags": [
                    "mineable/shovel"
                ]
            },
            "modifier": {
                "require_same": true
            },
            "mode": {
                "type": "staggered",
                "speed": "[module.speed]"
            },
            "shape": {
                "type": "cube",
                "width": "[module.width]",
                "height": "[module.height]",
                "depth": "[module.depth]"
            }
        }
    ]
}
```
```json
{
    "mining_shape": [
        {
            "condition": {
                "type": "block_tag",
                "tags": [
                    "minecraft:logs"
                ]
            },
            "mode": {
                "type": "staggered"
            },
            "modifier": {
                "require_same": true
            },
            "shape": {
                "type": "vein",
                "size": 100,
                "max": "[module.cutter_blocks]"
            }
        }
    ]
}
```
