// RelatedPages.tsx
import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Page from './Page'
import { useTheme } from './ThemeContext'

interface RelatedPagesProps {
	page: Page
}

const RelatedPages: React.FC<RelatedPagesProps> = ({ page }) => {
	const theme = useTheme()
	const [parentWidth, setParentWidth] = useState<number>(200)

	useEffect(() => {
		const updateWidth = () => {
			const contentElement = document.getElementById('PageContent')
			if (contentElement) {
				setParentWidth(contentElement.getBoundingClientRect().width)
			}
		}

		updateWidth() // Set initial width
		window.addEventListener('resize', updateWidth)
		return () => window.removeEventListener('resize', updateWidth)
	}, [])

	// Gather related pages
	const relatedPages = [
		page.parent && { name: page.parent.header, link: page.parent.buildLinkPath() },
		...Array.from(page.sub_pages.values()).map((subPage) => ({
			name: subPage.header,
			link: subPage.buildLinkPath()
		})),
		...(page.parent
			? Array.from(page.parent.sub_pages.values())
					.filter((subPage) => subPage !== page)
					.map((subPage) => ({
						name: subPage.header,
						link: subPage.buildLinkPath()
					}))
			: [])
	].filter(Boolean)

	if (relatedPages.length === 0) {
		return null
	}

	return (
		<div
			style={{
				backgroundColor: theme.sidebarBackgroundColor,
				borderRadius: '10px',
				padding: '10px',
				boxSizing: 'border-box', // Ensure padding and border are included in the width
				position: 'fixed',
				top: '80px',
				width: parentWidth
			}}
		>
			<h3>Related Pages</h3>
			<ul style={{ listStyleType: 'none', padding: '0' }}>
				{relatedPages.map((relatedPage, index) => (
					<li key={index}>
						<Link
							to={relatedPage ? relatedPage.link : ''}
							style={{
								textDecoration: 'none',
								color: theme.primaryColor,
								display: 'block',
								marginBottom: '8px',
								marginLeft: '10px'
							}}
						>
							{relatedPage ? relatedPage.name : ''}
						</Link>
					</li>
				))}
			</ul>
		</div>
	)
}

export default RelatedPages
