# Changelog

## 0.1.0 (2026-03-17)

- Add `envConfig` DSL for declarative environment variable validation
- Add `required()` and `optional()` variable declarations
- Add type parsers: `int()`, `long()`, `boolean()`, `float()`, `list()`
- Add constraint validators: `range()`, `oneOf()`
- Add `default()` for optional fallback values
- Collect all validation errors before throwing `EnvValidationException`
