
# CI/CD Tooling Project

This repository contains two independent CI/CD tooling modules implemented as a Gradle multi-module project using Kotlin DSL.

## Project Structure

The repository is split into two modules:

-   **`filter-cli`**: a standalone command-line tool for path-based CI/CD filtering

-   **`junit-xml-filename-plugin`**: a Gradle plugin that enriches JUnit XML reports with source file metadata


This separation keeps the two assignment parts independent and makes each module easier to reason about, test, and evolve.

----------

## Part 1: Filter CLI Tool (`filter-cli`)

`filter-cli` reads a YAML configuration of named filters, evaluates those filters against a set of changed file paths, and writes the result as a `.env`-compatible output file.

This enables CI/CD systems to trigger only the relevant jobs or pipelines instead of running everything on every change.

### Example usage

```bash
./gradlew :filter-cli:run --args="--config filters.yaml --output result.env --changed src/main/kotlin/App.kt"
```

### Design choices

-   **JDK `PathMatcher` for glob matching**  
    Instead of maintaining a custom glob parser, the tool delegates path matching to the JDK `PathMatcher` using `glob:` syntax. This avoids unnecessary complexity and relies on a well-tested standard implementation.

-   **Compiled filter model**  
    YAML filters are transformed into a runtime `CompiledFilter` model where include and exclude rules are separated during loading. This simplifies evaluation logic and avoids repeated string inspection during matching.

-   **Fail-fast configuration validation**  
    Configuration loading validates both YAML structure and semantic correctness. Invalid inputs such as missing files, empty filters, blank patterns, or invalid exclusion markers fail early with explicit error messages.

-   **Clikt-based CLI**  
    The command-line interface is implemented with Clikt, providing built-in help output, argument validation, and a cleaner implementation compared to manual parsing.


----------

## Part 2: JUnit XML Filename Plugin (`junit-xml-filename-plugin`)

`junit-xml-filename-plugin` is a Gradle plugin that post-processes JUnit XML reports produced by Gradle `Test` tasks.

For each `<testcase>` element, it attempts to resolve the `classname` attribute back to a repository-relative source file path and adds that path as a `filename` attribute in the transformed XML output.

### Bonus feature

If a test case fails, the plugin parses the stacktrace and extracts the line number where the failure occurred. This is added as a `line` attribute on the `<testcase>` element.

### Example usage

Apply the plugin in `build.gradle.kts`:
```kotlin
plugins {  
	id("io.github.oguzhandongul.junit-xml-filename")  
}  
  
junitXmlFilename {  
	outputDir.set(layout.buildDirectory.dir("enriched-test-results"))  
}
```

### Design choices

-   **Separate transformed output**  
    The plugin writes enriched XML reports to a separate output directory instead of modifying Gradle’s original JUnit XML files. This keeps the build deterministic and avoids interfering with Gradle’s internal outputs.

-   **Per-`Test` task companion processing**  
    For each Gradle `Test` task, the plugin creates a follow-up enhancement task that reads generated XML reports and writes transformed copies.

-   **JDK DOM APIs for XML processing**  
    XML transformation is implemented using the JDK DOM APIs, which are sufficient for structure-aware transformations without introducing external dependencies.

-   **Heuristic source file resolution**  
    Classname-to-file mapping is implemented using a pragmatic resolver that supports common Kotlin and Java test layouts without requiring language-level parsing.


----------

## Building and testing

The project includes both unit tests and integration tests.

-   Unit tests cover configuration parsing, matching behavior, evaluation logic, XML transformation, and source file resolution.

-   Gradle TestKit integration tests verify plugin behavior against isolated Gradle builds.


Run all tests:
```bash
./gradlew test
```
Build the project:
```bash
./gradlew build
```

---

### Note

AI-assisted tools were used selectively to improve documentation and JavaDoc clarity.  
All architectural decisions, implementation, and design trade-offs were defined and reviewed manually.