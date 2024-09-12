import React, { useState, useEffect } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import Page from './Page'
import { useTheme } from './ThemeContext'

interface SidebarProps {
	page: Page
	basePath: string
	level?: number
	indentSize?: number
	hideRoot?: boolean
}

const Sidebar: React.FC<SidebarProps> = ({
	page,
	basePath,
	level = 0,
	indentSize = level * 20,
	hideRoot = false // Default to false, so root is shown unless specified
}) => {
	const [isOpen, setIsOpen] = useState(level < 2) // To toggle minimizing
	const [hasChildren] = useState(page.sub_pages.size > 0 || true)
	const theme = useTheme()
	const [isCurrentPage, setIsCurrentPage] = useState<boolean>(false)
	const location = useLocation()
	const navigate = useNavigate()

	const buildLinkPath = (subPageKey: string) => {
		const urlParams = new URLSearchParams(window.location.search)
		urlParams.set('page', basePath)
		return `?${urlParams.toString()}`
	}

	useEffect(() => {
		// Recheck the URL parameters every time the location changes
		const isCurrent = `?${new URLSearchParams(window.location.search).toString()}` === buildLinkPath('')
		setIsCurrentPage(isCurrent)
	}, [location]) // 'location' dependency ensures the effect runs when the URL changes

	// Function to handle the toggle of the sidebar
	const handleToggle = (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
		e.stopPropagation() // Prevent event from bubbling up to the Link
		setIsOpen(!isOpen)
	}

	const handleNavigation = () => {
		console.log('clicked!')
		navigate(buildLinkPath(page.header))
	}

	// Build the link path with query parameters

	// Don't render the root node header if hideRoot is true, but render its sub-pages
	return (
		<div
			style={{
				marginLeft: '0px'
			}}
		>
			{!hideRoot && (
				<div
					style={{
						borderBottom: `2px solid ${theme.headerBackgroundColor}`,
						backgroundColor: isCurrentPage ? theme.primaryColor : theme.sidebarBackgroundColor,
						padding: '3px'
					}}
					onMouseEnter={(e) => {
						e.currentTarget.style.backgroundColor = theme.secondaryColor // Hover color for link
					}}
					onMouseLeave={(e) => {
						e.currentTarget.style.backgroundColor = isCurrentPage ? theme.primaryColor : theme.sidebarBackgroundColor // Reset to default color when not hovered
					}}
					onClick={handleNavigation}
				>
					<div
						style={{
							paddingLeft: indentSize,
							display: 'flex',
							alignItems: 'center',
							justifyContent: 'space-between',
							paddingRight: '10px'
						}}
					>
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
				</div>
			)}

			{isOpen && hasChildren && (
				<ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
					{Array.from(page.sub_pages.entries()).map(([subPageKey, subPage]) => (
						<li key={subPageKey}>
							<Sidebar page={subPage} basePath={basePath + '/' + subPageKey} level={level + 1} />
						</li>
					))}
				</ul>
			)}
		</div>
	)
}

export default Sidebar
