const fs = require('fs')
const path = require('path')

const sourceFolder = './' // Replace with the path to your source folder
const searchString = 'default' // Replace with the string you want to replace
const replaceString = 'netherite' // Replace with the string you want to use as a replacement
const requiredPath = `heavy`

function processFolder(folderPath) {
	const files = fs.readdirSync(folderPath)

	files.forEach((file) => {
		const filePath = path.join(folderPath, file)
		const stats = fs.statSync(filePath)

		if (stats.isDirectory()) {
			processFolder(filePath) // Recursive call for subfolders
		} else if (file === 'default.json' && filePath.includes(requiredPath)) {
			console.log('found json')
			// Process JSON files named "default.json"
			const data = fs.readFileSync(filePath, 'utf8')
			const updatedData = data.replace(new RegExp(searchString, 'g'), replaceString)
			let newFilePath = ''
			if (false) {
				newFilePath = filePath.replace(new RegExp(searchString, 'g'), replaceString)
				createFolderIfNotExists(newFilePath)
			} else {
				newFilePath = filePath.replace('default.json', `${replaceString}.json`)
			}
			fs.writeFileSync(newFilePath, updatedData, 'utf8')
		}
	})
}

function createFolderIfNotExists(path) {
	const folders = path.split('\\') // Assuming a forward slash as the separator
	let currentPath = ''
	console.log(`Requested fullpath is ${path}`)
	for (const folder of folders) {
		currentPath += folder + '\\'

		if (!fs.existsSync(currentPath) && !currentPath.includes('.')) {
			try {
				fs.mkdirSync(currentPath)
				console.log(`Folder created at ${currentPath}`)
			} catch (error) {
				console.error(`Error creating folder at ${currentPath}: ${error.message}`)
				break // Stop the loop if an error occurs
			}
		} else {
			console.log(`Folder already exists at ${currentPath}`)
		}
	}
}

// Ensure the destination folder exists
if (!fs.existsSync(sourceFolder)) {
	fs.mkdirSync(sourceFolder, { recursive: true })
}

// Start processing the source folder
processFolder(sourceFolder)

console.log('Files processed successfully!')
