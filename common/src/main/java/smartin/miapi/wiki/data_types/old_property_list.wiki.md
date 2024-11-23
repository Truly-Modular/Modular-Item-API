@header Old Properties List
@path /data_types/properties/old_list
@keywords 


Default Property Types:
# Property Types;
## Double Property
- Double Properties consist of a complex number. They have the full capabilities like a Attribute has
- allowed jsons include
    - `"propertyKey":5`
    - `"propertyKey":"5"`
    - `"propertyKey":"5*20*[material.density]"`
    - `"propertyKey":{
      "operation": "**",
      "value": "0.5"
      }`
    - `"propertyKey":true` -> this is equivalent to 1
- They are resolved like attributes, so first everything gets added, then all multipliers with "*" as Operation are added together, then all Multipliers with "**" are used to multiply the result.
## Boolean Property
- behaves like a double Property, it is considered true if the total value is above 0

# Full Property List

## Esential Properties for Modules

### Name Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/NameProperty.java)
- Key: `name`
- This property is a String.
- This property is REQUIRED on each module
- Example :
  `"name": "blade_dagger",`

### Slot Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/SlotProperty.java)
- Key: `slots`
- Example :
  `"slots": {
  "0": {
  "allowed": ["sword_blade","sword_blade_small","sword_blade_large"],
  "transform": {"rotation": {"x": 0.0, "y": 0.0, "z": 0.0}, "translation": {"x": 3.0, "y": 3.0, "z": 0.0},"scale": {"x": 1.0, "y": 1.0, "z": 1.0}}
  }
  },`
- this Property manages allowed submodules. its a Map by slotId(a Integer)
- Each moduleSlot has a List of allowed modules and a transform from the local position

### Allowed Slots Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/AllowedSlots.java)
- Key: `allowedInSlots`
- Example :
  `    "allowedInSlots": ["sword_guard","polearm_blade"],`
- This Property is a JSON array
- This is a List of Strings that Modules' Slots can allow for

### Allowed Material Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/AllowedMaterial.java)
- Key: `allowedMaterial`
- This property is a JSON Object.
- Example :
  `    "allowedMaterial": {
  "allowedMaterials": [
  "wood",
  "stone",
  "metal",
  "crystal"
  ],
  "cost": 1
  },`
- the JSON key allowedMaterials is a List of allowed material keys/material group names.
- the Cost key is a number and describes how much of that material is needed.

# Render/Model Related Properties

