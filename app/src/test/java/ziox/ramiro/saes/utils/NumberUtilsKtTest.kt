package ziox.ramiro.saes.utils

import org.junit.Test

import org.junit.Assert.*

class NumberUtilsKtTest {

    @Test
    fun toStringPresition() {
        val test = Math.PI
        val test2 = 12.345678
        val test3 = 1.0

        assertEquals("3.1416", test.toStringPresition(4))
        assertEquals("12.35", test2.toStringPresition(2))
        assertEquals("1", test3.toStringPresition(5))
    }

    @Test
    fun toHour() {
        val test = 15.5
        val test2 = 20.25
        val test3 = 1.0

        assertEquals("15:30", test.toHour())
        assertEquals("20:15", test2.toHour())
        assertEquals("01:00", test3.toHour())
    }

    @Test
    fun decimal() {
        val test = 3.1416
        val test2 = 12.345678
        val test3 = 1.0

        assertEquals(0.1416, test.decimal(), 0.001)
        assertEquals(0.345678, test2.decimal(), 0.001)
        assertEquals(0.0, test3.decimal(), 0.001)
    }

    @Test
    fun mean() {
        val testInts = arrayOf(1,2,3,4,5,6,7,8,9)
        val testDouble = arrayOf(1.9,2.56,45.2,1.1)


        assertEquals(5.0, testInts.mean(), 0.000001)
        assertEquals(12.69, testDouble.mean(), 0.000001)
    }
}