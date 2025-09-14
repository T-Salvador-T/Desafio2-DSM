package edu.udb.sv.umbral.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import edu.udb.sv.umbral.Modelo.Estudiant
import edu.udb.sv.umbral.databinding.ActivityListBinding
import edu.udb.sv.umbral.adapters.EstudianteNotasAdapter

class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private lateinit var adapter: EstudianteNotasAdapter
    private val estudiantesList = mutableListOf<Estudiant.Estudiante>()
    private val notasList = mutableListOf<Estudiant.Nota>()
    private val materiasList = mutableListOf<Estudiant.Materia>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        setupRecyclerView()
        cargarDatos()
        configurarEventos()
    }

    private fun setupRecyclerView() {
        adapter = EstudianteNotasAdapter(estudiantesList, notasList, materiasList, false)
        binding.rvEstudiantesNotas.layoutManager = LinearLayoutManager(this)
        binding.rvEstudiantesNotas.adapter = adapter
    }

    private fun cargarDatos() {
        val userId = auth.currentUser?.uid ?: return

        // Cargar estudiantes
        db.child("estudiantes")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    estudiantesList.clear()
                    for (data in snapshot.children) {
                        val estudiante = data.getValue(Estudiant.Estudiante::class.java)
                        estudiante?.let { estudiantesList.add(it) }
                    }
                    cargarNotas()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ListActivity, "Error al cargar estudiantes", Toast.LENGTH_SHORT).show()
                }
            })

        // Cargar materias
        db.child("materias")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    materiasList.clear()
                    for (data in snapshot.children) {
                        val materia = data.getValue(Estudiant.Materia::class.java)
                        materia?.let { materiasList.add(it) }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ListActivity, "Error al cargar materias", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun cargarNotas() {
        val userId = auth.currentUser?.uid ?: return

        db.child("notas")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notasList.clear()
                    for (data in snapshot.children) {
                        val nota = data.getValue(Estudiant.Nota::class.java)
                        nota?.let { notasList.add(it) }
                    }
                    mostrarDatosCombinados()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ListActivity, "Error al cargar notas", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun mostrarDatosCombinados() {
        if (estudiantesList.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.rvEstudiantesNotas.visibility = android.view.View.GONE
        } else {
            binding.tvEmpty.visibility = android.view.View.GONE
            binding.rvEstudiantesNotas.visibility = android.view.View.VISIBLE
            adapter.notifyDataSetChanged()
        }
    }

    private fun configurarEventos() {
        binding.btnVolver.setOnClickListener {
            finish()
        }

        binding.btnActualizar.setOnClickListener {
            cargarDatos()
            Toast.makeText(this, "Lista actualizada", Toast.LENGTH_SHORT).show()
        }
    }
}