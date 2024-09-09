import React from 'react'
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom'
import PageContents from './PageContents'
import Sidebar from './Sidebar'
import Page from './Page'
import MockData from './MockData'
import { useTheme } from './ThemeContext'
import Header from './Header'

const WikiPage: React.FC = () => {
	const theme = useTheme()

	// Recursive function to generate routes
	const generateRoutes = (page: Page, basePath: string): JSX.Element[] => {
		const routes: JSX.Element[] = [<Route key={basePath} path={basePath} element={<PageContents page={page} />} />]

		page.sub_pages.forEach((subPage, subPageKey) => {
			routes.push(...generateRoutes(subPage, `${basePath}/${subPageKey.toLowerCase()}`))
		})

		return routes
	}

	return (
		<Router>
			<div style={{ display: 'flex', flexDirection: 'column', height: '100vh', backgroundColor: theme.backgroundColor, color: theme.textColor }}>
				{/* Header */}
				<Header />

				<div style={{ display: 'flex', flexGrow: 1 }}>
					{/* Sidebar */}
					<nav
						style={{
							width: '250px',
							backgroundColor: theme.sidebarBackgroundColor,
							padding: '1rem',
							borderRight: `2px solid ${theme.mutedTextColor}`,
							height: '100%'
						}}
					>
						<Sidebar page={MockData} basePath="/home" indentSize={20} />
					</nav>

					{/* Main Content */}
					<main style={{ padding: '1rem', flexGrow: 1 }}>
						<Routes>{generateRoutes(MockData, '/home')}</Routes>
					</main>
				</div>
			</div>
		</Router>
	)
}

export default WikiPage
