package com.philiprehberger.envvalidator

/**
 * Represents an environment variable with chainable validation and type conversion.
 *
 * Values are parsed and validated lazily when the configuration is built.
 * Multiple constraints can be chained to define complex validation rules.
 *
 * @property name the name of the environment variable
 * @property rawValue the raw string value from the environment (null if missing)
 * @property required whether this variable is required
 */
public class EnvVar internal constructor(
    public val name: String,
    private val rawValue: String?,
    private val required: Boolean,
) {
    private var defaultValue: Any? = NO_DEFAULT
    private val validators = mutableListOf<Validator>()
    private var typeName: String = "String"
    private var parser: (String) -> Any = { it }

    /**
     * Validates that the value can be parsed as an [Int].
     *
     * @return this [EnvVar] for chaining
     */
    public fun int(): EnvVar {
        typeName = "Int"
        parser = { it.toInt() }
        return this
    }

    /**
     * Validates that the value can be parsed as a [Long].
     *
     * @return this [EnvVar] for chaining
     */
    public fun long(): EnvVar {
        typeName = "Long"
        parser = { it.toLong() }
        return this
    }

    /**
     * Validates that the value can be parsed as a [Boolean].
     *
     * Accepts `"true"`, `"false"`, `"1"`, `"0"`, `"yes"`, `"no"` (case-insensitive).
     *
     * @return this [EnvVar] for chaining
     */
    public fun boolean(): EnvVar {
        typeName = "Boolean"
        parser = { value ->
            when (value.lowercase()) {
                "true", "1", "yes" -> true
                "false", "0", "no" -> false
                else -> throw IllegalArgumentException("Cannot parse '$value' as Boolean")
            }
        }
        return this
    }

    /**
     * Validates that the value can be parsed as a [Float].
     *
     * @return this [EnvVar] for chaining
     */
    public fun float(): EnvVar {
        typeName = "Float"
        parser = { it.toFloat() }
        return this
    }

    /**
     * Parses the value as a list by splitting on the given [delimiter].
     *
     * @param delimiter the delimiter to split on (default: `","`)
     * @return this [EnvVar] for chaining
     */
    public fun list(delimiter: String = ","): EnvVar {
        typeName = "List"
        parser = { it.split(delimiter).map { item -> item.trim() } }
        return this
    }

    /**
     * Validates that the integer value falls within the given [range].
     *
     * This should be called after [int].
     *
     * @param range the acceptable range of values
     * @return this [EnvVar] for chaining
     */
    public fun range(range: IntRange): EnvVar {
        validators.add(Validator("must be in range $range") { value ->
            val intVal = when (value) {
                is Int -> value
                is String -> value.toIntOrNull()
                else -> null
            }
            intVal != null && intVal in range
        })
        return this
    }

    /**
     * Validates that the value is one of the specified allowed values.
     *
     * @param values the set of allowed values
     * @return this [EnvVar] for chaining
     */
    public fun oneOf(vararg values: String): EnvVar {
        val allowed = values.toSet()
        validators.add(Validator("must be one of $allowed") { value ->
            value.toString() in allowed
        })
        return this
    }

    /**
     * Sets a default value used when the environment variable is not present.
     *
     * @param value the default value
     * @return this [EnvVar] for chaining
     */
    public fun default(value: Any): EnvVar {
        defaultValue = value
        return this
    }

    /**
     * Validates this environment variable and returns any errors encountered.
     *
     * @return a list of error messages (empty if validation passes)
     */
    internal fun validate(): List<String> {
        val errors = mutableListOf<String>()
        val effectiveValue = rawValue ?: if (defaultValue !== NO_DEFAULT) defaultValue.toString() else null

        if (effectiveValue == null) {
            if (required) {
                errors.add("$name: required but not set")
            }
            return errors
        }

        // Try parsing
        val parsed = try {
            parser(effectiveValue)
        } catch (e: Exception) {
            errors.add("$name: cannot parse '$effectiveValue' as $typeName")
            return errors
        }

        // Run validators
        for (validator in validators) {
            if (!validator.check(parsed)) {
                errors.add("$name: ${validator.message}")
            }
        }

        return errors
    }

    /**
     * Returns the resolved value after parsing and applying defaults.
     *
     * @return the parsed value, or null if not set and no default
     */
    internal fun resolvedValue(): Any? {
        val effectiveValue = rawValue ?: if (defaultValue !== NO_DEFAULT) defaultValue.toString() else return null
        return try {
            parser(effectiveValue)
        } catch (e: Exception) {
            null
        }
    }

    private class Validator(val message: String, val check: (Any) -> Boolean)

    private companion object {
        private val NO_DEFAULT = Any()
    }
}
