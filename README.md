# DeepSeek Eclipse Plugin

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk)
![Eclipse](https://img.shields.io/badge/Eclipse_Plugin-2C2255?logo=eclipse)
![OSGi](https://img.shields.io/badge/OSGi-Equinox-6DB33F)
[![License](https://img.shields.io/badge/License-EPL_2.0-red.svg)](https://www.eclipse.org/legal/epl-2.0/)

## Overview
DeepSeek Eclipse Plugin integrates AI assistance directly into Eclipse IDE, providing seamless access to DeepSeek's powerful language model for code analysis, explanations, and development guidance.

The plugin is open source and licensed under the Eclipse Public License 2.0.

## Features
- **AI-Powered Chat**: Direct DeepSeek integration within Eclipse
- **Code Analysis**: Get explanations and suggestions for your code
- **Real-time Assistance**: Instant AI support while coding
- **Customizable**: Configurable API settings and preferences

## Installation

### From Source
```bash
git clone https://github.com/evandroluizvieira/deepseek-eclipse-plugin.git
```

### Eclipse Setup
 - Import as existing Eclipse project
 - Ensure Eclipse for RCP Developers is used
 - Run as Eclipse Application to test

### Development Prerequisites
 - Eclipse IDE for RCP and RAP Developers
 - Java 21+
 - DeepSeek API key

### Project Structure
```
deepseek-eclipse-plugin/
├── src/
│   └── com/deepseek/plugin/
│       ├── views/DeepSeekView.java
│       └── Activator.java
├── META-INF/MANIFEST.MF
├── plugin.xml
└── build.properties
```

### Build & Test
- Right-click project → Run As → Eclipse Application
- In new Eclipse instance: Window → Show View → Other → DeepSeek Assistant

## Usage
**Once installed:**
 - Open DeepSeek Assistant view
 - Enter your questions or code snippets
 - Get AI-powered responses directly in Eclipse

### Technical Stack
 - **Platform:** Eclipse RCP/Plugin
 - **Runtime:** OSGi Equinox
 - **UI:** SWT/JFace
 - **Java:** 21+
 - **Build:** Eclipse PDE

### Contributing
Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

### License
This project is licensed under the Eclipse Public License 2.0 - see the LICENSE file for details.
