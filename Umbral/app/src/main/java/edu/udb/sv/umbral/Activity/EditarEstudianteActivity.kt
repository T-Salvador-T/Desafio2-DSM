package edu.udb.sv.umbral.Activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import edu.udb.sv.umbral.Modelo.Estudiant
import edu.udb.sv.umbral.databinding.ActivityEditarEstudianteBinding

class EditarEstudianteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarEstudianteBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var estudiante: Estudiant.Estudiante

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarEstudianteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        // Obtener estudiante del intent
        estudiante = intent.getSerializableExtra("estudiante") as? Estudiant.Estudiante ?: run {
            Toast.makeText(this, "Error al cargar estudiante", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cargarDatosEstudiante()
        configurarEventos()
    }

    private fun cargarDatosEstudiante() {
        binding.etNombres.setText(estudiante.nombres)
        binding.etApellidos.setText(estudiante.apellidos)
        binding.etCarnet.setText(estudiante.carnet)
        binding.etEdad.setText(estudiante.edad.toString())
        binding.etTelefono.setText(estudiante.telefono)
    }

    private fun configurarEventos() {
        binding.btnActualizar.setOnClickListener {
            actualizarEstudiante()
        }

        binding.btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun actualizarEstudiante() {
        val nombres = binding.etNombres.text.toString().trim()
        val apellidos = binding.etApellidos.text.toString().trim()
        val carnet = binding.etCarnet.text.toString().trim()
        val edad = binding.etEdad.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()

        if (nombres.isEmpty() || apellidos.isEmpty() || carnet.isEmpty() || edad.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, " Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val edadInt = try {
            edad.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, " La edad debe ser un número válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (edadInt <= 0 || edadInt > 100) {
            Toast.makeText(this, " La edad debe estar entre 1 y 100 años", Toast.LENGTH_SHORT).show()
            return
        }

        estudiante.nombres = nombres
        estudiante.apellidos = apellidos
        estudiante.carnet = carnet
        estudiante.edad = edadInt
        estudiante.telefono = telefono

        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnActualizar.isEnabled = false

        // Guardar en Firebase
        db.reference.child("estudiantes").child(estudiante.id).setValue(estudiante)
            .addOnSuccessListener {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnActualizar.isEnabled = true
                Toast.makeText(this, " Estudiante actualizado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnActualizar.isEnabled = true
                Toast.makeText(this, " Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}