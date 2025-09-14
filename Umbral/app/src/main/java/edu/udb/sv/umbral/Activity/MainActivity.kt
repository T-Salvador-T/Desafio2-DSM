package edu.udb.sv.umbral.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import edu.udb.sv.umbral.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        configurarEventos()
    }

    private fun configurarEventos() {
        binding.cardEstudiantes.setOnClickListener {
            irAListaEstudiantes()
        }

        binding.cardMaterias.setOnClickListener {
            irAListaMaterias()
        }



        binding.cardListados.setOnClickListener {
            irAListados()
        }

        binding.btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun irAListaEstudiantes() {
        val intent = Intent(this, ListaEstudiantesActivity::class.java)
        startActivity(intent)
    }

    private fun irAListaMaterias() {
        val intent = Intent(this, ListaMateriasActivity::class.java)
        startActivity(intent)
    }


    private fun irAListados() {
        val intent = Intent(this, ListActivity::class.java)
        startActivity(intent)
    }


    private fun cerrarSesion() {
        auth.signOut()
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}