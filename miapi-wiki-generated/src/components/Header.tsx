import React, { useState, useEffect } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useTheme } from './ThemeContext'

const Header: React.FC = () => {
	const theme = useTheme()
	const location = useLocation()
	const navigate = useNavigate()
	const [selectedVersion, setSelectedVersion] = useState<string>('')

	// Dropdown options
	const versions = [
		{ label: 'Release 1.21', value: 'release/1.21-mojmaps' },
		{ label: 'Release 1.20', value: 'release/1.20' }
		// Add more versions here as needed
	]

	// Set the selected version based on the current URL when the component mounts
	useEffect(() => {
		const branch = new URLSearchParams(location.search).get('branch') || ''
		const matchedVersion = versions.find((version) => branch.startsWith(version.value))
		if (matchedVersion) {
			setSelectedVersion(matchedVersion.value)
		} else {
			setSelectedVersion('release/1.21-mojmaps') // Default version if no match
		}
	}, [location.search, versions])

	// Function to handle navigation based on selected version
	const handleVersionChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
		const versionPath = e.target.value
		setSelectedVersion(versionPath)
		navigate(`?branch=${versionPath}&page=home`)
	}

	// Function to handle the navigation for "Wiki" link
	const handleWikiClick = (e: React.MouseEvent<HTMLAnchorElement, MouseEvent>) => {
		e.preventDefault()
		const params = new URLSearchParams(location.search)
		params.set('page', 'home')
		navigate(`?${params.toString()}`)
	}

	return (
		<header
			style={{
				backgroundColor: theme.headerBackgroundColor,
				color: theme.headerTextColor,
				padding: '1rem',
				borderBottom: `2px solid ${theme.mutedTextColor}`,
				display: 'flex',
				justifyContent: 'space-between',
				alignItems: 'center'
			}}
		>
			<h1 style={{ margin: 0 }}>
				<a
					href="/"
					onClick={handleWikiClick} // Attach the click handler
					style={{ textDecoration: 'none', color: theme.headerTextColor }}
				>
					Truly Modular Wiki
				</a>
			</h1>

			{/* Dropdown for version selection */}
			<select
				value={selectedVersion}
				onChange={handleVersionChange}
				style={{
					backgroundColor: theme.backgroundColor,
					color: theme.textColor,
					border: `1px solid ${theme.mutedTextColor}`,
					padding: '0.5rem',
					borderRadius: '4px',
					cursor: 'pointer'
				}}
			>
				{versions.map((version) => (
					<option key={version.value} value={version.value}>
						{version.label}
					</option>
				))}
			</select>
		</header>
	)
}

export default Header
