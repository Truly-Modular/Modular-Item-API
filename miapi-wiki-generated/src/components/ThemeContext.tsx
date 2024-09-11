import React, { createContext, useContext } from 'react'

// Define the shape of your theme
interface Theme {
	primaryColor: string
	secondaryColor: string
	backgroundColor: string
	lightBackgroundColor: string
	textColor: string
	sidebarBackgroundColor: string
	mutedTextColor: string
	linkHoverColor: string
	headerBackgroundColor: string
	headerTextColor: string
}

// Define your default theme
const defaultTheme: Theme = {
	primaryColor: '#5865F2', // Discord's blurple (primary brand color)
	secondaryColor: '#57F287', // Green used for online status or highlights
	backgroundColor: '#2C2F33', // Discord's dark background
	lightBackgroundColor: '#80848E', // Discord's dark background
	textColor: '#FFFFFF', // White for text
	sidebarBackgroundColor: '#23272A', // Slightly darker sidebar background
	mutedTextColor: '#99AAB5', // Muted grayish text (used for secondary text or labels)
	linkHoverColor: '#7289DA',
	headerBackgroundColor: '#23272A',
	headerTextColor: '#5865F2'
}

// Create a context for the theme
const ThemeContext = createContext<Theme>(defaultTheme)

// Hook to use the theme
export const useTheme = () => useContext(ThemeContext)

interface ThemeProviderProps {
	children: React.ReactNode
	theme?: Theme // Allow custom themes to be passed in
}

export const ThemeProvider: React.FC<ThemeProviderProps> = ({ children, theme = defaultTheme }) => {
	return <ThemeContext.Provider value={theme}>{children}</ThemeContext.Provider>
}
