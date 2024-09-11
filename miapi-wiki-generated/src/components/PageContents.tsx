import React from 'react'
import Page from './Page'
import { Link } from 'react-router-dom'
import PageDetails from './PageDetails'
import NavButton from './NavButton'
import RelatedPages from './RelatedPages'
import { useTheme } from './ThemeContext'

interface PageContentsProps {
	page: Page
	branch_name: string
}

const PageContents: React.FC<PageContentsProps> = ({ page, branch_name }) => {
	const theme = useTheme()

	return (
		<div>
			<div style={{ position: 'relative', minHeight: '300px', padding: '20px', display: 'flex' }}>
				<div
					style={{
						flex: 1
					}}
				>
					{/* Main Content */}
					<PageDetails page={page} isSubPage={false} />

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
								{`< Category ${page.parent.header}`}
							</Link>
						</div>
					)}

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
								to={`https://github.com/Truly-Modular/Modular-Item-API/tree/${branch_name}/${page.java}`.replace('tree//', 'tree/')}
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
					{/* If the current page is a category, render sub-pages */}
					{page.isCategory && (
						<div style={{ marginTop: '40px', marginLeft: '50px' }}>
							<h2>Sub Pages</h2>
							{Array.from(page.sub_pages.values()).map((subPage) => (
								<div key={subPage.header} style={{ marginBottom: '20px' }}>
									<PageDetails page={subPage} isSubPage={true} />
								</div>
							))}
						</div>
					)}
				</div>
				<div
					id="PageContent"
					style={{
						boxSizing: 'border-box', // Ensure padding and border are included in the width
						alignSelf: 'flex-start', // Ensures the element stays aligned to the top of its container
						flexBasis: '25%', // 1/4 of the available space
						maxWidth: '400px',
						minWidth: '80px'
					}}
				>
					<RelatedPages page={page} />
				</div>
			</div>
		</div>
	)
}

export default PageContents
