package edu.udb.sv.umbral.Activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import edu.udb.sv.umbral.Modelo.Estudiant
import edu.udb.sv.umbral.databinding.ActivityEditarMateriaBinding

class EditarMateriaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarMateriaBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var materia: Estudiant.Materia

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarMateriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        // Obtener materia del intent
        materia = intent.getSerializableExtra("materia") as? Estudiant.Materia ?: run {
            Toast.makeText(this, "Error al cargar materia", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        cargarDatosMateria()
        configurarEventos()
    }

    private fun cargarDatosMateria() {
        binding.etNombreMateria.setText(materia.nombre)
        binding.etCodigoMateria.setText(materia.codigo)
    }

    private fun configurarEventos() {
        binding.btnActualizar.setOnClickListener {
            actualizarMateria()
        }

        binding.btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun actualizarMateria() {
        val nombre = binding.etNombreMateria.text.toString().trim()
        val codigo = binding.etCodigoMateria.text.toString().trim()

        if (nombre.isEmpty() || codigo.isEmpty()) {
            Toast.makeText(this, " Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Actualizar objeto materia
        materia.nombre = nombre
        materia.codigo = codigo

        // Mostrar progreso
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnActualizar.isEnabled = false

        // Guardar en Firebase
        db.reference.child("materias").child(materia.id).setValue(materia)
            .addOnSuccessListener {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnActualizar.isEnabled = true
                Toast.makeText(this, " Materia actualizada", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnActualizar.isEnabled = true
                Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}