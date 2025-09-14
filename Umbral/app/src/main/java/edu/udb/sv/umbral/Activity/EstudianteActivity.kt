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
import edu.udb.sv.umbral.databinding.ActivityEstudianteBinding
import java.util.UUID

class EstudianteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEstudianteBinding
    private lateinit var auth: FirebaseAuth
    // CAMBIO: La variable 'db' ahora es de tipo FirebaseDatabase
    private lateinit var db: FirebaseDatabase
    private var estudianteEditando: Estudiant.Estudiante? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEstudianteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        Log.d("FirebaseDebug", "Firebase Auth: ${auth.currentUser?.uid}")
        Log.d("FirebaseDebug", "Realtime Database instance created")

        configurarEventos()
    }

    private fun configurarEventos() {
        binding.btnGuardar.setOnClickListener {
            Log.d("FirebaseDebug", "Botón Guardar clickeado")
            guardarEstudiante()
        }
        // ... tus otros botones no cambian
        binding.btnVolver.setOnClickListener { finish() }
    }

    private fun guardarEstudiante() {
        val nombres = binding.etNombres.text.toString().trim()
        val apellidos = binding.etApellidos.text.toString().trim()
        val carnet = binding.etCarnet.text.toString().trim()
        val edad = binding.etEdad.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()

        // ... tus validaciones no cambian
        if (nombres.isEmpty() || apellidos.isEmpty() || carnet.isEmpty() || edad.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }
        val edadInt = try {
            edad.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "La edad debe ser un número válido", Toast.LENGTH_SHORT).show()
            return
        }

        auth.currentUser?.let { user ->
            val userId = user.uid
            val estudianteId = estudianteEditando?.id ?: UUID.randomUUID().toString()

            val estudiante = Estudiant.Estudiante(
                id = estudianteId,
                nombres = nombres,
                apellidos = apellidos,
                carnet = carnet,
                edad = edadInt,
                telefono = telefono,
                userId = userId
            )

            binding.progressBar.visibility = View.VISIBLE
            binding.btnGuardar.isEnabled = false

            Log.d("FirebaseDebug", "Intentando guardar en Realtime Database...")

            // CAMBIO GRANDE: Lógica para guardar en Realtime Database
            // 1. Obtenemos una referencia al "nodo" principal 'estudiantes'.
            // 2. Con .child() creamos una nueva entrada con el ID del estudiante.
            // 3. Con .setValue() guardamos el objeto completo.
            db.getReference("estudiantes")
                .child(estudianteId)
                .setValue(estudiante)
                .addOnCompleteListener { task ->
                    // Este listener se ejecuta cuando la operación termina (éxito o fracaso)
                    binding.progressBar.visibility = View.GONE
                    binding.btnGuardar.isEnabled = true

                    if (task.isSuccessful) {
                        Log.d("FirebaseDebug", "Éxito al guardar en Realtime Database")
                        val mensaje = if (estudianteEditando != null) {
                            "Estudiante actualizado correctamente"
                        } else {
                            "Estudiante registrado correctamente"
                        }
                        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                        limpiarCampos()
                        estudianteEditando = null
                        binding.btnGuardar.text = "Guardar Estudiante"
                    } else {
                        Log.e("FirebaseDebug", "Error al guardar: ${task.exception?.message}")
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } ?: run {
            Toast.makeText(this, "Error: No hay un usuario autenticado.", Toast.LENGTH_LONG).show()
        }
    }

    private fun limpiarCampos() {
        binding.etNombres.setText("")
        binding.etApellidos.setText("")
        binding.etCarnet.setText("")
        binding.etEdad.setText("")
        binding.etTelefono.setText("")
        estudianteEditando = null
        binding.btnGuardar.text = "Guardar Estudiante"
    }
}