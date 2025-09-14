package edu.udb.sv.umbral.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.udb.sv.umbral.Modelo.Estudiant
import edu.udb.sv.umbral.R

class EstudianteNotasAdapter(
    private val estudiantes: List<Estudiant.Estudiante>,
    private val notas: List<Estudiant.Nota>,
    private val materias: List<Estudiant.Materia>,
    private val mostrarBotones: Boolean = false
) : RecyclerView.Adapter<EstudianteNotasAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreEstudiante)
        val tvCarnet: TextView = itemView.findViewById(R.id.tvCarnet)
        val tvNotas: TextView = itemView.findViewById(R.id.tvNotas)
        val tvPromedio: TextView = itemView.findViewById(R.id.tvPromedio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estudiante_notas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val estudiante = estudiantes[position]

        holder.tvNombre.text = "${estudiante.nombres} ${estudiante.apellidos}"
        holder.tvCarnet.text = "Carnet: ${estudiante.carnet}"

        // Obtener notas del estudiante
        val notasEstudiante = notas.filter { it.estudianteId == estudiante.id }

        if (notasEstudiante.isEmpty()) {
            holder.tvNotas.text = "No tiene notas registradas"
            holder.tvPromedio.visibility = View.GONE
        } else {
            holder.tvPromedio.visibility = View.VISIBLE
            val notasTexto = StringBuilder(" Notas:\n")
            val promediosPorMateria = mutableMapOf<String, MutableList<Double>>()

            // Organizar notas por materia
            notasEstudiante.forEach { nota ->
                val materia = materias.find { it.id == nota.materiaId }
                materia?.let {
                    if (!promediosPorMateria.containsKey(it.nombre)) {
                        promediosPorMateria[it.nombre] = mutableListOf()
                    }
                    promediosPorMateria[it.nombre]?.add(nota.calificacion)

                    // Mostrar período y nota
                    notasTexto.append("• ${it.nombre} (${nota.periodo}): ${nota.calificacion}\n")
                }
            }

            // Calcular y mostrar promedios por materia
            notasTexto.append("\n Promedios por Materia:\n")
            var promedioGeneral = 0.0
            var materiasConPromedio = 0

            promediosPorMateria.forEach { (materia, calificaciones) ->
                val promedioMateria = calificaciones.average()
                notasTexto.append("• $materia: ${String.format("%.2f", promedioMateria)}\n")
                promedioGeneral += promedioMateria
                materiasConPromedio++
            }

            // Calcular promedio general
            if (materiasConPromedio > 0) {
                promedioGeneral /= materiasConPromedio
                holder.tvPromedio.text = " Promedio General: ${String.format("%.2f", promedioGeneral)}"
            } else {
                holder.tvPromedio.visibility = View.GONE
            }

            holder.tvNotas.text = notasTexto.toString()
        }
    }

    override fun getItemCount(): Int = estudiantes.size
}