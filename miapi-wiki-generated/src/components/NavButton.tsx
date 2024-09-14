import React from 'react'
import { Link } from 'react-router-dom'
import { useTheme } from './ThemeContext'
import Page from './Page'

interface NavButtonProps {
	to: Page
	direction: 'prev' | 'next' // Indicates whether it's a Previous or Next button
}

const NavButton: React.FC<NavButtonProps> = ({ to, direction }) => {
	const isPrev = direction === 'prev'
	const theme = useTheme()

	return (
		<Link
			to={to.buildLinkPath()}
			style={{
				position: 'absolute',
				[isPrev ? 'left' : 'right']: '0', // Position on left if prev, right if next
				padding: '10px 20px',
				//backgroundColor: theme.primaryColor,
				color: theme.primaryColor,
				borderRadius: '5px',
				textDecoration: 'none'
			}}
		>
			{isPrev ? `< ${to.header} Page` : `${to.header} Page >`}
		</Link>
	)
}

export default NavButton
