package ziox.ramiro.saes.fragments

import org.junit.Test

import org.junit.Assert.*

class ColumnAnalizerTest {

    @Test
    fun analiseColumns() {
        val jsi = ColumnAnalizer()
        val test1Cols = arrayOf("Grupo","Materia","Profesor","Edificio","Salón","Lunes","Martes","Miércoles","Jueves","Viernes")
        val test1Row = arrayOf("3CM2","TEORIA DE COMUNICACIONES Y SEÑALES","RODRÍGUEZ VÁZQUEZ DANIEL","A1","124", "","11:30 - 13:00","13:00 - 14:30","07:00 - 08:30", "")

        val test2Cols = arrayOf("Secuencia","Materia","Descripcion","Profesor", "Edificio","Salon","Lunes","Martes","Miercoles","Jueves","Viernes", "Sabado")
        val test2Row = arrayOf("1CM21","C101", "CALCULO DIFERENCIAL E INTEGRAL","GONZALEZ MEDINA DIEGO","61", "01", "08:30 - 10:00","08:30 - 10:00","","08:30 - 10:00", "08:30 - 10:00", "")

        assertEquals("[5,9,0,1,2,3,4]", jsi.analiseColumns(test1Cols, test1Row))
        assertEquals("[6,10,0,2,3,4,5]", jsi.analiseColumns(test2Cols, test2Row))
    }
}