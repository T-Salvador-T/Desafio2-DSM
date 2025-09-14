package edu.udb.sv.umbral.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import edu.udb.sv.umbral.Activity.NotaEditarActivity
import edu.udb.sv.umbral.Modelo.Estudiant
import edu.udb.sv.umbral.R

class EstudianteAdapter(
    private val estudiantes: List<Estudiant.Estudiante>,
    private val onEditEstudiante: (Estudiant.Estudiante) -> Unit
) : RecyclerView.Adapter<EstudianteAdapter.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    // ✅ VIEWHOLDER DENTRO DE LA CLASE
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreEstudiante)
        val tvCarnet: TextView = itemView.findViewById(R.id.tvCarnet)
        val tvEdad: TextView = itemView.findViewById(R.id.tvEdad)
        val btnEditarEstudiante: Button = itemView.findViewById(R.id.btnEditarEstudiante)
        val btnEditarNotas: Button = itemView.findViewById(R.id.btnEditarNotas)  // ✅ NUEVO
        val btnEliminarEstudiante: Button = itemView.findViewById(R.id.btnEliminarEstudiante)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estudiante, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val estudiante = estudiantes[position]

        holder.tvNombre.text = "${estudiante.nombres} ${estudiante.apellidos}"
        holder.tvCarnet.text = "Carnet: ${estudiante.carnet}"
        holder.tvEdad.text = "Edad: ${estudiante.edad} años"

        // ✅ BOTÓN EDITAR ESTUDIANTE (DATOS PERSONALES)
        holder.btnEditarEstudiante.setOnClickListener {
            onEditEstudiante(estudiante)
        }

        // ✅ BOTÓN ELIMINAR ESTUDIANTE
        holder.btnEliminarEstudiante.setOnClickListener {
            mostrarDialogoEliminar(estudiante, holder.itemView.context)
        }

        // ✅ BOTÓN EDITAR NOTAS (NUEVO)
        holder.btnEditarNotas.setOnClickListener {
            val intent = Intent(holder.itemView.context, NotaEditarActivity::class.java).apply {
                putExtra("estudiante", estudiante)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    private fun mostrarDialogoEliminar(estudiante: Estudiant.Estudiante, context: Context) {
        AlertDialog.Builder(context)
            .setTitle("¿Eliminar Estudiante?")
            .setMessage("¿Estás seguro de eliminar a ${estudiante.nombres} ${estudiante.apellidos}?")
            .setPositiveButton("Eliminar") { dialog, which ->
                eliminarEstudiante(estudiante, context)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarEstudiante(estudiante: Estudiant.Estudiante, context: Context) {
        db.child("estudiantes").child(estudiante.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "✅ Estudiante eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "❌ Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int = estudiantes.size
}