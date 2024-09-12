import React, { useState, useEffect } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import PageContents from './PageContents'
import Sidebar from './Sidebar'
import { useTheme } from './ThemeContext'
import Header from './Header'
import Page from './Page'

const BASE_API_URL = 'https://raw.githubusercontent.com/Truly-Modular/Modular-Item-API/'

const WikiPage: React.FC = () => {
	const theme = useTheme()
	const [searchParams] = useSearchParams()
	const [data, setData] = useState<Page | null>(null)
	const [loading, setLoading] = useState(true)
	const [branch, setBranch] = useState<string>('release/1.21-mojmaps')
	const [page, setPage] = useState<string>('home')

	useEffect(() => {
		const branchParam = searchParams.get('branch') || ''
		const pageParam = searchParams.get('page') || 'home'
		if (branch !== branchParam || page !== pageParam) {
			setBranch(branchParam)
			setPage(pageParam)
		}
	}, [searchParams, branch, page])

	useEffect(() => {
		const fetchData = async () => {
			if (!branch) return
			setLoading(true)
			try {
				const response = await fetch(`${BASE_API_URL}${branch}/miapi-wiki-generated/output.json`)
				const result = await response.json()
				const pageData = new Page(result)
				setData(pageData)
			} catch (error) {
				setData(null)
				console.error('Error fetching data:', error)
			} finally {
				setLoading(false)
			}
		}
		fetchData()
	}, [branch])

	const findPageByPath = (rootPage: Page | null, pagePath: string): Page => {
		if (!rootPage || !pagePath) return new Page()
		const pathParts = pagePath
			.split('/')
			.map((part) => part.toLowerCase())
			.filter((part) => part !== 'home')
		let currentPage: Page | undefined = rootPage
		for (const part of pathParts) {
			if (!currentPage || !currentPage.sub_pages.has(part)) {
				return new Page()
			}
			currentPage = currentPage.sub_pages.get(part)
		}
		return currentPage || new Page()
	}

	if (!data && loading) {
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
				<Header rootPage={data || new Page()} />
				<div style={{ display: 'flex', flexGrow: 1 }}>
					<nav
						style={{
							width: '300px',
							backgroundColor: theme.sidebarBackgroundColor,
							padding: '1rem',
							height: '100%'
						}}
					>
						<Sidebar page={new Page({})} basePath={`?branch=${branch}&page=home`} hideRoot={true} />
					</nav>
					<main style={{ padding: '1rem', flexGrow: 1 }}>
						<div>Loading...</div>
					</main>
				</div>
			</div>
		)
	}

	if (!data) {
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
				<Header rootPage={data || new Page()} />
				<div style={{ display: 'flex', flexGrow: 1 }}>
					<nav
						style={{
							width: '300px',
							backgroundColor: theme.sidebarBackgroundColor,
							padding: '1rem',
							height: '100%'
						}}
					>
						<Sidebar page={new Page({})} basePath={`?branch=${branch}&page=home`} hideRoot={true} />
					</nav>
					<main style={{ padding: '1rem', flexGrow: 1 }}>
						<PageContents page={findPageByPath(data, page)} branch_name={branch} />
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
			{/* Fixed Header */}
			<div style={{ position: 'fixed', top: 0, left: 0, right: 0, zIndex: 1000 }}>
				<Header rootPage={data || new Page()} />
			</div>

			<div style={{ display: 'flex', flexGrow: 1, paddingTop: '60px' }}>
				{' '}
				{/* Adjust padding for fixed header */}
				{/* Fixed Sidebar */}
				<nav
					style={{
						width: 'clamp(50px, 15vw, 300px)',
						backgroundColor: theme.sidebarBackgroundColor,
						paddingTop: '1rem',
						position: 'fixed',
						top: '70px', // Below the fixed header
						bottom: 0,
						overflowY: 'auto' // Make the sidebar scrollable if it overflows
					}}
				>
					<Sidebar page={data} basePath={'home'} hideRoot={true} />
				</nav>
				{/* Main content that scrolls independently */}
				<main
					style={{
						marginLeft: 'clamp(50px, 15vw, 300px)',
						padding: '1rem',
						flexGrow: 1,
						overflowY: 'auto',
						height: 'calc(100vh - 95px)' // Full height minus the header
					}}
				>
					<PageContents page={findPageByPath(data, page)} branch_name={branch} />
				</main>
			</div>
		</div>
	)
}

export default WikiPage
