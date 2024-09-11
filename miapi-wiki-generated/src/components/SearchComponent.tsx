import React, { useState } from 'react'
import Page from './Page'
import { useTheme } from './ThemeContext'

interface SearchProps {
	rootPage: Page
}

const SearchComponent: React.FC<SearchProps> = ({ rootPage }) => {
	const [searchTerm, setSearchTerm] = useState('')
	const [filteredItems, setFilteredItems] = useState<Map<string, string>>(new Map())
	const theme = useTheme()

	const searchPages = async (term: string) => {
		const lowerTerm = term.toLowerCase()
		const results = new Map<string, string>()

		// Helper function to scan pages
		const scanPage = async (page: Page) => {
			if (results.size >= 20) return // Limit results to 20

			if (page.header.toLowerCase().includes(lowerTerm)) {
				if (results.size < 20) results.set(page.buildLinkPath(), page.header)
			}

			if (page.description.toLowerCase().includes(lowerTerm)) {
				if (results.size < 20) results.set(page.buildLinkPath(), page.header)
			}

			const subPageEntries = Array.from(page.sub_pages.entries())
			await Promise.all(subPageEntries.map(([_, subPage]) => scanPage(subPage)))
		}

		// Perform the search
		await scanPage(rootPage)

		setFilteredItems(results)
	}

	// Use this function in your component, e.g., in `handleInputChange`:
	const handleInputChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
		const term = event.target.value
		setSearchTerm(term)

		if (term.trim() === '') {
			setFilteredItems(new Map())
		} else {
			await searchPages(term)
		}
	}

	return (
		<div
			style={{
				maxWidth: '400px',
				minWidth: '100px',
				width: '100%',
				paddingRight: '20px'
			}}
		>
			<div
				style={{
					position: 'relative',
					maxWidth: '100%',
					paddingRight: '0px'
				}}
			>
				<input
					type="text"
					value={searchTerm}
					onChange={handleInputChange}
					placeholder="Search..."
					style={{
						width: '100%',
						padding: '0.5rem',
						borderRadius: '4px',
						border: '1px solid #ccc',
						marginBottom: '0.5rem'
					}}
				/>
				{filteredItems.size > 0 && (
					<div
						style={{
							position: 'absolute',
							top: '100%', // Positions the dropdown directly below the input
							left: '0',
							width: '200px',
							maxHeight: '600px',
							overflowY: 'auto',
							border: `2px solid ${theme.headerBackgroundColor}`,
							borderRadius: '4px',
							zIndex: 1000 // Ensure the dropdown is on top
						}}
					>
						{Array.from(filteredItems.entries()).map(([id, name]) => (
							<a
								key={id}
								href={id}
								style={{
									display: 'block',
									width: '100%',
									paddingTop: '0.5rem',
									paddingBottom: '0.5rem',
									border: 'none',
									backgroundColor: theme.backgroundColor,
									borderBottom: `1px solid ${theme.headerBackgroundColor}`,
									textAlign: 'left',
									textDecoration: 'none',
									color: theme.textColor,
									cursor: 'pointer',
									transition: 'background-color 0.2s'
								}}
								onMouseOver={(e) => (e.currentTarget.style.backgroundColor = theme.mutedTextColor)}
								onMouseOut={(e) => (e.currentTarget.style.backgroundColor = theme.sidebarBackgroundColor)}
							>
								{name}
							</a>
						))}
					</div>
				)}
			</div>
		</div>
	)
}

export default SearchComponent
