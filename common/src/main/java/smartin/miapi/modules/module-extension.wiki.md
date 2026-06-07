@header Module Extension
@path /datapack/module-extension

Module Extensions extend existing modules by adding or overriding properties. Extensions are loaded from `mod-id:miapi/module-extensions/any-path-and-file-name.json`.

## Examples

### Module Extension

```json5
{
	"id": '"miapi:super_blade",
    "merge": {
		"material": "miapi:metal/netherite",
		"durability": 2000
	}
}
```

### Tag Extension

```json5
{
	"tag": "chain_armor",
	"merge": {
		"material_indication": {
			"hardness": {
				"strength": 2,
				"info": {
					"type": "translatable"
					"translate": "tm_armory.material_indication.default.armor_points",
					"color": "dark_gray"
				}
			},
			"flexibility": {
				"strength": -1,
				"info": {
					"type": "translatable",
					"translate": "tm_armory.material_indication.chain.flexibility",
					"color": "dark_gray"'
				}
			}
		}
	}
}
```

---

## Notes

- Use `module` for module extensions
- Use `tag` for tag-based extensions
- Properties are merged with the base module
