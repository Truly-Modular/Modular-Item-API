import React from 'react'
import Page from './Page'

interface PageContentsProps {
	page: Page
}

const PageContents: React.FC<PageContentsProps> = ({ page }) => {
	return (
		<div>
			<h1>{page.header}</h1>
			<p>{page.description}</p>
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
