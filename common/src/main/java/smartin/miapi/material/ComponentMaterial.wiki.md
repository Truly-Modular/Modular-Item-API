@header Component Material
@path /datapack/material/component

Component Materials use a special components for simply runtime Materials.
Its called ``modular_material``.
Example:
```json
{
    "modular_material": {
        "parent": "miapi:metal/iron",
        "cost": 1,
        "overwrite": {
            "hardness": 5
        }
    }
}
```
- ``"parent"`` Needs to refer to a json controlled material.
- ``"cost"`` The value of this item as Ingredient, defaults to 1.
- ``"overwrite"`` The json to overwrite material data of its parent. Works the same a a Material Extension.


Overall, a better structure for this are Composite Materials, as they offer greater flexibility in most scenarios.
But for quick testing this still offers some value.