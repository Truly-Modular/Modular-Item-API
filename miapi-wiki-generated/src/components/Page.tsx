const dictionary: { [key: string]: Page } = {}
class Page {
	header: string = 'header is missing! Report this to a developer to fix this'
	description: string = 'Description missing! Report this to a developer to fix this'
	sub_pages: Map<string, Page> = new Map()
	buildDescription: string | null = null
	java: string = ''
	data: Map<string, string> = new Map()
	parent: Page | null = null
	next: Page | null = null
	prev: Page | null = null
	keyWords: string[] = []
	isCategory: boolean = true

	constructor(data?: Partial<Page>, previousPage?: Page | null) {
		// Validate and assign values with type checks
		if (data) {
			try {
				this.header = typeof data.header === 'string' ? data.header : this.header
				this.description = typeof data.description === 'string' ? data.description : this.description
				this.java = typeof data.java === 'string' ? data.java : this.java

				// Check if 'data' is a valid Map or object, otherwise default to an empty Map
				if (data.data && (data.data instanceof Map || typeof data.data === 'object')) {
					try {
						this.data = new Map(Object.entries(data.data))
					} catch (e) {}
				}
				if (previousPage) {
					this.prev = previousPage
					previousPage.next = this
				}
				if (data.keyWords) {
					data.keyWords.forEach((key) => {
						dictionary[key] = this
					})
				}
				dictionary[this.header] = this
				/**
				if (data.sub_pages && (data.sub_pages instanceof Map || typeof data.sub_pages === 'object')) {
					// Check if 'sub_pages' is a valid Map or object, otherwise default to an empty Map
					// Convert each subpage to a Page instance
					let previous: Page | null = this
					Object.entries(data.sub_pages).forEach(([key, value]) => {
						const subPage = new Page(value, previous)
						let parsing: Page = subPage
						while (parsing.next) {
							parsing = parsing.next
						}
						previous = parsing

						subPage.parent = this
						this.sub_pages.set(key, subPage)
					})
				}*/

				if (data.sub_pages && (data.sub_pages instanceof Map || typeof data.sub_pages === 'object')) {
					// Check if 'sub_pages' is a valid Map or object, otherwise default to an empty Map
					// Convert each subpage to a Page instance
					const subPagesMap = new Map<string, Page>()

					Object.entries(data.sub_pages).forEach(([key, value]) => {
						const subPage = new Page(value, this)
						subPage.parent = this
						subPagesMap.set(key, subPage)
					})

					// Separate pages with subpages from those without
					const pagesWithSubPages = Array.from(subPagesMap.entries())
						.filter(([_, page]) => page.sub_pages.size > 0)
						.sort(([keyA, pageA], [keyB, pageB]) => pageA.header.localeCompare(pageB.header))

					const pagesWithoutSubPages = Array.from(subPagesMap.entries())
						.filter(([_, page]) => page.sub_pages.size === 0)
						.sort(([keyA, pageA], [keyB, pageB]) => pageA.header.localeCompare(pageB.header))

					// Combine sorted pages
					const sortedEntries = [...pagesWithSubPages, ...pagesWithoutSubPages]

					// Add sorted entries to the sub_pages map
					sortedEntries.forEach(([key, page]) => this.sub_pages.set(key, page))
				}
			} catch (e) {}
		}
	}

	getDescription = (): string => {
		console.log('calculating description')
		let reworked: string = this.description

		const linkPlaceholder = '__LINK__PLACEHOLDER__'
		const linkRegex = /\[([^\]]+)\]\([^\)]+\)/g

		reworked = reworked.replace(linkRegex, (match) => {
			return `${linkPlaceholder}${match}${linkPlaceholder}`
		})

		for (const key in dictionary) {
			const toLinkPage = dictionary[key]
			if (toLinkPage !== this) {
				const link: string = dictionary[key].buildLinkPath()

				const regex = new RegExp(`(?<!\\[)${key}(?=[\\s.!])`, 'gi')

				if (regex.test(reworked)) {
					console.log('inserting link for ' + key)
				}

				reworked = reworked.replace(regex, (match) => {
					return `[${key}](${link})`
				})
			}
		}

		reworked = reworked.replace(new RegExp(`\\${linkPlaceholder}`, 'g'), '')

		return reworked
	}

	buildLinkPath = (): string => {
		const urlParams = new URLSearchParams(window.location.search)
		let path = 'home'
		let parsing: Page = this

		while (parsing.parent != null) {
			let key = ''

			// Use the forEach method to iterate over the sub_pages Map
			parsing.parent.sub_pages.forEach((value, k) => {
				if (value === parsing) {
					key = k // Found the matching key
				}
			})

			// Prepend the key to the path
			path = key + '/' + path

			// Move to the parent page for the next iteration
			parsing = parsing.parent
		}

		// Update the URL parameters and return the constructed URL
		urlParams.set('page', path)
		return `?${urlParams.toString()}`
	}
}

export default Page
