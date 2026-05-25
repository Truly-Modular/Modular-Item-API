@header Data Material - Properties
@path /datapack/material/data/property_fields

Now, a large part of the behaviour of modular items is controlled by properties.
Obviously, materials require some control over them as well.
This is done via the 3 Property fields,
- `properties` applied and displyed
- `display_properties` only displayed when hovering the material outside of crafting.  
should be used to indicate often hard to describe behaviour that relies on a module being present
- `hidden_properties` hidden when using material inspect, but applied on the item

now, a property section of a material json looks like

```json5
{
  "properties":{
    "module_tag":{
        "fireproof":true
    }
  }
}
```

The property `fireproof` will now be applied to all modules with the tag `module_tag`.  
To check a modules tags, simply search its json and search for `module_tags`.
But be aware synergies and extensions can change modules outside of their json and add tags to them.


by default these tags find heavy usage
`default`    
`handheld` `tool` `blade`  `head`  `axe` `pickaxe` `hammer` `hoe` `shovel`  
`armor` `helmet` `chest` `pants` `boots`  

