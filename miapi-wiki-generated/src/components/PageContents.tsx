import React from 'react'
import Page from './Page'

interface PageContentsProps {
	page: Page
}

const PageContents: React.FC<PageContentsProps> = ({ page }) => {
	console.log(page)
	console.log('page contents')
	return (
		<div>
			<h1>{page.header}</h1>
			<p style={{ lineHeight: '1.2' }}>
				{page.description.split('\n').map((line, index) => (
					<span key={index}>
						{line}
						<br />
					</span>
				))}
			</p>

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
