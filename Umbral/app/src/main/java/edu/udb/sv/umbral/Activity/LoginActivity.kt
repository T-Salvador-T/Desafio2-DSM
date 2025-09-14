package edu.udb.sv.umbral.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import edu.udb.sv.umbral.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Verificar si el usuario ya está logueado
        verificarSesionActiva()

        // Configurar los eventos de los botones
        configurarEventos()
    }

    private fun verificarSesionActiva() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Si ya hay una sesión activa, ir directamente a MainActivity
            irAMainActivity()
        }
    }

    private fun configurarEventos() {
        binding.btnLogin.setOnClickListener {
            iniciarSesion()
        }

        binding.tvRegistrar.setOnClickListener {
            irARegistro()
        }
    }

    private fun iniciarSesion() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validar campos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar progreso (puedes agregar un ProgressBar después)
        binding.btnLogin.isEnabled = false

        // Iniciar sesión con Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.btnLogin.isEnabled = true

                if (task.isSuccessful) {
                    // Login exitoso
                    Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                    irAMainActivity()
                } else {
                    // Error en login
                    val errorMessage = task.exception?.message ?: "Error desconocido"
                    Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun irARegistro() {
        val intent = Intent(this, RegistroActivity::class.java)
        startActivity(intent)
        // No llamamos finish() para que pueda volver al login si lo desea
    }

    private fun irAMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Cerramos LoginActivity para que no pueda volver atrás
    }
}