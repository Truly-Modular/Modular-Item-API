import React, { useState, useEffect } from 'react'
import { BrowserRouter as Router, Route, Routes, useLocation, Navigate } from 'react-router-dom'
import PageContents from './PageContents'
import Sidebar from './Sidebar'
import { useTheme } from './ThemeContext'
import Header from './Header'
import Page from './Page' // Import your Page class here

// Example API base endpoint
const BASE_API_URL = 'https://raw.githubusercontent.com/Truly-Modular/Modular-Item-API/'

const WikiPage: React.FC = () => {
	const theme = useTheme()
	const location = useLocation() // Use location to dynamically parse the URL
	const [data, setData] = useState<Page | null>(null) // Data state to hold the fetched Page object
	const [loading, setLoading] = useState(true) // State to manage loading
	const [currentBranch, setCurrentBranch] = useState<string>('') // State to manage the current branch
	const [lastBranch, setLastBranch] = useState<string>('') // State to manage the current branch

	// Extract branch info from URL
	useEffect(() => {
		const path = location.pathname
		const branch = path.split('/home')[0]
		if (currentBranch !== branch) {
			console.log('updating content to ' + branch)
			setCurrentBranch(branch)
		}
	}, [location])

	// Fetch data whenever the branch changes
	useEffect(() => {
		const fetchData = async () => {
			console.log('update start')
			if (!currentBranch) return
			setLoading(true) // Set loading state before the fetch starts
			try {
				const response = await fetch(`${BASE_API_URL}${currentBranch}/miapi-wiki-generated/output.json`)
				const result = await response.json()
				const pageData = new Page(result) // Parse the result into the Page class
				setData(pageData) // Set the parsed Page object into state
			} catch (error) {
				setData(new Page(new Object()))
				console.error('Error fetching data:', error)
			} finally {
				setLoading(false)
			}
		}

		fetchData()
	}, [currentBranch]) // Re-run effect when `currentBranch` changes

	// Recursive function to generate routes
	const generateRoutes = (page: Page, basePath: string): JSX.Element[] => {
		const routes: JSX.Element[] = [<Route key={basePath} path={basePath} element={<PageContents page={page} />} />]

		page.sub_pages.forEach((subPage, subPageKey) => {
			routes.push(...generateRoutes(subPage, `${basePath}/${subPageKey.toLowerCase()}`))
		})

		return routes
	}

	if (!data && loading) {
		// Render while loading, with a placeholder Page object for the sidebar
		return (
			<div
				style={{
					display: 'flex',
					flexDirection: 'column',
					height: '100vh',
					backgroundColor: theme.backgroundColor,
					color: theme.textColor
				}}
			>
				<Header />
				<div style={{ display: 'flex', flexGrow: 1 }}>
					<nav
						style={{
							width: '250px',
							backgroundColor: theme.sidebarBackgroundColor,
							padding: '1rem',
							borderRight: `2px solid ${theme.mutedTextColor}`,
							height: '100%'
						}}
					>
						<Sidebar page={new Page(new Object())} basePath={`/${currentBranch}/home`} indentSize={20} />
					</nav>

					<main style={{ padding: '1rem', flexGrow: 1 }}>
						<div>Loading...</div> {/* You can replace this with a spinner if desired */}
					</main>
				</div>
			</div>
		)
	}

	if (!data) {
		// Handle when data couldn't be loaded
		return (
			<div
				style={{
					display: 'flex',
					flexDirection: 'column',
					height: '100vh',
					backgroundColor: theme.backgroundColor,
					color: theme.textColor
				}}
			>
				<Header />
				<div style={{ display: 'flex', flexGrow: 1 }}>
					<nav
						style={{
							width: '250px',
							backgroundColor: theme.sidebarBackgroundColor,
							padding: '1rem',
							borderRight: `2px solid ${theme.mutedTextColor}`,
							height: '100%'
						}}
					>
						<Sidebar page={new Page(new Object())} basePath={`/${currentBranch}/home`} indentSize={20} />
					</nav>

					<main style={{ padding: '1rem', flexGrow: 1 }}>
						<div>Branch could not be loaded. This either means the branch doesn't exist or its data is broken</div>
					</main>
				</div>
			</div>
		)
	}

	return (
		<div
			style={{
				display: 'flex',
				flexDirection: 'column',
				height: '100vh',
				backgroundColor: theme.backgroundColor,
				color: theme.textColor
			}}
		>
			<Header />
			<div style={{ display: 'flex', flexGrow: 1 }}>
				<nav
					style={{
						width: '250px',
						backgroundColor: theme.sidebarBackgroundColor,
						padding: '1rem',
						borderRight: `2px solid ${theme.mutedTextColor}`,
						height: '100%'
					}}
				>
					<Sidebar page={data} basePath={`/${currentBranch}/home`} indentSize={20} hideRoot={true} />
				</nav>

				<main style={{ padding: '1rem', flexGrow: 1 }}>
					<Routes>
						{generateRoutes(data, `/${currentBranch}/home`)}
						{/* Default to home page */}
						<Route path="/" element={<Navigate to={`/${currentBranch}/home`} />} />
					</Routes>
				</main>
			</div>
		</div>
	)
}

export default WikiPage
