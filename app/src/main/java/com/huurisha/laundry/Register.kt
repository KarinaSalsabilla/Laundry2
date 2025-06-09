package com.huurisha.laundry

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge
import com.google.firebase.database.*
import com.huurisha.laundry.modeldata.ModelAkun

class Register : AppCompatActivity() {

    private lateinit var inputNama: EditText
    private lateinit var inputPassword: EditText
    private lateinit var inputNoHP: EditText
    private lateinit var inputCabang: EditText
    private lateinit var btnDaftar: Button
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("akun")

        // Initialize Views
        initViews()

        // Set Click Listener
        btnDaftar.setOnClickListener {
            registerUser()
        }
    }

    private fun initViews() {
        inputNama = findViewById(R.id.inputNama)
        inputPassword = findViewById(R.id.inputPassword)
        inputNoHP = findViewById(R.id.inputNoHP)
        inputCabang = findViewById(R.id.inputCabang)
        btnDaftar = findViewById(R.id.btnDaftar)
    }

    private fun registerUser() {
        val nama = inputNama.text.toString().trim()
        val password = inputPassword.text.toString().trim()
        val noHP = inputNoHP.text.toString().trim()
        val cabang = inputCabang.text.toString().trim()

        // Validation dengan string resources
        if (TextUtils.isEmpty(nama)) {
            inputNama.error = getString(R.string.name_empty_error)
            inputNama.requestFocus()
            return
        }

        if (TextUtils.isEmpty(password)) {
            inputPassword.error = getString(R.string.password_empty_error)
            inputPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            inputPassword.error = getString(R.string.password_min_length_error)
            inputPassword.requestFocus()
            return
        }

        if (TextUtils.isEmpty(noHP)) {
            inputNoHP.error = getString(R.string.phone_empty_error)
            inputNoHP.requestFocus()
            return
        }

        if (TextUtils.isEmpty(cabang)) {
            inputCabang.error = getString(R.string.branch_empty_error)
            inputCabang.requestFocus()
            return
        }

        // Check if phone number already exists
        checkPhoneNumberExists(nama, password, noHP, cabang)
    }

    private fun checkPhoneNumberExists(nama: String, password: String, noHP: String, cabang: String) {
        databaseReference.orderByChild("noHP").equalTo(noHP)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@Register, getString(R.string.phone_already_registered), Toast.LENGTH_SHORT).show()
                    } else {
                        // Phone number doesn't exist, proceed with registration
                        saveUserToDatabase(nama, password, noHP, cabang)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Register, getString(R.string.error_message, error.message), Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveUserToDatabase(nama: String, password: String, noHP: String, cabang: String) {
        // Create user object
        val user = ModelAkun(nama, password, noHP, cabang)

        // Generate Firebase push ID (format seperti -OMMjDR0urqw5zgN05d1)
        val key = databaseReference.push().key

        if (key != null) {
            // Save to Firebase using push ID
            databaseReference.child(key).setValue(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@Register, getString(R.string.registration_success), Toast.LENGTH_SHORT).show()

                        // Clear input fields
                        clearInputFields()

                        // Go to login activity (user harus login manual)
                        val intent = Intent(this@Register, login::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Register, getString(R.string.registration_failed, task.exception?.message ?: "Unknown error"), Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this@Register, getString(R.string.error_generating_user_id), Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveLoginState(userId: String, nama: String, password: String, noHP: String, cabang: String) {
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("userId", userId)
        editor.putString("nama", nama)
        editor.putString("password", password)
        editor.putString("noHP", noHP)
        editor.putString("cabang", cabang)
        editor.putBoolean("isLoggedIn", true)
        editor.apply()
    }

    private fun clearInputFields() {
        inputNama.setText("")
        inputPassword.setText("")
        inputNoHP.setText("")
        inputCabang.setText("")
    }
}