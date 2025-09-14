package edu.udb.sv.umbral.Activity

import android.content.Intent
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
import edu.udb.sv.umbral.adapters.EstudianteAdapter
import edu.udb.sv.umbral.databinding.ActivityListaEstudiantesBinding

class ListaEstudiantesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaEstudiantesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private lateinit var adapter: EstudianteAdapter
    private val estudiantesList = mutableListOf<Estudiant.Estudiante>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaEstudiantesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        setupRecyclerView()
        cargarEstudiantes()
        configurarEventos()
    }

    private fun setupRecyclerView() {
        adapter = EstudianteAdapter(estudiantesList) { estudiante ->
            // Editar estudiante
            val intent = Intent(this, EditarEstudianteActivity::class.java).apply {
                putExtra("estudiante", estudiante)
            }
            startActivity(intent)
        }
        binding.rvEstudiantes.layoutManager = LinearLayoutManager(this)
        binding.rvEstudiantes.adapter = adapter
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
                    adapter.notifyDataSetChanged()

                    if (estudiantesList.isEmpty()) {
                        binding.tvEmpty.visibility = android.view.View.VISIBLE
                        binding.rvEstudiantes.visibility = android.view.View.GONE
                    } else {
                        binding.tvEmpty.visibility = android.view.View.GONE
                        binding.rvEstudiantes.visibility = android.view.View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ListaEstudiantesActivity, "Error al cargar estudiantes", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun configurarEventos() {
        binding.btnAgregar.setOnClickListener {
            val intent = Intent(this, EstudianteActivity::class.java)
            startActivity(intent)
        }

        binding.btnVolver.setOnClickListener {
            finish()
        }

    }
}