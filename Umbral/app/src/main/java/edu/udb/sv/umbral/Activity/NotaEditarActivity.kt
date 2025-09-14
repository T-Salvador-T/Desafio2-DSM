package edu.udb.sv.umbral.Activity

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import edu.udb.sv.umbral.Modelo.Estudiant
import edu.udb.sv.umbral.databinding.ActivityNotaEditarBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class NotaEditarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotaEditarBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private lateinit var estudiante: Estudiant.Estudiante
    private val materiasList = mutableListOf<Estudiant.Materia>()
    private val notasExistentes = mutableListOf<Estudiant.Nota>()
    private val periodos = listOf("Primer Parcial", "Segundo Parcial", "Tercer Parcial", "Cuarto Parcial", "Examen Final")
    private var notaActual: Estudiant.Nota? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotaEditarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().reference

        // Obtener estudiante del intent
        estudiante = intent.getSerializableExtra("estudiante") as? Estudiant.Estudiante ?: run {
            Toast.makeText(this, "Error al cargar estudiante", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        configurarUI()
        cargarMaterias()
        configurarEventos()
    }

    private fun configurarUI() {
        binding.tvTitulo.text = "Registrar Notas de ${estudiante.nombres} ${estudiante.apellidos}"
        binding.tvCarnet.text = "Carnet: ${estudiante.carnet}"

        // Configurar spinner de períodos
        val periodoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periodos)
        periodoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPeriodo.adapter = periodoAdapter
    }

    private fun cargarMaterias() {
        val userId = auth.currentUser?.uid ?: return

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

                    val nombresMaterias = materiasList.map { it.nombre }
                    val adapter = ArrayAdapter(this@NotaEditarActivity,
                        android.R.layout.simple_spinner_item, nombresMaterias)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerMaterias.adapter = adapter

                    // Cargar notas después de tener las materias
                    cargarNotasExistentes()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@NotaEditarActivity, "Error al cargar materias", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun cargarNotasExistentes() {
        val userId = auth.currentUser?.uid ?: return

        db.child("notas")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notasExistentes.clear()
                    for (data in snapshot.children) {
                        val nota = data.getValue(Estudiant.Nota::class.java)
                        if (nota?.estudianteId == estudiante.id) {
                            nota?.let { notasExistentes.add(it) }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@NotaEditarActivity, "Error al cargar notas", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun configurarEventos() {
        binding.btnGuardar.setOnClickListener {
            guardarNota()
        }

        binding.btnBuscar.setOnClickListener {
            buscarNotaExistente()
        }

        binding.btnEliminar.setOnClickListener {
            eliminarNota()
        }

        binding.btnLimpiar.setOnClickListener {
            limpiarCampos()
        }

        binding.btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun buscarNotaExistente() {
        val materiaIndex = binding.spinnerMaterias.selectedItemPosition
        val periodoIndex = binding.spinnerPeriodo.selectedItemPosition

        if (materiaIndex == -1 || periodoIndex == -1) {
            Toast.makeText(this, " Seleccione materia y período", Toast.LENGTH_SHORT).show()
            return
        }

        val materia = materiasList[materiaIndex]
        val periodo = periodos[periodoIndex]

        notaActual = notasExistentes.find {
            it.materiaId == materia.id && it.periodo == periodo
        }

        if (notaActual != null) {
            binding.etNota.setText(notaActual!!.calificacion.toString())
            binding.btnEliminar.isEnabled = true
            Toast.makeText(this, " Nota encontrada", Toast.LENGTH_SHORT).show()
        } else {
            binding.etNota.setText("")
            binding.btnEliminar.isEnabled = false
            notaActual = null
            Toast.makeText(this, "ℹ No hay nota registrada para este período", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarNota() {
        if (notaActual == null) {
            Toast.makeText(this, "❌ No hay nota seleccionada para eliminar", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("¿Eliminar Nota?")
            .setMessage("¿Estás seguro de eliminar la nota de ${notaActual!!.periodo}?")
            .setPositiveButton("Eliminar") { dialog, which ->
                procederEliminacion()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun procederEliminacion() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnEliminar.isEnabled = false

        db.child("notas").child(notaActual!!.id).removeValue()
            .addOnSuccessListener {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this, "✅ Nota eliminada correctamente", Toast.LENGTH_SHORT).show()
                limpiarCampos()
                // Actualizar lista local
                notasExistentes.remove(notaActual)
                notaActual = null
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnEliminar.isEnabled = true
                Toast.makeText(this, "❌ Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limpiarCampos() {
        binding.etNota.setText("")
        binding.btnEliminar.isEnabled = false
        notaActual = null
    }

    private fun guardarNota() {
        val materiaIndex = binding.spinnerMaterias.selectedItemPosition
        val periodoIndex = binding.spinnerPeriodo.selectedItemPosition
        val notaStr = binding.etNota.text.toString().trim()

        if (materiaIndex == -1 || periodoIndex == -1 || notaStr.isEmpty()) {
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

        val materia = materiasList[materiaIndex]
        val periodo = periodos[periodoIndex]
        val userId = auth.currentUser?.uid ?: ""
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        // Usar ID existente o crear uno nuevo
        val notaId = notaActual?.id ?: UUID.randomUUID().toString()

        val notaObj = Estudiant.Nota(
            id = notaId,
            estudianteId = estudiante.id,
            materiaId = materia.id,
            calificacion = nota,
            userId = userId,
            periodo = periodo,
            fecha = fecha
        )

        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnGuardar.isEnabled = false
        binding.btnEliminar.isEnabled = false

        db.child("notas").child(notaObj.id).setValue(notaObj)
            .addOnSuccessListener {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnGuardar.isEnabled = true
                binding.btnEliminar.isEnabled = true

                val mensaje = if (notaActual != null) {
                    " Nota actualizada correctamente"
                } else {
                    " Nota registrada correctamente"
                }
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()

                // Actualizar lista de notas
                if (notaActual == null) {
                    notasExistentes.add(notaObj)
                    notaActual = notaObj
                } else {
                    val index = notasExistentes.indexOfFirst { it.id == notaActual!!.id }
                    if (index != -1) {
                        notasExistentes[index] = notaObj
                    }
                    notaActual = notaObj
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnGuardar.isEnabled = true
                binding.btnEliminar.isEnabled = true
                Toast.makeText(this, " Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}