import React, { useState, useEffect } from 'react'
import { BrowserRouter as Router, Route, Routes, useLocation, Navigate, useSearchParams } from 'react-router-dom'
import PageContents from './PageContents'
import Sidebar from './Sidebar'
import { useTheme } from './ThemeContext'
import Header from './Header'
import Page from './Page' // Import your Page class here

// Updated API base endpoint with the prefix
const BASE_API_URL = 'https://raw.githubusercontent.com/Truly-Modular/Modular-Item-API/'

const WikiPage: React.FC = () => {
	const theme = useTheme()
	const [searchParams] = useSearchParams() // Use searchParams to read query parameters
	const [data, setData] = useState<Page | null>(null) // Data state to hold the fetched Page object
	const [loading, setLoading] = useState(true) // State to manage loading
	const [branch, setBranch] = useState<string>('release/1.21-mojmaps') // State to manage the current branch
	const [page, setPage] = useState<string>('home') // State to manage the current page

	// Extract branch and page info from URL
	useEffect(() => {
		const branchParam = searchParams.get('branch') || ''
		const pageParam = searchParams.get('page') || 'home'
		if (branch !== branchParam || page !== pageParam) {
			setBranch(branchParam)
			setPage(pageParam)
		}
	}, [searchParams, branch, page])

	// Fetch data whenever the branch changes
	useEffect(() => {
		const fetchData = async () => {
			console.log('Update start')
			if (!branch) return
			setLoading(true) // Set loading state before the fetch starts
			try {
				const response = await fetch(`${BASE_API_URL}${branch}/miapi-wiki-generated/output.json`)
				const result = await response.json()
				const pageData = new Page(result)
				setData(pageData)
			} catch (error) {
				setData(null)
				console.log(`${BASE_API_URL}${branch}/miapi-wiki-generated/output.json`)
				console.log('failed branch', error)
				console.error('Error fetching data:', error)
			} finally {
				setLoading(false)
			}
		}

		fetchData()
	}, [branch]) // Re-run effect when `branch` changes

	// Recursive function to generate routes
	// Recursive function to find a page by its path (decoded from `page` param)
	const findPageByPath = (rootPage: Page | null, pagePath: string): Page => {
		if (!rootPage || !pagePath) return new Page()

		// Split the `pagePath` into parts and remove any "home" references
		const pathParts = pagePath
			.split('/')
			.map((part) => part.toLowerCase())
			.filter((part) => part !== 'home')

		let currentPage: Page | undefined = rootPage

		// Traverse the sub_pages using the path parts
		for (const part of pathParts) {
			if (!currentPage || !currentPage.sub_pages.has(part)) {
				return new Page() // If any part of the path doesn't exist, return a default Page
			}
			currentPage = currentPage.sub_pages.get(part)
		}

		return currentPage || new Page() // Return the found page or a new Page as fallback
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
						<Sidebar page={new Page(new Object())} basePath={`?branch=${branch}&page=home`} indentSize={20} />
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
						<Sidebar page={new Page(new Object())} basePath={`?branch=${branch}&page=home`} indentSize={20} />
					</nav>

					<main style={{ padding: '1rem', flexGrow: 1 }}>{<PageContents page={findPageByPath(data, page)} />}</main>
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
					<Sidebar page={data} basePath={'home'} indentSize={20} hideRoot={false} />
				</nav>

				<main style={{ padding: '1rem', flexGrow: 1 }}>
					{findPageByPath(data, page) ? <PageContents page={findPageByPath(data, page)} /> : <PageContents page={new Page()} />}
				</main>
			</div>
		</div>
	)
}

export default WikiPage
