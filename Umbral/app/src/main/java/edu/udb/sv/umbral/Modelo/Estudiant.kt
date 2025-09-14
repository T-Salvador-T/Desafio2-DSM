package edu.udb.sv.umbral.Modelo

import java.io.Serializable

class Estudiant {
    data class Estudiante(
        var id: String = "",
        var nombres: String = "",
        var apellidos: String = "",
        var carnet: String = "",
        var edad: Int = 0,
        var telefono: String = "",
        var userId: String = ""
    ): Serializable {
        constructor() : this("", "", "", "", 0, "", "")
    }

    data class Materia(
        var id: String = "",
        var nombre: String = "",
        var codigo: String = "",
        var userId: String = ""
    ): Serializable {
        constructor() : this("", "", "", "")
    }

    data class Nota(
        var id: String = "",
        var estudianteId: String = "",
        var materiaId: String = "",
        var calificacion: Double = 0.0,
        var userId: String = "",
        var periodo: String = "",
        var fecha: String = ""
    ) : Serializable {
        constructor() : this("", "", "", 0.0, "", "", "")
    }

}