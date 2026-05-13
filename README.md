# DeepSeek Eclipse Plugin

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-EPL_2.0-red.svg)](https://www.eclipse.org/legal/epl-2.0/)
[![Eclipse Plugin](https://img.shields.io/badge/Eclipse_Plugin-RCP-2C2255?logo=eclipse)](https://www.eclipse.org/)
[![Documentation](https://img.shields.io/badge/docs-Javadoc-blue.svg)](https://evandroluizvieira.github.io/deepseek-eclipse-plugin/index.html)
[![Version](https://img.shields.io/github/v/release/evandroluizvieira/deepseek-eclipse-plugin?sort=semver&cacheSeconds=0)](https://github.com/evandroluizvieira/deepseek-eclipse-plugin/releases)

## Overview

DeepSeek Eclipse Plugin brings AI assistance directly into Eclipse IDE, enabling developers to ask questions, analyze code, and receive guided suggestions from a DeepSeek language model.

The plugin integrates seamlessly with Eclipse RCP using SWT/JFace and OSGi technologies, providing a native IDE experience for AI-assisted development workflows.

## Resources

**[API Documentation](https://evandroluizvieira.github.io/deepseek-eclipse-plugin/index.html)** - Complete JavaDoc API reference

**[Download Plugin](https://github.com/evandroluizvieira/deepseek-eclipse-plugin/releases/latest)** - Latest Eclipse plugin release

## Features

- **AI Chat Integration** - DeepSeek assistant directly inside Eclipse
- **Code Analysis** - Analyze and explain source code
- **Conversation History** - Persistent interaction workflow
- **Request Cancellation** - Interrupt long-running API requests
- **Custom Preferences** - Configure API key and plugin settings
- **SWT/JFace Integration** - Native Eclipse UI experience
- **OSGi Plugin Architecture** - Eclipse RCP compatible
- **GitHub Pages JavaDoc** - Automatically published API documentation

## Installation

### Manual Installation

1. Download the latest plugin `.jar` from the Releases page:

   https://github.com/evandroluizvieira/deepseek-eclipse-plugin/releases/latest

2. Open your Eclipse installation directory.

3. Copy the plugin JAR into the `dropins/` folder.

Example:

   eclipse/dropins/deepseek-eclipse-plugin-*.jar

1. Restart Eclipse.

The plugin will be automatically detected and loaded by Eclipse.

## Requirements

- Eclipse IDE for RCP and RAP Developers (2025-09 or newer)
- Java 21+
- DeepSeek API key

## Usage

1. Open Eclipse
2. Open the DeepSeek Assistant view
3. Configure your DeepSeek API key
4. Enter prompts or source code snippets
5. Receive AI-powered responses directly inside Eclipse

## Troubleshooting

If the plugin does not appear after installation:
- Ensure Eclipse meets the requirements (2025-09+)
- Check the Eclipse error log: Window > Show View > Error Log
- Verify Java 21 is the default JRE

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests. See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines on commit conventions and development workflow.

## License
This project is licensed under the Eclipse Public License 2.0.

## Author
Evandro Luiz Vieira - [evandroluizvieira@hotmail.com](mailto:evandroluizvieira@hotmail.com)
