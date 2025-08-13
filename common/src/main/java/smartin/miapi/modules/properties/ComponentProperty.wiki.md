@header ComponentProperty
@path /data_types/properties/render/component
# `ComponentProperty`
The `ComponentProperty` allows you to attach **Minecraft data components** to an item via JSON.
When the module providing these components is removed, the associated components are also removed.
If a component already exists on the item, it will be overwritten by the one provided here.

---

## 📂 JSON Structure

```jsonc
{
  "components": {
    "namespace:component_id": { /* component data as JSON */ },
    "minecraft:custom_name": "|||miapi.evaluate<expression>",
    "minecraft:lore": [
      { "text": "Line 1" },
      { "text": "Line 2" }
    ]
  }
}
```

this allows for dynamic resolving as well
```jsonc
{
    "components": {
        "create:banktank_air": "|||miapi.evaluate[material.hardness]"
    }
}
```

To start the custom resolver start a string with `|||miapi.evaluate`
The rest of the string will be evaluated and the whole string will be replaced with a number.