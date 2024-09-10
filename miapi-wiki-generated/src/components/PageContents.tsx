import React from 'react'
import Page from './Page'
import ReactMarkdown from 'react-markdown'
import { Link } from 'react-router-dom'
import NavButton from './NavButton'
import { useTheme } from './ThemeContext'

interface PageContentsProps {
	page: Page
	branch_name: string
}

const PageContents: React.FC<PageContentsProps> = ({ page, branch_name }) => {
	const theme = useTheme()
	const desc: string = page.getDescription()
	return (
		<div style={{ position: 'relative', minHeight: '300px', padding: '20px' }}>
			{/* Link to Parent Page (if exists) */}
			{page.parent && (
				<div style={{ position: 'absolute', top: '20px', left: '20px' }}>
					<Link
						to={page.parent.buildLinkPath()}
						style={{
							textDecoration: 'none',
							color: theme.primaryColor,
							fontWeight: 'bold'
						}}
					>
						{`< ${page.parent.header} Page`}
					</Link>
				</div>
			)}

			{/* Centered Header */}
			<h1 style={{ textAlign: 'center' }}>{page.header}</h1>

			{/* Top Navigation Buttons (slightly below the header) */}
			<div
				style={{
					marginTop: '10px', // Spacing below the header
					marginBottom: '20px', // Spacing between buttons and content
					display: 'flex',
					justifyContent: 'space-between',
					alignItems: 'center',
					position: 'relative',
					padding: '0 50px' // Adjust the buttons to be closer to the center by adding padding
				}}
			>
				{/* Previous Button (Bound to the left, only rendered if available) */}
				{page.prev && <NavButton to={page.prev} direction="prev" />}

				{/* Next Button (Bound to the right, only rendered if available) */}
				{page.next && <NavButton to={page.next} direction="next" />}
			</div>

			{/* Description with minimum space */}
			<div
				style={{
					maxWidth: '800px',
					margin: '0 auto',
					minHeight: '200px' // Ensure a minimum height for the content area
				}}
			>
				<ReactMarkdown>{desc}</ReactMarkdown>

				{/* Page data (Key-value pairs) */}
				<ul>
					{Array.from(page.data.entries()).map(([key, value]) => (
						<li key={key}>
							<strong>{key}: </strong>
							{value}
						</li>
					))}
				</ul>
			</div>

			{/* Bottom Navigation Buttons */}
			<div
				style={{
					marginTop: '20px',
					paddingBottom: '40px',
					display: 'flex',
					justifyContent: 'space-between',
					alignItems: 'center',
					position: 'relative',
					padding: '0 50px' // Adjust the buttons to be closer to the center
				}}
			>
				{/* Previous Button (Bound to the left, only rendered if available) */}
				{page.prev && <NavButton to={page.prev} direction="prev" />}

				{/* Java Implementation Link (Centered) */}
				<div style={{ flexGrow: 1, textAlign: 'center' }}>
					<Link
						to={`https://github.com/Truly-Modular/Modular-Item-API/tree/${branch_name}/${page.java}`}
						style={{
							textDecoration: 'none',
							color: theme.secondaryColor,
							fontWeight: 'bold'
						}}
					>
						Java Implementation
					</Link>
				</div>

				{/* Next Button (Bound to the right, only rendered if available) */}
				{page.next && <NavButton to={page.next} direction="next" />}
			</div>
		</div>
	)
}

export default PageContents
