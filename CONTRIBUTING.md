# Contributing to DeepSeek Eclipse Plugin

First off, thank you for considering contributing to DeepSeek Eclipse Plugin.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Automated CI/CD Pipeline](#automated-cicd-pipeline)
- [Code Style Guidelines](#code-style-guidelines)
- [Pull Request Process](#pull-request-process)
- [Reporting Bugs](#reporting-bugs)
- [Feature Requests](#feature-requests)
- [License](#license)

## Code of Conduct

This project adheres to the Contributor Covenant Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to evandroluizvieira@hotmail.com.

## Getting Started

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   git clone https://github.com/YOUR_USERNAME/deepseek-eclipse-plugin.git
3. Add the original repository as upstream:
   git remote add upstream https://github.com/evandroluizvieira/deepseek-eclipse-plugin.git

### Build Requirements

- Java 21 or newer
- Maven 3.8 or newer
- Eclipse 2025-09 or newer
- Tycho 4.0.8 (managed by Maven)

### Setting Up Development Environment

1. Import into Eclipse IDE for RCP and RAP Developers: File > Import > Existing Maven Projects
2. Build to verify: mvn clean verify

## Development Workflow

### Branch Strategy

- main - Production branch. All releases are built from this branch.
- feature/* - New features and enhancements
- fix/* - Bug fixes
- docs/* - Documentation changes

Always create feature/fix branches from the latest main and submit Pull Requests for merging.

### Commit Conventions

This project uses Conventional Commits to enable automated semantic versioning.

### Commit Message Format

<type>: <subject>

### Version Impact

| Type | Version Impact |
|------|----------------|
| feat! or fix! | MAJOR (1.0.0 -> 2.0.0) |
| feat | MINOR (1.0.0 -> 1.1.0) |
| fix | PATCH (1.0.0 -> 1.0.1) |
| All other types (docs, chore, refactor, test, style) | No version |

### Valid Examples

git commit -m "feat: add conversation persistence"
git commit -m "fix: resolve API validation error"
git commit -m "feat!: remove deprecated API"

### Invalid Examples (Will NOT trigger version)

git commit -m "updated readme"
git commit -m "chore: update dependencies"

## Automated CI/CD Pipeline

### Pipeline Stages

1. Version Calculation
2. File Update (MANIFEST.MF and pom.xml)
3. Tag Creation (vX.Y.Z)
4. Build with Maven/Tycho
5. Javadoc Generation
6. GitHub Pages Deployment
7. Release Publication

### Important Notes

- Do not manually edit version numbers
- Pipeline does not trigger on its own commits
- JAR is attached to each release
- Documentation updates automatically

## Code Style Guidelines

### Java Standards

- camelCase for methods/variables
- PascalCase for classes
- 4 spaces indentation (no tabs)
- Maximum 120 characters per line

### Documentation

All public classes, methods, and fields must have Javadoc.

### UI Components

- Use SWT/JFace components
- Dispose resources properly
- Use Display.getDefault().asyncExec() for UI updates

## Pull Request Process

1. Ensure your branch is up to date with main: git rebase main
2. Run build: mvn clean verify
3. Push to your fork
4. Submit PR with clear description
5. PR requires review before merging
6. Squash and merge to main

## Reporting Bugs

Include:
- Eclipse version and OS
- Java version
- Plugin version
- Steps to reproduce
- Error logs

## Feature Requests

Provide:
- Clear description
- Use case and benefit

## License

By contributing, you agree your contributions will be licensed under the Eclipse Public License 2.0.