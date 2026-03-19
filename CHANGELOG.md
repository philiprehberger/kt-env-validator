# Changelog

## [0.1.2] - 2026-03-18

- Upgrade to Kotlin 2.0.21 and Gradle 8.12
- Enable explicitApi() for stricter public API surface
- Add issueManagement to POM metadata

## 0.1.0 (2026-03-17)

- Add `envConfig` DSL for declarative environment variable validation
- Add `required()` and `optional()` variable declarations
- Add type parsers: `int()`, `long()`, `boolean()`, `float()`, `list()`
- Add constraint validators: `range()`, `oneOf()`
- Add `default()` for optional fallback values
- Collect all validation errors before throwing `EnvValidationException`
