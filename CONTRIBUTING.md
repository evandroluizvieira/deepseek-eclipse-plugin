/*
# Contributing to DeepSeek Eclipse Plugin

First off, thank you for considering contributing to DeepSeek Eclipse Plugin.

## Table of Contents

- [Contributing to DeepSeek Eclipse Plugin](#contributing-to-deepseek-eclipse-plugin)
  - [Table of Contents](#table-of-contents)
  - [Code of Conduct](#code-of-conduct)
  - [Getting Started](#getting-started)
    - [Fork and Clone](#fork-and-clone)
    - [Build Requirements](#build-requirements)
    - [Setting Up Development Environment](#setting-up-development-environment)
  - [Development Workflow](#development-workflow)
    - [Branch Strategy](#branch-strategy)
    - [Commit Conventions](#commit-conventions)
    - [Commit Message Format](#commit-message-format)
    - [Valid Examples](#valid-examples)
    - [Invalid Examples (Will NOT trigger version)](#invalid-examples-will-not-trigger-version)
  - [Automated CI/CD Pipeline](#automated-cicd-pipeline)
    - [Versioning Rules](#versioning-rules)
    - [Pipeline Stages](#pipeline-stages)
    - [Important Notes](#important-notes)
  - [Code Style Guidelines](#code-style-guidelines)
    - [Java Standards](#java-standards)
    - [Documentation](#documentation)
    - [UI Components](#ui-components)
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

This project uses Conventional Commits to enable automated semantic versioning. The CI/CD pipeline analyzes commit messages since the last tag and determines the appropriate version increment.

### Commit Message Format

<type>: <subject>

Valid types and their version impact:

| Type | Version Impact |
|------|----------------|
| feat! or fix! | MAJOR (1.0.0 -> 2.0.0) |
| feat | MINOR (1.0.0 -> 1.1.0) |
| fix | PATCH (1.0.0 -> 1.0.1) |
| All other types (docs, chore, refactor, test, style, etc) | No new version |

### Valid Examples

git commit -m "feat: add conversation persistence"
git commit -m "fix: resolve API validation error"
git commit -m "feat!: remove deprecated API"

### Invalid Examples (Will NOT trigger version)

git commit -m "updated readme"
git commit -m "chore: update dependencies"
git commit -m "docs: improve installation guide"

## Automated CI/CD Pipeline

### Versioning Rules

The pipeline determines version bumps based solely on commit types in the message title. When a commit with feat: or fix: is pushed to main, the pipeline:

1. Finds the last Git tag (vX.Y.Z)
2. Analyzes all commits since that tag
3. Calculates the next version based on the highest impact commit:

   feat! or fix! -> MAJOR (increment X)
   feat:         -> MINOR (increment Y)
   fix:          -> PATCH (increment Z)

4. Commits without feat: or fix: do not trigger a new release

### Pipeline Stages

When a qualifying commit is pushed to main, the following stages execute sequentially:

1. Version Calculation - Scans commits since last tag to determine version bump
2. File Update - Updates version in MANIFEST.MF and pom.xml
3. Tag Creation - Creates and pushes Git tag (vX.Y.Z)
4. Build - Compiles using Maven with Tycho
5. Javadoc Generation - Generates API documentation
6. GitHub Pages Deployment - Publishes Javadoc to GitHub Pages
7. Release Publication - Creates GitHub Release with changelog and JAR artifact

### Important Notes

- Do not manually edit version numbers in pom.xml or MANIFEST.MF
- Pipeline does NOT trigger on its own commits (chore: bump version to X.Y.Z-SNAPSHOT)
- The JAR artifact is named com.deepseek.plugin-X.Y.Z.jar and attached to each release
- GitHub Pages documentation updates automatically with each release
- Badges in the README reflect the latest released version (may take a few minutes due to caching)

## Code Style Guidelines

### Java Standards

- camelCase for methods and variables
- PascalCase for classes and interfaces
- UPPER_SNAKE_CASE for constants
- 4 spaces indentation (no tabs)
- Maximum 120 characters per line

### Documentation

All public classes, methods, and fields must have Javadoc.

Example:

/**
 * Sends a prompt to the DeepSeek API and returns the response.
 *
 * @param prompt The user input to send to the AI model
 * @param apiKey The DeepSeek API authentication key
 * @return The AI-generated response as a string
 * @throws IOException If network communication fails
 */
public String sendPrompt(String prompt, String apiKey) throws IOException {
    // Implementation
}

### UI Components

- Use SWT/JFace components for native Eclipse look and feel
- Follow Eclipse UI Guidelines for consistency
- Dispose resources properly to avoid memory leaks
- Run UI updates in the Display thread using Display.getDefault().asyncExec()

## Pull Request Process

1. Ensure your branch is up to date with main:
   git checkout main
   git pull upstream main
   git checkout your-feature-branch
   git rebase main

2. Run the full build to verify everything works:
   mvn clean verify

3. Push your changes to your fork:
   git push origin your-feature-branch

4. Submit a Pull Request on GitHub with a clear description:
   - What problem does this PR solve?
   - What changes were made?
   - How was it tested?
   - Screenshots for UI changes (if applicable)

5. All PRs must be reviewed and approved by at least one maintainer before merging

6. After approval, squash and merge to main (the pipeline will handle versioning)

## Reporting Bugs

Before submitting a bug report:
1. Check if the issue already exists in the Issues section
2. Ensure you are using the latest version of the plugin

When submitting a bug report, include:
- Eclipse version and operating system
- Java version (java -version)
- Plugin version
- Steps to reproduce
- Expected vs actual behavior
- Error logs (Window > Show View > Error Log)

## Feature Requests

Feature requests are welcome. Please provide:
- Clear description of the feature
- Use case and benefit
- Possible implementation approach (optional)

## License

By contributing to DeepSeek Eclipse Plugin, you agree that your contributions will be licensed under the Eclipse Public License 2.0.