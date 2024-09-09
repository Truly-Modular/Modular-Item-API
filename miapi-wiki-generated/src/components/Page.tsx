class Page {
	header: string = 'header is missing! Report this to a developer to fix this'
	description: string = 'Description missing! Report this to a developer to fix this'
	java: string = ''
	data: Map<string, string> = new Map()
	sub_pages: Map<string, Page> = new Map()

	constructor(data?: Partial<Page>) {
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

				// Check if 'sub_pages' is a valid Map or object, otherwise default to an empty Map
				if (data.sub_pages && (data.sub_pages instanceof Map || typeof data.sub_pages === 'object')) {
					// Convert each subpage to a Page instance
					Object.entries(data.sub_pages).forEach(([key, value]) => {
						this.sub_pages.set(key, new Page(value))
					})
				}
			} catch (e) {}
		}
	}
}

export default Page
