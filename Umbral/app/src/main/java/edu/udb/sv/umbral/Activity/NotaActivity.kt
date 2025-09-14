package edu.udb.sv.umbral.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import edu.udb.sv.umbral.Modelo.Estudiant
import edu.udb.sv.umbral.databinding.ActivityNotaBinding
import java.util.UUID

class NotaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotaBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private val estudiantesList = mutableListOf<Estudiant.Estudiante>()
    private val materiasList = mutableListOf<Estudiant.Materia>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        cargarEstudiantes()
        cargarMaterias()
        configurarEventos()
    }

    private fun cargarEstudiantes() {
        val userId = auth.currentUser?.uid ?: return

        db.child("estudiantes")
            .orderByChild("userId")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    estudiantesList.clear()
                    for (data in snapshot.children) {
                        val estudiante = data.getValue(Estudiant.Estudiante::class.java)
                        estudiante?.let { estudiantesList.add(it) }
                    }

                    val nombresEstudiantes = estudiantesList.map { "${it.nombres} ${it.apellidos}" }
                    val adapter = ArrayAdapter(this@NotaActivity, android.R.layout.simple_spinner_item, nombresEstudiantes)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerEstudiantes.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@NotaActivity, "Error al cargar estudiantes", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun cargarMaterias() {
        val userId = auth.currentUser?.uid ?: return

        db.child("materias")
            .orderByChild("userId")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    materiasList.clear()
                    for (data in snapshot.children) {
                        val materia = data.getValue(Estudiant.Materia::class.java)
                        materia?.let { materiasList.add(it) }
                    }

                    val nombresMaterias = materiasList.map { it.nombre }
                    val adapter = ArrayAdapter(this@NotaActivity, android.R.layout.simple_spinner_item, nombresMaterias)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerMaterias.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@NotaActivity, "Error al cargar materias", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun configurarEventos() {
        binding.btnGuardarNota.setOnClickListener {
            guardarNota()
        }

        binding.btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun guardarNota() {
        val estudianteIndex = binding.spinnerEstudiantes.selectedItemPosition
        val materiaIndex = binding.spinnerMaterias.selectedItemPosition
        val notaStr = binding.etNota.text.toString().trim()

        if (estudianteIndex == -1 || materiaIndex == -1 || notaStr.isEmpty()) {
            Toast.makeText(this, "❌ Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val nota = try {
            notaStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "❌ La nota debe ser un número válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (nota < 0 || nota > 10) {
            Toast.makeText(this, "❌ La nota debe estar entre 0 y 10", Toast.LENGTH_SHORT).show()
            return
        }

        val estudiante = estudiantesList[estudianteIndex]
        val materia = materiasList[materiaIndex]
        val userId = auth.currentUser?.uid ?: ""
        val notaId = UUID.randomUUID().toString()

        val notaObj = Estudiant.Nota(
            id = notaId,
            estudianteId = estudiante.id,
            materiaId = materia.id,
            calificacion = nota,
            userId = userId
        )

        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnGuardarNota.isEnabled = false

        db.child("notas").child(notaId).setValue(notaObj)
            .addOnSuccessListener {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnGuardarNota.isEnabled = true
                Toast.makeText(this, " Nota registrada correctamente", Toast.LENGTH_SHORT).show()
                binding.etNota.setText("")
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnGuardarNota.isEnabled = true
                Toast.makeText(this, " Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}