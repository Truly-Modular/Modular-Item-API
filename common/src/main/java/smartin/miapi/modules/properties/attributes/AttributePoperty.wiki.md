@header Attribute Property  
@path /data_types/properties/attributes/item_attributes

The **Attribute Property** is used to modify various attributes of items. It supports setting base values, applying mathematical operations, and resolving complex attribute combinations. This is a core component of the API.

### Fields

- **`attributes`**:  
  A list of item attribute modifications. Each entry can contain the following fields:

    - **`attribute`** *(string)*:  
      The ID of the attribute to modify.

    - **`value`** *(number)*:  
      A Double Resolvable and freely settable number. Supports dynamic resolution at runtime.

    - **`operation`** *(string)*:  
      The mathematical operation to apply to the attribute. Supported operations are:
        - `+` : Addition
        - `*` : Multiplication (Additive)
        - `**` : Multiplication (Total)

    - **`slot`** *(string)*:  
      The equipment slot group that this attribute applies to. Valid options include:
        - `mainhand`, `offhand`, `hand`, `any`
        - `feet`, `legs`, `chest`, `head`
        - `armor`, `body`

    - **`targetOperation`** *(optional, string)*:  
      Specifies the operation to merge with an existing attribute. 
  This sets the type of attribute this is going to be, if its additive, % or a multiplier.

---

### Example

```json
{
  "attributes": [
    {
      "attribute": "minecraft:generic.attack_damage",
      "value": 5,
      "operation": "+",
      "slot": "mainhand"
    },
    {
      "attribute": "minecraft:generic.movement_speed",
      "value": "[material.hardness] * 1.2 + log(1.2)",
      "operation": "*",
      "slot": "feet",
      "targetOperation": "*"
    }
  ]
}
```

This example:
- Adds 5 to the attack damage when the item is in the main hand.
- Multiplies movement speed by 1.2 times the used materials hardness + the logarithm of 1.2 when the item is equipped in the feet slot.
