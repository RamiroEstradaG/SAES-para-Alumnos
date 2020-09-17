package ziox.ramiro.saes.utils

import org.junit.Test

import org.junit.Assert.*

class StringUtilsKtTest {

    @Test
    fun generateRandomString() {
        val random1 = generateRandomString(10)
        val random2 = generateRandomString(10)

        assertNotEquals(random1, random2)
    }

    @Test
    fun isUrl() {
        val url = "https://www.google.com/"
        val notUrl = "String, 12345678"

        assert(url.isUrl())
        assertFalse(notUrl.isUrl())
    }

    @Test
    fun toProperCase() {
        val test = "string de prueba de la 1234"
        val test2 = "string de prueba (parentesis de la 1234) xxiv"

        assertEquals("String de Prueba de la 1234", test.toProperCase())
        assertEquals("String de Prueba (parentesis de la 1234) XXIV", test2.toProperCase() )
    }

    @Test
    fun getInitials() {
        val test = "string de prueba de la hola"
        val test2 = "string de prueba (parentesis de la 1234) xxiv"

        assertEquals("SPH", test.getInitials())
        assertEquals("SP XXIV", test2.getInitials())
    }

    @Test
    fun toDate() {
        //TODO: Se necesita un String de fecha real en la cita de reinscripcion
    }

    @Test
    fun dividirHoras() {
        val test = "12:45 - 14:59"
        val test2 = "Horario de clase de 14:45 - 16:59 y de 12:45 - 14:59"
        val test3 = "Horariod de 12:45 a 14:59"

        assertEquals(Pair(12+(45/60.0), 14+(59/60.0)),dividirHoras(test))
        assertEquals(Pair(14+(45/60.0), 16+(59/60.0)),dividirHoras(test2))
        assertNull(dividirHoras(test3))
    }

    @Test
    fun toDateString() {
        //TODO: Se necesita un String de fecha real en la cita de reinscripcion
    }

    @Test
    fun hourToDouble() {
        val test = "20:14"
        val test2 = "04:26"
        val test3 = "String"

        assertEquals(20+(14/60.0), hourToDouble(test), 0.0)
        assertEquals(4+(26/60.0), hourToDouble(test2), 0.0)
        assertEquals(-1.0, hourToDouble(test3), 0.0)
    }
}