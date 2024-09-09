import React from 'react'
import { BrowserRouter as Router } from 'react-router-dom'
import WikiPage from './components/WikiPage'
import { ThemeProvider, useTheme } from './components/ThemeContext'

// Define the custom theme you want to use
const discordTheme = {
	primaryColor: '#5865F2', // Discord's blurple (primary brand color)
	secondaryColor: '#57F287', // Green used for online status or highlights
	backgroundColor: '#36393E', // Discord's dark background
	textColor: '#FFFFFF', // White for text
	sidebarBackgroundColor: '#313338', // Slightly darker sidebar background
	mutedTextColor: '#7289DA', // Muted grayish text (used for secondary text or labels)
	linkHoverColor: '#1289DA', // Lighter version of the primary color for hover effects
	headerBackgroundColor: '#282B30',
	headerTextColor: '#FFFFFF'
}

const App: React.FC = () => {
	return (
		<ThemeProvider theme={discordTheme}>
			<ThemedAppWrapper>
				<WikiPage />
			</ThemedAppWrapper>
		</ThemeProvider>
	)
}

// Wrapper component to apply the theme's background color
const ThemedAppWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => {
	const theme = useTheme() // Access the theme

	return (
		<Router>
			<div
				style={{
					backgroundColor: theme.backgroundColor,
					color: theme.textColor,
					minHeight: '100vh', // Ensures background covers the full height
					padding: '0',
					margin: '0',
					display: 'flex',
					flexDirection: 'column'
				}}
			>
				{children}
			</div>
		</Router>
	)
}

export default App

