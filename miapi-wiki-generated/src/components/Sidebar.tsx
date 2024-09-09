import React, { useState } from 'react'
import { Link } from 'react-router-dom'
import Page from './Page'
import { useTheme } from './ThemeContext'

interface SidebarProps {
	page: Page
	basePath: string
	level?: number // To control indentation for sub-pages
	indentSize?: number // Customize the indentation distance
	hideRoot?: boolean // Flag to hide the root node
}

const Sidebar: React.FC<SidebarProps> = ({
	page,
	basePath,
	level = 0,
	indentSize = 20,
	hideRoot = false // Default to false, so root is shown unless specified
}) => {
	const [isOpen, setIsOpen] = useState(true) // To toggle minimizing
	const [hasChildren] = useState(page.sub_pages.size > 0 || true)
	const theme = useTheme()

	// Function to handle the toggle of the sidebar
	const handleToggle = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
		e.stopPropagation() // Prevent event from bubbling up to the Link
		setIsOpen(!isOpen)
	}

	// Build the link path with query parameters
	const buildLinkPath = (subPageKey: string) => {
		const urlParams = new URLSearchParams(window.location.search)
		urlParams.set('page', basePath)
		return `?${urlParams.toString()}`
	}

	// Don't render the root node header if hideRoot is true, but render its sub-pages
	return (
		<div style={{ paddingLeft: indentSize, marginBottom: '5px' }}>
			{!hideRoot && (
				<div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
					<Link
						to={buildLinkPath(page.header)}
						style={{
							color: theme.textColor,
							textDecoration: 'none',
							display: 'flex',
							alignItems: 'center'
						}}
					>
						{page.header}
					</Link>
					<div
						onClick={handleToggle}
						style={{
							cursor: 'pointer',
							marginLeft: '10px' // Space between the link and the arrow
						}}
					>
						<span>{page.sub_pages.size > 0 ? (isOpen ? '▼' : '▶') : ''} </span> {/* Toggle arrow */}
					</div>
				</div>
			)}

			{isOpen && hasChildren && (
				<ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
					{Array.from(page.sub_pages.entries()).map(([subPageKey, subPage]) => {
						console.log(subPageKey)
						return (
							<li key={subPageKey}>
								<Sidebar page={subPage} basePath={basePath + '/' + subPageKey} level={level + 1} indentSize={indentSize} />
							</li>
						)
					})}
				</ul>
			)}
		</div>
	)
}

export default Sidebar
