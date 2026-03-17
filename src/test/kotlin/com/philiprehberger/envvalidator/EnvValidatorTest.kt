package com.philiprehberger.envvalidator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EnvValidatorTest {

    @Test
    fun `required variable present passes validation`() {
        val result = envConfig(source = mapOf("DB_HOST" to "localhost")) {
            required("DB_HOST")
        }
        assertEquals("localhost", result.get<String>("DB_HOST"))
    }

    @Test
    fun `required variable missing throws exception`() {
        val ex = assertFailsWith<EnvValidationException> {
            envConfig(source = emptyMap()) {
                required("DB_HOST")
            }
        }
        assertTrue(ex.errors.any { "DB_HOST" in it && "required" in it })
    }

    @Test
    fun `optional variable missing returns null`() {
        val result = envConfig(source = emptyMap()) {
            optional("DEBUG")
        }
        assertNull(result.get<String>("DEBUG"))
    }

    @Test
    fun `optional variable with default uses default`() {
        val result = envConfig(source = emptyMap()) {
            optional("PORT").int().default(3000)
        }
        assertEquals(3000, result.get<Int>("PORT"))
    }

    @Test
    fun `int parsing succeeds`() {
        val result = envConfig(source = mapOf("PORT" to "8080")) {
            required("PORT").int()
        }
        assertEquals(8080, result.get<Int>("PORT"))
    }

    @Test
    fun `int parsing failure reports error`() {
        val ex = assertFailsWith<EnvValidationException> {
            envConfig(source = mapOf("PORT" to "abc")) {
                required("PORT").int()
            }
        }
        assertTrue(ex.errors.any { "PORT" in it && "Int" in it })
    }

    @Test
    fun `boolean parsing succeeds`() {
        val result = envConfig(source = mapOf("DEBUG" to "true", "VERBOSE" to "0")) {
            required("DEBUG").boolean()
            required("VERBOSE").boolean()
        }
        assertEquals(true, result.get<Boolean>("DEBUG"))
        assertEquals(false, result.get<Boolean>("VERBOSE"))
    }

    @Test
    fun `boolean parsing with yes and no`() {
        val result = envConfig(source = mapOf("A" to "yes", "B" to "no")) {
            required("A").boolean()
            required("B").boolean()
        }
        assertEquals(true, result.get<Boolean>("A"))
        assertEquals(false, result.get<Boolean>("B"))
    }

    @Test
    fun `list parsing splits by comma`() {
        val result = envConfig(source = mapOf("HOSTS" to "a, b, c")) {
            required("HOSTS").list()
        }
        assertEquals(listOf("a", "b", "c"), result.get<List<String>>("HOSTS"))
    }

    @Test
    fun `range validation passes for value in range`() {
        val result = envConfig(source = mapOf("PORT" to "8080")) {
            required("PORT").int().range(1..65535)
        }
        assertEquals(8080, result.get<Int>("PORT"))
    }

    @Test
    fun `range validation fails for value out of range`() {
        val ex = assertFailsWith<EnvValidationException> {
            envConfig(source = mapOf("PORT" to "99999")) {
                required("PORT").int().range(1..65535)
            }
        }
        assertTrue(ex.errors.any { "PORT" in it && "range" in it })
    }

    @Test
    fun `oneOf validation passes for valid value`() {
        val result = envConfig(source = mapOf("ENV" to "production")) {
            required("ENV").oneOf("development", "staging", "production")
        }
        assertEquals("production", result.get<String>("ENV"))
    }

    @Test
    fun `oneOf validation fails for invalid value`() {
        val ex = assertFailsWith<EnvValidationException> {
            envConfig(source = mapOf("ENV" to "invalid")) {
                required("ENV").oneOf("development", "staging", "production")
            }
        }
        assertTrue(ex.errors.any { "ENV" in it && "one of" in it })
    }

    @Test
    fun `multiple errors are collected`() {
        val ex = assertFailsWith<EnvValidationException> {
            envConfig(source = emptyMap()) {
                required("DB_HOST")
                required("DB_PORT").int()
                required("DB_NAME")
            }
        }
        assertEquals(3, ex.errors.size)
    }

    @Test
    fun `float parsing succeeds`() {
        val result = envConfig(source = mapOf("RATE" to "0.75")) {
            required("RATE").float()
        }
        assertEquals(0.75f, result.get<Float>("RATE"))
    }

    @Test
    fun `long parsing succeeds`() {
        val result = envConfig(source = mapOf("BIG" to "9999999999")) {
            required("BIG").long()
        }
        assertEquals(9999999999L, result.get<Long>("BIG"))
    }
}
