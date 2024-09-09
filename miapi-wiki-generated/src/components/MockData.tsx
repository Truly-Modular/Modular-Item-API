import Page from './Page'

const MockData: Page = {
	header: 'Home',
	description: 'This is the home page of the wiki.',
	java: 'public class HomePage {}',
	data: new Map<string, string>([
		['author', 'Admin'],
		['version', '1.0.0']
	]),
	sub_pages: new Map<string, Page>([
		[
			'About',
			{
				header: 'About',
				description: 'About this wiki and its purpose.',
				java: 'public class AboutPage {}',
				data: new Map<string, string>([
					['author', 'Admin'],
					['last_updated', '2023-09-08']
				]),
				sub_pages: new Map<string, Page>([
					[
						'Team',
						{
							header: 'Team',
							description: 'Meet the team behind the wiki.',
							java: 'public class TeamPage {}',
							data: new Map<string, string>([
								['lead', 'John Doe'],
								['members', '4']
							]),
							sub_pages: new Map<string, Page>([
								[
									'Developers',
									{
										header: 'Developers',
										description: 'Details about the developers.',
										java: 'public class DevelopersPage {}',
										data: new Map<string, string>([
											['languages', 'Java, TypeScript'],
											['experience', '5 years']
										]),
										sub_pages: new Map<string, Page>()
									}
								]
							])
						}
					],
					[
						'History',
						{
							header: 'History',
							description: 'The history of the wiki project.',
							java: 'public class HistoryPage {}',
							data: new Map<string, string>([
								['year_started', '2020'],
								['initial_contributors', '2']
							]),
							sub_pages: new Map<string, Page>([
								[
									'Milestones',
									{
										header: 'Milestones',
										description: "Significant milestones in the project's development.",
										java: 'public class MilestonesPage {}',
										data: new Map<string, string>([
											['launch_date', '2021'],
											['milestone_count', '5']
										]),
										sub_pages: new Map<string, Page>()
									}
								]
							])
						}
					]
				])
			}
		],
		[
			'Documentation',
			{
				header: 'Documentation',
				description: 'Technical documentation and user guides.',
				java: 'public class DocumentationPage {}',
				data: new Map<string, string>([
					['maintainer', 'Tech Team'],
					['version', '1.2.0']
				]),
				sub_pages: new Map<string, Page>([
					[
						'API Reference',
						{
							header: 'API Reference',
							description: 'API reference guide for developers.',
							java: 'public class APIReferencePage {}',
							data: new Map<string, string>([
								['total_endpoints', '35'],
								['updated', '2024-08-01']
							]),
							sub_pages: new Map<string, Page>([
								[
									'Auth API',
									{
										header: 'Auth API',
										description: 'Authentication API details.',
										java: 'public class AuthAPIPage {}',
										data: new Map<string, string>([
											['methods', 'OAuth2, JWT'],
											['version', '2.0']
										]),
										sub_pages: new Map<string, Page>()
									}
								]
							])
						}
					],
					[
						'User Guide',
						{
							header: 'User Guide',
							description: 'Comprehensive user guide for the wiki.',
							java: 'public class UserGuidePage {}',
							data: new Map<string, string>([
								['sections', '8'],
								['examples_included', 'Yes']
							]),
							sub_pages: new Map<string, Page>([
								[
									'Installation',
									{
										header: 'Installation',
										description: 'Guide to installing and setting up the wiki.',
										java: 'public class InstallationPage {}',
										data: new Map<string, string>([
											['os_support', 'Windows, Linux, MacOS'],
											['requirements', 'JDK 11+']
										]),
										sub_pages: new Map<string, Page>()
									}
								]
							])
						}
					]
				])
			}
		]
	])
}

export default MockData
