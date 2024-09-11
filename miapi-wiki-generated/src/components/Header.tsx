import React, { useState, useEffect } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useTheme } from './ThemeContext'
import Page from './Page'
import SearchComponent from './SearchComponent'

interface HeaderProps {
	rootPage: Page
}

const Header: React.FC<HeaderProps> = ({ rootPage }) => {
	const theme = useTheme()
	const location = useLocation()
	const navigate = useNavigate()
	const [selectedVersion, setSelectedVersion] = useState<string>('')

	// Dropdown options
	const versions = [
		{ label: 'Release 1.21', value: 'release/1.21-mojmaps' },
		{ label: 'Release 1.20', value: 'release/1.20.1' }
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
				display: 'flex',
				height: '40px',
				justifyContent: 'space-between',
				alignItems: 'center'
			}}
		>
			{/* Left side: Version dropdown */}
			<div style={{ display: 'flex', alignItems: 'center' }}>
				<h1 style={{ margin: 0 }}>
					<a
						href="/"
						onClick={handleWikiClick} // Attach the click handler
						style={{ textDecoration: 'none', color: theme.headerTextColor }}
					>
						Truly Modular Wiki
					</a>
				</h1>
				<select
					value={selectedVersion}
					onChange={handleVersionChange}
					style={{
						backgroundColor: theme.backgroundColor,
						color: theme.textColor,
						border: `1px solid ${theme.mutedTextColor}`,
						padding: '0.5rem',
						borderRadius: '4px',
						cursor: 'pointer',
						marginRight: '1rem',
						marginLeft: '2rem'
					}}
				>
					{versions.map((version) => (
						<option key={version.value} value={version.value}>
							{version.label}
						</option>
					))}
				</select>
			</div>

			{/* Right side: Search bar and links */}
			<div style={{ display: 'flex', alignItems: 'center' }}>
				<SearchComponent rootPage={rootPage}></SearchComponent>

				<a href="https://discord.gg/TebNhbCAUP" style={{ margin: '0 0.5rem' }}>
					<img
						src="https://simpleicons.org/icons/discord.svg"
						alt="Discord"
						style={{
							width: '24px',
							height: '24px',
							filter: 'invert(36%) sepia(84%) saturate(2682%) hue-rotate(180deg) brightness(89%) contrast(90%)',
							transition: 'filter 0.3s ease'
						}}
						className="icon"
					/>
				</a>
				<a href="https://github.com/Truly-Modular/Modular-Item-API" style={{ margin: '0 0.5rem' }}>
					<img
						src="https://simpleicons.org/icons/github.svg"
						alt="GitHub"
						style={{
							width: '24px',
							height: '24px',
							filter: 'invert(0%) sepia(0%) saturate(0%) hue-rotate(0deg) brightness(0%) contrast(0%)', // Gray color
							transition: 'filter 0.3s ease'
						}}
						className="icon"
					/>
				</a>
				<a href="https://modrinth.com/organization/truly-modular" style={{ margin: '0 0.5rem' }}>
					<img
						src="https://simpleicons.org/icons/modrinth.svg"
						alt="Modrinth"
						style={{
							width: '24px',
							height: '24px',
							filter: 'invert(55%) sepia(55%) saturate(2680%) hue-rotate(90deg) brightness(89%) contrast(90%)',
							transition: 'filter 0.3s ease'
						}}
						className="icon"
					/>
				</a>
				<a href="https://www.curseforge.com/minecraft/mc-mods/modular-item-api" style={{ margin: '0 0.5rem' }}>
					<img
						src="https://simpleicons.org/icons/curseforge.svg"
						alt="CurseForge"
						style={{
							width: '24px',
							height: '24px',
							filter: 'invert(23%) sepia(56%) saturate(5020%) hue-rotate(6deg) brightness(94%) contrast(94%)',
							transition: 'filter 0.3s ease'
						}}
						className="icon"
					/>
				</a>
			</div>
		</header>
	)
}

export default Header
