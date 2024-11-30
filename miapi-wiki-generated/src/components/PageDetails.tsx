import React from 'react'
import { Link } from 'react-router-dom'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm' // Adds `id` attributes to headers
import 'highlight.js/styles/github.css' // GitHub theme for syntax highlighting
import Page from './Page'
import rehypePrismPlus from 'rehype-prism-plus'
import 'prismjs/themes/prism-tomorrow.css' // or any Prism theme you like
//import './github-style.css' // GitHub markdown styles

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
				paddingRight: '30px',
				backgroundColor: '#1c1c1c', // Set the background color to your desired dark color
				color: '#f0f0f0', // Change text color to a light one for better contrast
				paddingTop: '20px' // Space for header
			}}
		>
			{/* Conditionally render the header as a link if isSubPage is true */}
			{isSubPage ? (
				<h1 style={{ textAlign: 'left' }}>
					<Link
						to={page.buildLinkPath()}
						style={{
							textDecoration: 'none',
							color: 'inherit'
						}}
					>
						{page.header}
					</Link>
				</h1>
			) : (
				<h1 style={{ textAlign: 'center' }}>{page.header}</h1>
			)}

			{/* Description with plugins */}
			<div
				style={{
					maxWidth: '800px',
					margin: isSubPage ? 'left' : 'auto',
					minHeight: isSubPage ? '50px' : '200px',
					paddingBottom: '5px',
					paddingLeft: '20px'
				}}
			>
				<div className="markdown-body">
					<ReactMarkdown
						children={desc}
						remarkPlugins={[remarkGfm]} // Enable GitHub-flavored markdown
						rehypePlugins={[rehypePrismPlus]} // Enable syntax highlighting
					/>
				</div>

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
