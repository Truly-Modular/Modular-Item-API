import React from 'react'
import Page from './Page'
import ReactMarkdown from 'react-markdown'

interface PageContentsProps {
	page: Page
}

const PageContents: React.FC<PageContentsProps> = ({ page }) => {
	console.log(page)
	console.log('page contents')
	return (
		<div>
			<h1>{page.header}</h1>
			<ReactMarkdown>{page.description}</ReactMarkdown>

			<pre>
				<code>{page.java}</code>
			</pre>
			<ul>
				{Array.from(page.data.entries()).map(([key, value]) => (
					<li key={key}>
						<strong>{key}: </strong>
						{value}
					</li>
				))}
			</ul>
		</div>
	)
}

export default PageContents