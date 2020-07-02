# env-validator

[![Tests](https://github.com/philiprehberger/kt-env-validator/actions/workflows/publish.yml/badge.svg)](https://github.com/philiprehberger/kt-env-validator/actions/workflows/publish.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.philiprehberger/env-validator.svg)](https://central.sonatype.com/artifact/com.philiprehberger/env-validator)
[![Last updated](https://img.shields.io/github/last-commit/philiprehberger/kt-env-validator)](https://github.com/philiprehberger/kt-env-validator/commits/main)

Declarative environment variable validation and typed loading for Kotlin.

## Installation

### Gradle Kotlin DSL

```kotlin
implementation("com.philiprehberger:env-validator:0.1.5")
```

### Maven

```xml
<dependency>
    <groupId>com.philiprehberger</groupId>
    <artifactId>env-validator</artifactId>
    <version>0.1.5</version>
</dependency>
```

## Usage

```kotlin
import com.philiprehberger.envvalidator.envConfig

val config = envConfig {
    required("DATABASE_URL")
    required("PORT").int().range(1..65535)
    required("ENV").oneOf("development", "staging", "production")
    optional("DEBUG").boolean().default(false)
    optional("ALLOWED_HOSTS").list()
}

val port: Int? = config["PORT"]
val debug: Boolean? = config["DEBUG"]
```

If any required variable is missing or any validation fails, an `EnvValidationException` is thrown with all errors:

```
Environment validation failed:
  - DATABASE_URL: required but not set
  - PORT: cannot parse 'abc' as Int
```

## API

| Function / Class | Description |
|---|---|
| `envConfig(source, block)` | Validates and loads env vars using DSL; throws on failure |
| `EnvConfigScope.required(name)` | Declares a required variable; returns `EnvVar` |
| `EnvConfigScope.optional(name)` | Declares an optional variable; returns `EnvVar` |
| `EnvVar.int()` | Validates and parses as Int |
| `EnvVar.long()` | Validates and parses as Long |
| `EnvVar.boolean()` | Validates and parses as Boolean (true/false/1/0/yes/no) |
| `EnvVar.float()` | Validates and parses as Float |
| `EnvVar.list(delimiter)` | Parses as a list split by delimiter |
| `EnvVar.range(range)` | Validates Int value is within range |
| `EnvVar.oneOf(vararg values)` | Validates value is one of the allowed values |
| `EnvVar.default(value)` | Sets default value when variable is absent |
| `EnvConfigResult[name]` | Retrieves resolved value by name |
| `EnvValidationException` | Thrown with all validation errors collected |

## Development

```bash
./gradlew build
./gradlew test
```

## Support

If you find this project useful:

⭐ [Star the repo](https://github.com/philiprehberger/kt-env-validator)

🐛 [Report issues](https://github.com/philiprehberger/kt-env-validator/issues?q=is%3Aissue+is%3Aopen+label%3Abug)

💡 [Suggest features](https://github.com/philiprehberger/kt-env-validator/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement)

❤️ [Sponsor development](https://github.com/sponsors/philiprehberger)

🌐 [All Open Source Projects](https://philiprehberger.com/open-source-packages)

💻 [GitHub Profile](https://github.com/philiprehberger)

🔗 [LinkedIn Profile](https://www.linkedin.com/in/philiprehberger)

## License

[MIT](LICENSE)
