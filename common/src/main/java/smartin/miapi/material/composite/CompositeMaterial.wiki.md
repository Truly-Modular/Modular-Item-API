@header Composite Material
@path /datapack/material/composites

Composite Materials allow you to build a material by stacking multiple composites, each of which modifies the base material in some way.
This enables dynamic, data-driven creation of new materials that combine visual, statistical, and functional traits.

How It Works

A CompositeMaterial consists of:
Base Material (starts as DefaultMaterial)

List of Composite Modifiers
- These can change:
- Color & palette
- Name & UI grouping
- Stats (add, multiply, set)
- Ingredient behavior
- Material properties
And more

Composites are applied in order, transforming the base material step-by-step into the final result.

Composite materials are serialized using the /datapack/material/composites path, using the codec:

```
{
  "composites": [
    {
      "type": "miapi:material_color",
      "color": "#FF00FF"
    },
    {
      "type": "miapi:material_name",
      "name": "My Fancy Material"
    }
  ]
}

```

When using Composite Materials, it is technically possible to write every composite directly onto an ItemStack.
While this is useful for testing or quick comparisons, it is *not recommended* for regular gameplay or for
large-scale datapack usage, because the full composite data is stored directly on each item and increases NBT size.

For production use, we strongly recommend the **Datapack Composite** system (`miapi:data_composite`).  
A Datapack Composite allows you to define a complete composite setup inside a datapack and reference it by ID in-game.
This significantly reduces the amount of data written to each ItemStack and keeps your content clean and modular.

A Datapack Composite must be placed under:
``mod-id:miapi/data_composites``

Each entry there defines a composite “template” consisting of any number of composite modifiers.

You can study a real example here:  
[Modular Material Datapack](
https://github.com/Truly-Modular/Modular-Item-API/blob/release/1.21-mojmaps/modular_material/data/modular-material/miapi/data_composite/line.json)

To use a Datapack Composite in an item, reference it via the composite type:
``miapi:data_composite``


This also enables *dynamic material inheritance*:  
The data Composite has a field for a material, if this is set its in-datapack defined composites to the same materials if they don't specify one.  
The Modular Material Datapack makes strong use of this feature.