### Model Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/render/ModelProperty.java)
- Key: `texture`
- This Property is a list of Models.
- Each Model is built with the following subproperties
    - path : The Path to the Model.json this should be in miapi:, because the model needs to be already loaded and miapi loads all models in its directories. This can be any [Java Block/Item Model](https://web.blockbench.net/)
    - transform : a Tranform (position, rotation and scale data of the model
    - id : Optional - an ID to identify this model for other actions
    - color_provider : allows for different Color Providers. They color the model. Allows for "material", "model", "potion" and "parent"
    - trim_mode : Optional - defaults to nono - Determines if Trims are applied to this item. allows for "armor_layer_one", "armor_layer_two", "item".
    - entity_render : Optional, default false. If true the model will be rendered from both sides
    - condition : Optional - defaults to 1. This is a complex number, if this is resolves 0 the model wont be rendered


### Model Overly Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/render/OverlayModelProperty.java)
- Key: `overlay_texture_model`
- This Property is a list of Overlays Models.
- Each Model is built with the following subproperties
    - texture : The Path to the texture to overlay to the model
    - modelTargetType : the type of identification used to target models. This can target Models from all modules. allows for "id" and "path"
    - modelTargetInfo : A Regex match for either id or path of the model
    - colorProvider : allows for "this"(uses this material as a colorProvider) and "other" using the targets colorProvider. also allows for all other
      color_providers from the ModelProperty
    - priority : A way to order overlays, lower ones are applied first

# Ability Properties
Abilities control Right Click behaviour of an Item

### Ability Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/AbilityProperty.java)
- Key: `abilities`
- This Property is a List of String keys, it order determines the Order of Right Click Abilites
- Abilities set higher are tried first, f.e. `abilities:[        "axe_ability", "throw"]` this would use an axes Strip ability, but if not targeting anything it will try to throw the item instead

### Block Ability Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/BlockProperty.java)
- Key: `blocking`
- This Property is a [complex Number](https://github.com/Truly-Modular/Modular-Item-API/wiki/Json-Data-Types#complex-numbers)
- Its main use is for scaling the Block Ability

### Edible Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/EdibleProperty.java)
- Key: `edible`
- This Property allows the modular item to be eaten. Make sure to add "eat" to the abilities property.
- It is a JSON object that can define the following:
    * `hunger`: The amount of hunger to receive from consuming the item as a [complex number](https://github.com/Truly-Modular/Modular-Item-API/wiki/Json-Data-Types#complex-numbers)
    * `saturation`: The amount of saturation to receive from consuming the item as a [complex number](https://github.com/Truly-Modular/Modular-Item-API/wiki/Json-Data-Types#complex-numbers)
    * `eatingSpeed`: (optional) A multiplier for how fast the item should be eaten as a [complex number](https://github.com/Truly-Modular/Modular-Item-API/wiki/Json-Data-Types#complex-numbers)
    * `durability`: (optional) defaults to 0 (if 0 the item will be consumed.) positive values detract durability on eat [complex number](https://github.com/Truly-Modular/Modular-Item-API/wiki/Json-Data-Types#complex-numbers)
    * `alwaysEdible`: (optional) Whether the item is always edible(can be eaten at full hunger)
    * `effects`: (optional) A list of status effects to receive from consuming the item - each entry in the list consists of the following keys:
        * `effect`: The id of the status effect. F.E. "minecraft:resistance"
        * `duration`: How long the potion effect lasts, in ticks
        * `amplifier`: The amplifier of the potion effect(starting at 0)
        * `ambient`: (optional) Whether the potion effect is ambient
        * `showParticles`: (optional) Whether to show the potion effect's particles
        * `showIcon`: (optional) Whether to show the potion effect's icon(?)
- Example:
```
  {
    "abilities": ["eat"],
    "edible": {
      "hunger": 5,
      "saturation": 3.8,
      "eatingSpeed": 0.5,
      "durability": -5,
      "alwaysEdible": true,
      "effects": [
        {
          "effect": "minecraft:hunger",
          "duration": 40,
          "amplifier": 0
        }
      ]
    }
  }
```


# Projectile Related Property


### Airdrag Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/AirDragProperty.java)
- Key: `air_drag`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Json-Data-Types#complex-numbers)
- This Property scales how fast a Projectile looses Speed in air, needs to be between 0 and 1, values above 1 dont lose any speed.

### Waterdrag Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/WaterDragProperty.java)
- Key: `water_drag`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Json-Data-Types#complex-numbers)
- This Property scales how fast a Projectile looses Speed in Water, needs to be between 0 and 1, values above 1 dont lose any speed.


### WaterGravity Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/WaterGravityProperty.java)
- Key: `water_gravity`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Json-Data-Types#complex-numbers)
- This Property scales how heavy a Player is considered under water and how fast he sinks

### Arrow Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/ArrowProperty.java)
- Key: `is_arrow`
- This Property is a [Boolean Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/#boolean-property)
- This Property determines if a Projectile is internally treated like an arrow or thrown Item, this changes some damage calculations

### IsCrossbow Projectile Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/IsCrossbowShootAble.java)
- Key: `crossbowAmmunition`
- This Property is a [Boolean Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/#boolean-property)
- If true lets Item be shot as a Crossbow Projectile

### Enderpearl Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/EnderpearlProperty.java)
- Key: `is_enderpearl`
- This Property is a [Boolean Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/#boolean-property)
- If true lets Projectiles behaves like an Enderpearl on impact, teleporting the player


### Teleport Target Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/TeleportTarget.java)
- Key: `teleport_target`
- This Property is a [Boolean Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/#boolean-property)
- If true lets Projectiles teleports the entity hit by this like it has eaten a Chorus Fruit

# Enchantment Related Properties

### Allowed Enchants Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/EnchantmentProperty.java)
- Key: `enchantments`
- This Property is a List of allowed or Forbidden Enchants
- Example `"enchantments":"allowed:"["minecraft:sharpness","minecraft:protection"]`
- Alows for `allowed` and `forbidden` respectively
- This Property determines if an Enchantment can be put onto an Item

### Crafting Enchants Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/CraftingEnchantProperty.java)
- Key: `crafting_enchants`
- This Property will enchant the Item on Craft with those Enchantments, if the Item allows for this Enchantment
- This Property is a Map of Enchantment Identifiers
- Example `"crafting_enchants":{"minecraft:sharpness":5,"minecraft:protection":3}`
- Respects [This](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List#allowed-enchants-property)

### EnchantmentTransformer Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/EnchantmentTransformerProperty.java)
- Key: `enchantment_transformerst`
- This Property will adjust existing enchantments to your liking, if multiple transformers are applied on one item they will transform in order
- Unlike previous "fake_enchant" property this has no native gui represenation, we recommend the usage of the gui stat property
- Example
```
{  
    "enchantment_transformers": [  
        {  
            "enchantment": "minecraft:sharpness",  
            "level": "[old_level]+5"  
        },  
        {  
            "enchantment": "minecraft:efficiency",  
            "level": "[old_level]*2"  
        },  
        {  
            "enchantment": "minecraft:fire_aspect",
            "level": "ceil([old_level]/3)"
        }  
    ]  
}
```
- Respects [This](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List#allowed-enchants-property)

# Other Properties

### Attribute Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/AttributeProperty.java)
- Key: `attributes`
- this is a fairly complex Property that aims to allow the setting of any Attribute.
- It is a JSON array of individual Properties
- each entry in the array has the following Properties
    * `attribute` : this is the Identifier of the Attribute in question. this is required
    * `value` : this is a [complex Number](https://github.com/Truly-Modular/Modular-Item-API/wiki/Complex-Number) and the value of the Attribute . this is required
    * `operation` : This is the Operation used. allowed values are `+`,`*`,`**`. this is required
    * `slot` : The slot in witch the attribute should be active. this is required
    * `target_operation` : the Target Operation to merge to
    * use the following only together and if you know what you are doing, uuid should be used for Attackdamage and Attackspeed to be green
        * `uuid` : the UUID of the attribute. Same UUID without `seperateOnItem = true' will be merged into a single add on the Item this is optional
        * `seperateOnItem` : a Boolean value, defaults to false. if set to true this Entry will be listed seperatly on the Item

### Armor Penetration Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/EquipmentSlotProperty.java)
- Key: `armor_pen`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/_edit#double-property)
- This Property is ingame scaled with a limited growth function so high values don't get ridiculous.


### Crafting Condition Property Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/CraftingConditionProperty.java)
- Key: `crafting_condition`
- this property contains 3 Conditions:
    * (_optional_ <default: none>) `visible`: ([Condition](https://github.com/Truly-Modular/Modular-Item-API/wiki/Json-Data-Types#conditions)) if this module should be showen in the Crafting UI
    * (_optional_ <default: none>) `craftable`: ([Condition](https://github.com/Truly-Modular/Modular-Item-API/wiki/Json-Data-Types#conditions)) if this module should be clickAble in the Crafting UI
    * (_optional_ <default: none>) `on_craft`: ([Condition](https://github.com/Truly-Modular/Modular-Item-API/wiki/Json-Data-Types#conditions)) if this module should be Craftable in the Crafting UI

### Channeling Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/ChannelingProperty.java)
- Key: `channeling`
- This Property is a [Boolean Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/#boolean-property)
- If true lets Projectiles summon lightning on Impact like if it was enchanted with Channeling

### Cryo Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/CryoProperty.java)
- Key: `cryo`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/#double-property)
- This Property applies Cryo on ArrowHit if it is on the arrow. The higher the value, the longer and higher the applied Cryo effect is.  
  For details check the Java implementation

### Display Name Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/DisplayNameProperty.java)
- Key: `displayName`
- This Property is a simple String containing the Language key for the Items Displayname

### Durability Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/DurabilityProperty.java)
- Key: `cryo`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/#double-property)
- This Property determines the total Durability of the Item

### Emissive Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/EmissiveProperty.java)
- Key: `emissive`
- This Property determines the light than a module will render with. In Minecraft, the light an item renders with is determined by two values: sky and block light. Sky light is the amount of light the sky is shining on the item(0-15), and block light is the amount of light being shined by nearby light sources(a torch, for example).
- Set to "true" to always render this module/material with full light. Set as an object with "sky" and "block" keys to individually determine each light value. -1 will make that value have no effect and use the normal light instead.
- Example:
```
  {
    "emissive": {
      "sky": 15,
      "block": -1
    }
  }
```

### Equipmentslot Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/ArmorPenProperty.java)
- Key: `equipmentSlot`
- This Property is a Simple String Property
- Example: `"equipmentSlot":"helmet"`
- This Property should make the Item Equipable on the Equipment Slot. Its heavily recommneded to use an ItemId that is allowed on that slot as well


### Fake Item Tag
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/FakeItemTagProperty.java)
- Key: `fake_item_tag`
- This Property is a List if Tag Identifiers
- It tries to fake the Item tag onto the Item. This might cause issues with certain tags or mods and might not be detected Correctly

### FireProof Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/FireProof.java)
- Key: `fireProof`
- This Property is a [Boolean Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/#boolean-property)
- if set to true items wont burn and behave like Netherite Items

### Food Exhaustion
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/ExhaustionProperty.java)
- Key: `food_exhaustion`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/_edit#double-property)
- this property introduces a passive Food Drain onto the Player if a Item with this is worn

### Fortune Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/FortuneProperty.java)
- Key: `fortune`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/_edit#double-property)
- this property increases the Fortune Level of the Item by its rounded value

### Fracturing Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/FortuneProperty.java)
- Key: `fracturing`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/_edit#double-property)
- this property increases Damage the lower Durability is

### Gui Stat Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/GuiStatProperty.java)
- Key: `gui_stat`
- This Property is a map of numbers to additionally display in the gui.
- if two modules share the same key their numbers can be compared with each other
- Example:
```
{
    "gui_stat": {
        "ht_treechop_gui": {
            "value": "ceil( min(200,(([material.mining_level] + [material.hardness] / 10 ) ^ 3 + 2))-0.5)",
            "min": 0,
            "max": 20,
            "header": {
                "translate": "miapi.arsenal.tree_mining"
            },
            "description": {
                "translate": "miapi.arsenal.tree_mining.description"
            }
        }
    }
}
```

### Health Percentage Damage Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/HealthPercentDamage.java)
- Key: `healthPercent`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/_edit#double-property)
- this property increases Damage scaled with the current Health of the Target

### IllagerBane Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/IllagerBane.java)
- Key: `illagerBane`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/_edit#double-property)
- this property increases Damage to illagers and other raid related Mobs

### Immolate Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/ImmolateProperty.java)
- Key: `immolate`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/_edit#double-property)
- this property burns the holder, the target and so on (This will get reworked soon)

### IsPiglinGold Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/IsPiglinGold.java)
- Key: `isPiglinGold`
- This Property is a [Boolean Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/#boolean-property)
- If set to true Piglins wont attack people wearing this as a Armor Module

### ItemId Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/ItemIdProperty.java)
- Key: `itemId`
- This Property is a simple Identifier String
- Any string here needs to be registered in Java and all defaults can be seen [here](https://github.com/Truly-Modular/Modular-Item-API/blob/e7b07ae43a970a74b13fbc9d4fc15888f99a6114/common/src/main/java/smartin/miapi/registries/RegistryInventory.java#L242)

### Item Lore Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/compat/LoreProperty.java)
- Key: `itemLore`
- This Property allows for a list Item Lores.
- The "position" element can be "top" or "bottom". Top appears under item name, bottom appears above the advanced tooltip section.
- The "priority" element is optional, defaulting to 0. Lower numbers will make this appear above other text, higher numbers will make it appear below other text.
- The "text" element allows for [this](https://minecraft.wiki/w/Formatting_codes#Use_in_custom_language_packs)
- Example:
```
{
    "itemLore": [{
      "position" : "top",
      "text" : "TextElement"
    }]
  }
```

### Leeching Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/LeechingProperty.java)
- Key: `leeching`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/_edit#double-property)
- this is a basic lifesteel property


### Luminous Learning Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/LuminousLearningProperty.java)
- Key: `luminiousLearning`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/_edit#double-property)
- this property increases Xp Drops from Blocks and Drops

### Mining Level Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/MiningLevelProperty.java)
- Key: `mining_level`
- This Property manages the Mining level
- it is a map `mining_level:{"axe":"[material.mining_level]"}`


### Module Stats Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/ModuleStats.java)
- Key: `module_stats`
- This Property supplies stats that are similar to material stat, reachable by "[module.custom_stat]"
- The idea is to give Synergies and Material Properties some guidance on for stats
- Same with the Material stats, if a stat is not set it defaults to 0
- For Obvious reasons of self reference no complex Numbers are allowed here
- the property is a simple JSON Map
- "[module.cost]" will always be redirected to the cost of the [AllowedMaterials property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List#allowed-material-property)
- Example:
```
{
    "module_stats": {
        "gem_size": 1,
        "power": 5
    }
  }
```

### Module Tag Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/TagProperty.java)
- Key: `tag`
- This Property is a List of String Tags. Its main use is to easily Identify groups of modules for synergies and other behaviour tweaks


### Pillagers Guard Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/PillagesGuard.java)
- Key: `pillagerGuard`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/_edit#double-property)
- this property decreases Damage from Pillagers and other Raid related Mobs

### Priority Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/PillagesGuard.java)
- Key: `priority`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/_edit#double-property)
- this property determins the Ordering in the GUI, lower Prio is sorted Higher

### Rarity Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/RarityProperty.java)
- Key: `rarity`
- This Property allows you to set custom rarities, by default "common","uncommon","rare","epic", they are white, yellow,aqua and light purple respectivly

### RepairPriority Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/RepairPriority.java)
- Key: `repairPriority`
- This Property is a [Double Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/_edit#double-property)
- this property manages witch modules are allowed to contribute to the Repair Material, lower number means higher prio, all modules with the same number are allowed

### Riptide Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/RiptideProperty.java)
- Key: `riptide`
- This Property manges Riptide behaviour, to enable Riptide also use "riptide" in the Ability Property
- for an example look at the [Trident Modules](https://github.com/Truly-Modular/Arsenal/blob/0b98028590df16236a3e497baaa68062572090aa/common/src/main/resources/data/miapi/modules/blade/blade_trident.json#L94)

### Snow Walk Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/CanWalkOnSnow.java)
- Key: `canWalkOnSnow`
- This Property is a [Boolean Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/#boolean-property)
- if set to true and on Boots the player can walk over powdered snow like if hes wearing leather boots

### ToolOrWeaponProperty Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/ToolOrWeaponProperty.java)
- Key: `isWeapon`
- This Property is a [Boolean Property](https://github.com/Truly-Modular/Modular-Item-API/wiki/Property-List/#boolean-property)
- If set to true the Item will behave more like a Weapon(loose 2 dura on blockbreak, one on hitting sth, if false the reverse is true)


# Compat Properties

### ApoliPower Property
- [java](https://github.com/Truly-Modular/Modular-Item-API/blob/main/common/src/main/java/smartin/miapi/modules/properties/compat/ApoliPowersProperty.java)
- Key: `apoli_powers`
- This Property allows for a list of apoli Powers.
- Uses [Apolis itemModifiers](https://origins.readthedocs.io/en/1.8.1/misc/item_modifiers/apoli/add_power/) system to decode without the need to define a function
- Example:
```
{
    "apoli_powers": [{
      "powerId" : "origins:fall_immunity",
      "slot" : "mainhand",
      "isHidden" : false,
      "isNegative" : false
    }]
}
```



