function convertOldToNew(oldJson) {
	let newJson = {} // Holds the new JSON structure
	let issues = '' // Collects issues where automatic handling is not possible

	// Mapping of old keys to new keys
	const keyMapping = {
		texture: 'model',
		guiOffset: 'gui_offset',
		displayName: 'display_name',
		itemId: 'item_id',
		materialProperty: 'material_property',
		allowedInSlots: 'allowed_in_slots',
		repairPriority: 'repair_priority',
		allowedMaterial: 'allowed_material',
		isWeapon: 'is_weapon',
		crossbowAmmunition: 'crossbow_ammunition',
		equipmentSlot: 'equipment_slot',
		fireProof: 'fire_proof',
		healthPercent: 'health_percent',
		isPiglinGold: 'is_piglin_gold',
		itemLore: 'item_lore',
		luminiousLearning: 'luminious_learning',
		canWalkOnSnow: 'can_walk_on_snow',
		fake_enchant: 'fake_enchantments',
		otherModule: 'other_module'
	}

	// Iterate through the old JSON object
	for (let key in oldJson) {
		let value = oldJson[key]

		// Handle key renaming based on keyMapping
		if (keyMapping.hasOwnProperty(key)) {
			newJson[keyMapping[key]] = value
		} else {
			switch (key) {
				case 'attributes': {
					if (Array.isArray(value)) {
						newJson[key] = value.map((attr) => {
							let newAttr = { ...attr } // Copy the attribute object

							// Check for deprecated 'uuid' and warn, then remove it
							if (newAttr.hasOwnProperty('uuid')) {
								issues += `Deprecated attribute 'uuid' in attributes cannot be automatically handled. UUID has been removed.\n`
								delete newAttr.uuid // Remove uuid from the attribute
							}

							// Check for '**' in 'operation' and warn
							if (newAttr.operation === '**') {
								issues += `The operation '**' works differently now for attribute '${newAttr.attribute}'. Please review it manually.\n`
							}

							return newAttr // Return the modified attribute
						})
					} else {
						issues += `Expected attributes to be an array, but got ${typeof value}.\n`
					}
					break
				}
				case 'modelTransform': {
					if (value.replace) {
						newJson['model_transform'] = value.replace
					}
					if (value.merge) {
						newJson['model_transform'] = value.merge
					}
					break
				}
				// Special case for 'mining_level'
				case 'mining_level':
					issues += `'mining_level' is no longer used, replaced by a Rule-based system. Please update manually.\n`
					break

				// Remove 'name' field
				case 'name':
					// Do not add to newJson, effectively removing it
					break

				// Handle abilities: warn that they need manual porting
				case 'abilities':
					issues += `Abilities need to be ported manually using 'ability_context'.\n`
					break
				case ('area_harvest_ability', 'block', 'heavy_attack', 'circle_attack', 'eat', 'riptide'):
					issues += `Abilities need to be ported manually using 'ability_context' instead of ` + key + `.\n`
					break

				// Special warnings for enchantments and fake_enchant
				case 'enchantments':
					issues += `'enchantments' have a new system. Tags/IDs have changed and need to be manually updated.\n`
					newJson[key] = value // Keep the enchantments for now
					break

				case 'fake_enchant':
					issues += `'fake_enchant' is deprecated. Use 'fake_enchantments', but they work slightly differently.\n`
					newJson['fake_enchantments'] = value // Apply the new key but warn
					break
				default: {
					newJson[key] = value
					break
				}
			}
		}
		// Special handling for deprecated attributes
	}

	// Return both the new JSON and the issues string
	return {
		newJson: newJson,
		issues: issues.trim() // Trim to remove any trailing newline
	}
}

const fs = require('fs')
const path = require('path')

// Entry point of the script
main()
function main() {
	const rootDir = '../archery/archery-common/src/main/resources/data'
	const jsonData = {}

	readJsonFiles(rootDir, jsonData)

	fs.writeFileSync('output.json', JSON.stringify(jsonData, null, 2))
	console.log('JSON file has been generated.')
}

function readJsonFiles(dir, jsonData = {}) {
	fs.readdirSync(dir).forEach((file) => {
		const filePath = path.join(dir, file)
		const stat = fs.statSync(filePath)

		if (stat.isDirectory()) {
			// Recurse into subdirectories
			readJsonFiles(filePath, jsonData)
		} else if (file.endsWith('.json')) {
			const content = fs.readFileSync(filePath, 'utf-8')
			let result = convertOldToNew(JSON.parse(content))
			fs.writeFileSync(filePath, JSON.stringify(result.newJson, null, 4))
			if (result.issues.length > 0) {
				console.log('file ' + filePath + ' has issues\n' + result.issues)
			}
		}
	})
}
