package edu.udb.sv.umbral.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import edu.udb.sv.umbral.Modelo.Estudiant
import edu.udb.sv.umbral.R

class MateriaAdapter(
    private val materias: List<Estudiant.Materia>,
    private val onEditMateria: (Estudiant.Materia) -> Unit
) : RecyclerView.Adapter<MateriaAdapter.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreMateria)
        val tvCodigo: TextView = itemView.findViewById(R.id.tvCodigoMateria)
        val btnEditar: Button = itemView.findViewById(R.id.btnEditarMateria)
        val btnEliminar: Button = itemView.findViewById(R.id.btnEliminarMateria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_materia, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val materia = materias[position]

        holder.tvNombre.text = materia.nombre
        holder.tvCodigo.text = "Código: ${materia.codigo}"

        holder.btnEditar.setOnClickListener {
            onEditMateria(materia)
        }

        holder.btnEliminar.setOnClickListener {
            mostrarDialogoEliminar(materia, holder.itemView.context)
        }
    }

    private fun mostrarDialogoEliminar(materia: Estudiant.Materia, context: Context) {
        AlertDialog.Builder(context)
            .setTitle("¿Eliminar Materia?")
            .setMessage("¿Estás seguro de eliminar la materia ${materia.nombre}? También se eliminarán todas las notas asociadas.")
            .setPositiveButton("Eliminar") { dialog, which ->
                eliminarMateriaYNotas(materia, context)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarMateriaYNotas(materia: Estudiant.Materia, context: Context) {
        eliminarNotasDeMateria(materia.id, context) {
            // 2. Después eliminar la materia
            db.child("materias").child(materia.id).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, " Materia y notas eliminadas", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, " Error al eliminar materia: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun eliminarNotasDeMateria(materiaId: String, context: Context, onComplete: () -> Unit) {
        db.child("notas")
            .orderByChild("materiaId")
            .equalTo(materiaId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var notasAEliminar = snapshot.children.count()
                        var notasEliminadas = 0

                        if (notasAEliminar == 0) {
                            onComplete()
                            return
                        }

                        for (data in snapshot.children) {
                            data.ref.removeValue()
                                .addOnSuccessListener {
                                    notasEliminadas++
                                    if (notasEliminadas == notasAEliminar) {
                                        onComplete()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, " Error al eliminar algunas notas", Toast.LENGTH_SHORT).show()
                                    notasEliminadas++
                                    if (notasEliminadas == notasAEliminar) {
                                        onComplete()
                                    }
                                }
                        }
                    } else {
                        onComplete()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, " Error al cargar notas: ${error.message}", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
            })
    }

    override fun getItemCount(): Int = materias.size
}