package com.philiprehberger.envvalidator

/**
 * DSL scope for declaring environment variable requirements.
 *
 * Use [required] to declare mandatory variables and [optional] for variables
 * with default values or that may be absent.
 */
public class EnvConfigScope internal constructor(private val source: Map<String, String>) {

    internal val vars = mutableListOf<EnvVar>()

    /**
     * Declares a required environment variable.
     *
     * Validation will fail if this variable is not present in the source map
     * and no default is set.
     *
     * @param name the environment variable name
     * @return an [EnvVar] for further configuration (type, constraints, etc.)
     */
    public fun required(name: String): EnvVar {
        val envVar = EnvVar(name, source[name], required = true)
        vars.add(envVar)
        return envVar
    }

    /**
     * Declares an optional environment variable.
     *
     * Validation will not fail if this variable is absent, unless additional
     * constraints are violated when a value is present.
     *
     * @param name the environment variable name
     * @return an [EnvVar] for further configuration (type, constraints, default, etc.)
     */
    public fun optional(name: String): EnvVar {
        val envVar = EnvVar(name, source[name], required = false)
        vars.add(envVar)
        return envVar
    }
}

/**
 * The result of environment configuration validation.
 *
 * @property values a map of variable names to their resolved (parsed) values
 */
public data class EnvConfigResult(
    public val values: Map<String, Any?>,
) {
    /**
     * Retrieves a resolved value by variable name, cast to the expected type.
     *
     * @param name the environment variable name
     * @return the resolved value, or null if not set
     */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> get(name: String): T? = values[name] as T?
}

/**
 * Validates and loads environment variables using a declarative DSL.
 *
 * All declared variables are validated, and if any errors are found,
 * an [EnvValidationException] is thrown with all errors collected.
 *
 * @param source the map of environment variables to validate against (defaults to [System.getenv])
 * @param block the DSL block declaring required and optional variables
 * @return an [EnvConfigResult] containing all resolved values
 * @throws EnvValidationException if any validation errors are found
 */
public fun envConfig(
    source: Map<String, String> = System.getenv(),
    block: EnvConfigScope.() -> Unit,
): EnvConfigResult {
    val scope = EnvConfigScope(source)
    scope.block()

    val allErrors = scope.vars.flatMap { it.validate() }
    if (allErrors.isNotEmpty()) {
        throw EnvValidationException(allErrors)
    }

    val values = scope.vars.associate { it.name to it.resolvedValue() }
    return EnvConfigResult(values)
}
