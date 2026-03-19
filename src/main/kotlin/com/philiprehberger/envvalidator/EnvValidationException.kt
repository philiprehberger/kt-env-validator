package com.philiprehberger.envvalidator

/**
 * Exception thrown when environment variable validation fails.
 *
 * Contains a list of all validation errors encountered during configuration loading.
 * The exception message includes all errors formatted as a bulleted list.
 *
 * @property errors the list of validation error messages
 */
public class EnvValidationException(
    public val errors: List<String>,
) : RuntimeException(
    "Environment validation failed:\n${errors.joinToString("\n") { "  - $it" }}",
)
