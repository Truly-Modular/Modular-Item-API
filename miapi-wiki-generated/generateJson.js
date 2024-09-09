const fs = require('fs')
const path = require('path')

// Recursive function to read Java files and parse JavaDocs
function readJavaFiles(dir, jsonData = {}) {
	fs.readdirSync(dir).forEach((file) => {
		const filePath = path.join(dir, file)
		const stat = fs.statSync(filePath)

		if (stat.isDirectory()) {
			// Recurse into subdirectories
			readJavaFiles(filePath, jsonData)
		} else if (file.endsWith('.java')) {
			// Process Java files
			processJavaFile(filePath, jsonData)
		}
	})
}

function setNestedValue(data, path, value) {
	if (path === 'root') {
		Object.assign(data, value)
		return
	}

	const keys = path.split('/').filter((k) => k)
	let obj = data

	keys.forEach((key, index) => {
		if (!obj.sub_pages) {
			obj.sub_pages = {}
		}
		if (!obj.sub_pages[key]) {
			obj.sub_pages[key] = {} // Initialize with an empty object if it does not exist
		}
		if (index === keys.length - 1) {
			// Only set the value if it is the last key in the path
			if (typeof obj.sub_pages[key] === 'object' && obj.sub_pages[key] !== null) {
				Object.assign(value, obj.sub_pages[key])
				obj.sub_pages[key] = value
			} else {
				console.log('setting' + path)
				// Otherwise, directly set the new value
				obj.sub_pages[key] = value
			}
		} else {
			// Move deeper into the nested structure
			obj = obj.sub_pages[key]
		}
	})
}

// Process each Java file to extract JavaDoc and build JSON structure
function processJavaFile(filePath, jsonData) {
	const content = fs.readFileSync(filePath, 'utf-8')
	const lines = content.split('\n')

	let header = 'header is missing! Report this to a developer to fix this'
	let description = ''
	let data = {}
	let subPages = {}

	let javaPath = filePath.replace('..\\', '').replaceAll('\\', '/')
	let in_description = false
	let inJavaDoc = false
	let hasAnnotations = false // Flag to check for annotations
	let path = ''
	let keyWords = []

	lines.forEach((line) => {
		if (line.startsWith('/**')) {
			inJavaDoc = true
		} else if (line.includes('*/')) {
			if (hasAnnotations) {
				const pageData = {
					header,
					description,
					java: javaPath,
					data: data,
					key_words: keyWords,
					sub_pages: subPages
				}
				setNestedValue(jsonData, path, pageData)
			}
			inJavaDoc = false
			return
		} else if (inJavaDoc) {
			const unmodifiedLine = new String(line)
			const trimmed = line.replace('*', '').trim()
			if (trimmed.startsWith('@header')) {
				header = trimmed.split('@header')[1].trim()
				hasAnnotations = true
			} else if (trimmed.startsWith('@description_start')) {
				description += unmodifiedLine.split(' * @description_start')[1].trim().replace(' /r', '  /r')
				in_description = true
			} else if (in_description && !trimmed.startsWith('@')) {
				description += unmodifiedLine.replace(' * ', '').replace(' /r', '  /r')
			} else if (trimmed.startsWith('@description_end')) {
				in_description = false
			} else if (trimmed.startsWith('@path')) {
				path = trimmed.split('@path')[1].trim()
			} else if (trimmed.startsWith('@java') && trimmed.includes('false')) {
				javaPath = ''
			} else if (trimmed.startsWith('@keywords')) {
				keyWords = trimmed.replace('@keywords', '').replaceAll(' ', '').split(',')
			} else if (trimmed.startsWith('@data')) {
				let dataString = trimmed.replace('@data ', '').split(':')
				data[dataString[0]] = dataString[1]
			}
		}
	})
}

// Entry point of the script
function main() {
	const rootDir = '../common/src/main/java/smartin'
	const jsonData = {}

	readJavaFiles(rootDir, jsonData)

	fs.writeFileSync('output.json', JSON.stringify(jsonData, null, 2))
	console.log('JSON file has been generated.')
}

main()
