const fs = require('fs')
const path = require('path')

const sourceFolder = './' // Replace with the path to your source folder
const searchString = 'default' // Replace with the string you want to replace
const replaceString = 'stone_magma' // Replace with the string you want to use as a replacement

function processFolder(folderPath) {
	const files = fs.readdirSync(folderPath)

	files.forEach((file) => {
		const filePath = path.join(folderPath, file)
		const stats = fs.statSync(filePath)

		if (stats.isDirectory()) {
			processFolder(filePath) // Recursive call for subfolders
		} else if (file === 'default.json') {
			console.log('found json')
			// Process JSON files named "default.json"
			const data = fs.readFileSync(filePath, 'utf8')
			const updatedData = data.replace(new RegExp(searchString, 'g'), replaceString)
			const newFilePath = filePath.replace('default.json', `${replaceString}.json`)
			fs.writeFileSync(newFilePath, updatedData, 'utf8')
		}
	})
}

// Ensure the destination folder exists
if (!fs.existsSync(sourceFolder)) {
	fs.mkdirSync(sourceFolder, { recursive: true })
}

// Start processing the source folder
processFolder(sourceFolder)

console.log('Files processed successfully!')
