import React from 'react'
import { Link } from 'react-router-dom' // Import Link for routing
import ReactMarkdown from 'react-markdown'
import Page from './Page'

interface PageDetailsProps {
	page: Page
	isSubPage?: boolean
}

const PageDetails: React.FC<PageDetailsProps> = ({ page, isSubPage = false }) => {
	const desc: string = page.getDescription()

	return (
		<div
			style={{
				flex: 1,
				paddingRight: '30px' // Padding to account for the space taken by the sidebar
			}}
		>
			{/* Conditionally render the header as a link if isSubPage is true */}
			{isSubPage ? (
				<h1 style={{ textAlign: 'left' }}>
					<Link
						to={page.buildLinkPath()} // Link to the page's path
						style={{
							textDecoration: 'none',
							color: 'inherit' // Maintain the theme's color for the header
						}}
					>
						{page.header}
					</Link>
				</h1>
			) : (
				<h1 style={{ textAlign: 'center' }}>{page.header}</h1>
			)}

			{/* Description with minimum space */}
			<div
				style={{
					maxWidth: '800px',
					margin: isSubPage ? 'left' : 'auto',
					minHeight: isSubPage ? '50px' : '200px', // Ensure a minimum height for the content area
					paddingBottom: '5px',
					paddingLeft: '20px'
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
		</div>
	)
}

export default PageDetails
