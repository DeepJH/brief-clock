# Repository Guidelines

## Project Structure & Module Organization

Brief Clock is a modern Android application built with Kotlin, Jetpack Compose, and Material 3. The codebase is organized as follows:

- `app/src/main/java/com/briefclock/app/`: Core application source code.
  - `ui/`: Compose UI screens (`screens/`), reusable widgets (`components/`), and themes (`theme/`).
  - `alarm/`: Alarm alert activities, receiver, and scheduling services.
  - `audio/`: Sound effect synthesizers and recording utilities.
  - `model/` & `data/`: Domain data structures and SQLite persistence.
- `app/src/main/res/`: Android vector drawables, mipmaps, and localized string resources (`values/`, `values-zh/`).
- `app/src/test/java/com/briefclock/app/`: JVM unit tests covering business logic, state machines, and calculations.
- `docs/` & `doc/`: Design documentation, release notes, and product specifications.

## Build, Test, and Development Commands

Execute all tasks via the Gradle wrapper (`Java 21` required):

- `./gradlew assembleDebug`: Compile and produce the debug APK under `app/build/outputs/apk/debug/`.
- `./gradlew installDebug`: Build and install the debug APK onto a connected ADB device or emulator.
- `./gradlew test`: Run all JVM unit tests.
- `./gradlew testDebugUnitTest`: Run debug variant unit tests directly.
- `./gradlew lint`: Run Android Lint static code analysis.

## Coding Style & Naming Conventions

- **Formatting**: 4-space indentation; standard Kotlin coding conventions.
- **Naming**:
  - `PascalCase` for Composable functions (e.g., `NapRouletteScreen`), classes, and singletons.
  - `camelCase` for member functions, state properties, and parameters.
  - `lowercase_with_underscores` for drawable resources (e.g., `ic_launcher_foreground.xml`) and layout/value files.
- **Compose Practices**: Hoist state to screen-level callers, keep UI components stateless where possible, and adhere strictly to Material 3 design tokens.

## Testing Guidelines

- **Frameworks**: JUnit 4 for JVM unit tests; AndroidX JUnit and Espresso for on-device instrumentation tests.
- **Conventions**: Suffix test files with `Test.kt` (e.g., `NapRouletteLogicTest.kt`).
- Ensure all unit tests pass before committing:
  ```bash
  ./gradlew testDebugUnitTest
  ```

## Commit & Pull Request Guidelines

- **Commit Format**: Follow Conventional Commits: `<type>(<scope>): <description>`.
  - Common types: `feat`, `fix`, `docs`, `refactor`, `chore`.
  - Example: `feat(ui): add authentic flat revolver recoil animation`.
- **Pull Requests**:
  - Provide a concise summary of changes and reference associated issues.
  - Attach screenshots or screen recordings for any Compose UI/animation modifications.
  - Verify that `./gradlew testDebugUnitTest assembleDebug` completes without errors.
