const fs = require('fs')

// Lookup table for part name conversions
const partNameMap = {
	head: 'head',
	torso: 'body',
	rightArm: 'rightArm',
	leftArm: 'leftArm',
	rightLeg: 'rightLeg',
	leftLeg: 'leftLeg',
	rightItem: 'fpUsedArm' // First-person used arm
}

// Converts Player Animator JSON format to Nucleus Poses JSON format
function convertToNucleusPoses(playerAnimatorData) {
	function radiansToDegrees(radians) {
		return radians * (180 / Math.PI)
	}

	let framesMap = new Map()

	playerAnimatorData.emote.moves.forEach((move) => {
		let tick = move.tick

		if (!framesMap.has(tick)) {
			framesMap.set(tick, {})
		}

		let frame = framesMap.get(tick)

		Object.keys(partNameMap).forEach((part) => {
			if (move[part]) {
				let nucleusPart = partNameMap[part]

				if (!frame[nucleusPart]) {
					frame[nucleusPart] = { position: [0, 0, 0], rotation: [0, 0, 0], scale: [1, 1, 1] }
				}

				frame[nucleusPart].position = [move[part].x || 0, move[part].y || 0, move[part].z || 0]

				frame[nucleusPart].rotation = [
					radiansToDegrees(move[part].pitch || 0),
					radiansToDegrees(move[part].yaw || 0),
					radiansToDegrees(move[part].roll || 0)
				]
			}
		})
	})

	let frames = Array.from(framesMap.values())

	return {
		name: playerAnimatorData.name,
		frames: frames
	}
}

// Read JSON file and convert
fs.readFile('test.json', 'utf8', (err, data) => {
	if (err) {
		console.error('Error reading file:', err)
		return
	}

	try {
		const playerAnimatorJson = JSON.parse(data)
		const nucleusPosesJson = convertToNucleusPoses(playerAnimatorJson)
		fs.writeFile('test-out.json', JSON.stringify(nucleusPosesJson, null, 2), (err) => {
			if (err) {
				console.error('Error writing file:', err)
			} else {
				console.log('Conversion successful. Output saved to test-out.json')
			}
		})
	} catch (parseError) {
		console.error('Error parsing JSON:', parseError)
	}
})
