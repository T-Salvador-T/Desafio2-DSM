package edu.udb.sv.umbral.Activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
// CAMBIO: Se importa FirebaseDatabase en lugar de Firestore
import com.google.firebase.database.FirebaseDatabase
import edu.udb.sv.umbral.Modelo.Estudiant
import edu.udb.sv.umbral.databinding.ActivityMateriaBinding
import java.util.UUID

class MateriaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMateriaBinding
    private lateinit var auth: FirebaseAuth
    // CAMBIO: La variable 'db' ahora es de tipo FirebaseDatabase
    private lateinit var db: FirebaseDatabase
    private var materiaEditando: Estudiant.Materia? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMateriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        // CAMBIO: Se obtiene la instancia de Realtime Database
        db = FirebaseDatabase.getInstance()

        configurarEventos()
    }

    private fun configurarEventos() {
        binding.btnGuardar.setOnClickListener {
            guardarMateria()
        }



        binding.btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun guardarMateria() {
        val nombre = binding.etNombreMateria.text.toString().trim()
        val codigo = binding.etCodigoMateria.text.toString().trim()

        if (nombre.isEmpty() || codigo.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        auth.currentUser?.let { user ->
            val userId = user.uid
            val materiaId = materiaEditando?.id ?: UUID.randomUUID().toString()

            // Asegúrate que tu modelo Estudiant.Materia existe y tiene estos campos
            val materia = Estudiant.Materia(
                id = materiaId,
                nombre = nombre,
                codigo = codigo,
                userId = userId
            )

            // Opcional: Desactivar botón para evitar dobles clics
            binding.btnGuardar.isEnabled = false

            // CAMBIO GRANDE: Lógica para guardar en Realtime Database
            // Se obtiene la referencia al nodo 'materias', se crea un hijo con el ID
            // y se guarda el objeto 'materia' usando setValue().
            db.getReference("materias")
                .child(materiaId)
                .setValue(materia)
                .addOnCompleteListener { task ->
                    // Vuelve a activar el botón cuando la operación termina
                    binding.btnGuardar.isEnabled = true

                    if (task.isSuccessful) {
                        Log.d("FirebaseDebug", "Éxito al guardar la materia en Realtime DB")
                        val mensaje = if (materiaEditando != null) {
                            "Materia actualizada correctamente"
                        } else {
                            "Materia registrada correctamente"
                        }
                        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                        limpiarCampos()
                    } else {
                        Log.e("FirebaseDebug", "Error al guardar materia: ${task.exception?.message}")
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } ?: run {
            Toast.makeText(this, "Error: Debes iniciar sesión para guardar.", Toast.LENGTH_LONG).show()
        }
    }

    private fun limpiarCampos() {
        binding.etNombreMateria.setText("")
        binding.etCodigoMateria.setText("")
        materiaEditando = null
        binding.btnGuardar.text = "Guardar Materia"
    }
}