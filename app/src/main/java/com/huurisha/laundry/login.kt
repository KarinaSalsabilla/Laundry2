package com.huurisha.laundry

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.huurisha.laundry.modeldata.ModelAkun
import com.huurisha.laundry.pelanggan.tambahPelanggan

class login : AppCompatActivity() {

    private lateinit var inputNoHP: EditText
    private lateinit var inputPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btngeser: Button
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            goToMainActivity()
            return
        }

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("akun")

        // Initialize Views FIRST
        initViews()

        // THEN set click listeners (after views are initialized)
        btngeser.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            loginUser()
        }
    }

    private fun initViews() {
        inputNoHP = findViewById(R.id.phone)           // ganti dari inputNoHP
        inputPassword = findViewById(R.id.inputpw)     // ganti dari inputPassword
        btnLogin = findViewById(R.id.simpanlogin)
        btngeser = findViewById(R.id.geser)
    }

    private fun isUserLoggedIn(): Boolean {
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        return prefs.getBoolean("isLoggedIn", false)
    }

    private fun loginUser() {
        val noHP = inputNoHP.text.toString().trim()
        val password = inputPassword.text.toString().trim()

        // Validation dengan string resources
        if (TextUtils.isEmpty(noHP)) {
            inputNoHP.error = getString(R.string.phone_empty_error)
            inputNoHP.requestFocus()
            return
        }

        if (TextUtils.isEmpty(password)) {
            inputPassword.error = getString(R.string.password_empty_error)
            inputPassword.requestFocus()
            return
        }

        // Show loading state dengan string resources
        btnLogin.isEnabled = false
        btnLogin.text = getString(R.string.login_loading)

        // Authenticate user
        authenticateUser(noHP, password)
    }

    // HANYA GUNAKAN METHOD INI - Yang menggunakan Firebase Push ID
    private fun authenticateUser(noHP: String, password: String) {
        // Karena sekarang menggunakan push ID, kita perlu search berdasarkan noHP
        databaseReference.orderByChild("noHP").equalTo(noHP)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Reset button state dengan string resources
                    btnLogin.isEnabled = true
                    btnLogin.text = getString(R.string.login_button)

                    if (snapshot.exists()) {
                        // Loop through results (seharusnya hanya 1 karena noHP unique)
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(ModelAkun::class.java)
                            val userId = userSnapshot.key // Ini adalah push ID seperti -OMMjDR0urqw5zgN05d1

                            if (user != null && user.password == password && userId != null) {
                                // Login successful dengan string resources
                                Toast.makeText(this@login, getString(R.string.login_success), Toast.LENGTH_SHORT).show()

                                // Save login state dengan push ID
                                saveLoginState(userId, user.nama, user.password, user.noHP, user.cabang)

                                // Go to main activity
                                goToMainActivity()
                                return
                            }
                        }
                        // Jika sampai sini berarti password salah dengan string resources
                        Toast.makeText(this@login, getString(R.string.wrong_password), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@login, getString(R.string.phone_not_registered), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Reset button state dengan string resources
                    btnLogin.isEnabled = true
                    btnLogin.text = getString(R.string.login_button)

                    // Menggunakan string format untuk error message
                    Toast.makeText(this@login, getString(R.string.error_message, error.message), Toast.LENGTH_SHORT).show()
                }
            })
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

    private fun goToMainActivity() {
        val intent = Intent(this@login, MainActivity::class.java) // Ganti dari Akun::class.java ke MainActivity::class.java
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}