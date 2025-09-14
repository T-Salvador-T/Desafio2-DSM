package edu.udb.sv.umbral.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.play.integrity.internal.ad
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import edu.udb.sv.umbral.Modelo.Estudiant
import edu.udb.sv.umbral.adapters.MateriaAdapter
import edu.udb.sv.umbral.databinding.ActivityListaMateriasBinding

class ListaMateriasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaMateriasBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private lateinit var adapter: MateriaAdapter
    private val materiasList = mutableListOf<Estudiant.Materia>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaMateriasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        setupRecyclerView()
        cargarMaterias()
        configurarEventos()
    }

    private fun setupRecyclerView() {
        adapter = MateriaAdapter(materiasList) { materia ->
            // Editar materia
            val intent = Intent(this, EditarMateriaActivity::class.java).apply {
                putExtra("materia", materia)
            }
            startActivity(intent)
        }
        binding.rvMaterias.layoutManager = LinearLayoutManager(this)
        binding.rvMaterias.adapter = adapter
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
                    adapter.notifyDataSetChanged()

                    if (materiasList.isEmpty()) {
                        binding.tvEmpty.visibility = android.view.View.VISIBLE
                    } else {
                        binding.tvEmpty.visibility = android.view.View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ListaMateriasActivity, "Error al cargar materias", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun configurarEventos() {
        binding.btnAgregar.setOnClickListener {
            val intent = Intent(this, MateriaActivity::class.java)
            startActivity(intent)
        }

        binding.btnVolver.setOnClickListener {
            finish()
        }
    }
}