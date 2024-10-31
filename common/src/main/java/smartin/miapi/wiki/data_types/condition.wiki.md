@header Conditions
@path /data_types/condition
@keywords condition, conditions

Conditions allow in a simple way to create complex conditions for certain things to apply

A common use is that ANY json loaded by truly modular supports loading conditions
the key "load_condition"://actual condition object
can be used in any json, this can make files [conditional on other mods](https://github.com/Truly-Modular/Modular-Item-API/wiki/Json-Data-Types#mod_loaded)

## Default Structure

```json
{
    "type": "condition_type"
}
```

## **Types**

### true
This condition is always True no matter what

### not
This condition negates a subcondition  
Example:
```json
{
	"type": "not",
	"condition": {
		"type": "true"
	}
}
```  
### and
This condition has the sub element "conditions" witch are a list of any number of conditions. if ALL are true this is conisdered true  
Example:
```json
{
	"type": "and",
	"conditions": [
		{
			"type": "not",
			"condition": {
				"type": "true"
			}
		},
		{
			"type": "true"
		}
	]
}
```
### or
This condition has the sub element "conditions" witch are a list of any number of conditions. if at least one is true this is conisdered true  
Example:
```json
{
	"type": "or",
	"conditions": [
		{
			"type": "not",
			"condition": {
				"type": "true"
			}
		},
		{
			"type": "true"
		}
	]
}
```
## Module Specific Conditions
These conditions only are able to return true if used in relation to modules
### parent
Parent Conditions shift the Condition Context to the parent module if applicable, if no parent module is there this is considered false  
Example:
```json
{
	"type": "parent",
	"condition": {
		"type": "not",
		"condition": {
			"type": "true"
		}
	}
}
```
### child
Child Conditions shift the Condition Context to the child module if applicable, if no child module is there this is considered false  
It is tested on all children and as long as one does return true this is considered true  
Example:
```json
{
	"type": "child",
	"condition": {
		"type": "not",
		"condition": {
			"type": "true"
		}
	}
}

```
### otherModule
This Condition shifts the context to all modules on the Item, if the subCondition is true for at least one module this is considered true  
Example:
```json
{
	"type": "otherModule",
	"condition": {
		"type": "not",
		"condition": {
			"type": "true"
		}
	}
}

```

### module
This Condition tests if the current module has a certain ID, it is best used with some of the conditions above  
Example:
```json
{
	"type": "module",  
	"module": "blade_sword"  
}

```  
### material
This Condition tests if the current module has a certain material, it is best used with some of the conditions above  
Example:
```json
{
	"type": "material",
	"material": "iron"
}

```   
### material  count
This Condition tests if the current iteme has a certain amount of modules with a certain material
Example:
```json
{
	"type": "material_count",
	"material": "iron",
        "count":5
}

```  
### tag
This Condition tests if the current module has a certain tag, module-tags are used to simply identify groups of related modules like blades or handles  
Example:
```json
{
	"type": "tag",
	"tag": "blade"
}

```    

## Other Types
### advancement
This Condition tests if the current assosiated Player has a certain advancement  
to find the correct Ids the /advancement command is very usefull  
Example:
```json
{
	"type": "advancement",
	"advancement": "minecraft:story/enter_the_nether"
}


```

### Item in Inventory
This Condition tests if the current assosiated Player has a amount of items of that type in their inventory
Example:
```json
{
	"type": "item_in_inventory",
	"item": "minecraft:dirt",
        "count": 5
}


```
### mod_loaded
This Condition tests another mod is loaded. this is usually used as a loadCondition for optional jsons  
as mod the mod ID of the other mod is used  
Example:
```json
{
	"type": "mod_loaded",
	"mod": "bettercombat"
}

```