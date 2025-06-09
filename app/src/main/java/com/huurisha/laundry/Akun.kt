package com.huurisha.laundry

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huurisha.laundry.modeldata.ModelAkun
import com.huurisha.laundry.login
import com.google.firebase.database.*

class Akun : AppCompatActivity() {

    private lateinit var namaAkun: EditText
    private lateinit var passwordAkun: EditText
    private lateinit var nohpakun: EditText
    private lateinit var inputcabang: EditText
    private lateinit var updateakun: Button
    private lateinit var logout: Button
    private lateinit var databaseReference: DatabaseReference
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_akun)

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("akun")

        // Initialize Views
        initViews()

        // Load user data
        loadUserData()

        // Set Click Listeners
        updateakun.setOnClickListener {
            updateUserData()
        }

        logout.setOnClickListener {
            logoutUser()
        }
    }

    private fun initViews() {
        namaAkun = findViewById(R.id.namaAkun)
        passwordAkun = findViewById(R.id.passwordAkun)
        nohpakun = findViewById(R.id.nohpakun)
        inputcabang = findViewById(R.id.inputcabang)
        updateakun = findViewById(R.id.updateakun)
        logout = findViewById(R.id.logout)
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)

        if (!prefs.getBoolean("isLoggedIn", false)) {
            // User not logged in, redirect to login
            val intent = Intent(this@Akun, login::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Get user data from SharedPreferences
        val nama = prefs.getString("nama", "") ?: ""
        val password = prefs.getString("password", "") ?: ""
        val noHP = prefs.getString("noHP", "") ?: ""
        val cabang = prefs.getString("cabang", "") ?: ""
        currentUserId = prefs.getString("userId", "") ?: ""

        // Set data to EditTexts
        namaAkun.setText(nama)
        passwordAkun.setText(password)
        nohpakun.setText(noHP)
        inputcabang.setText(cabang)

        // Also load fresh data from Firebase
        loadDataFromFirebase()
    }

    private fun loadDataFromFirebase() {
        if (currentUserId.isEmpty()) return

        databaseReference.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(ModelAkun::class.java)
                    if (user != null) {
                        namaAkun.setText(user.nama)
                        passwordAkun.setText(user.password)
                        nohpakun.setText(user.noHP)
                        inputcabang.setText(user.cabang)

                        // Update SharedPreferences with fresh data
                        updateSharedPreferences(user)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Akun, getString(R.string.error_loading_data, error.message), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserData() {
        val nama = namaAkun.text.toString().trim()
        val password = passwordAkun.text.toString().trim()
        val noHP = nohpakun.text.toString().trim()
        val cabang = inputcabang.text.toString().trim()

        // Validation
        if (TextUtils.isEmpty(nama)) {
            namaAkun.error = getString(R.string.name_empty_error)
            namaAkun.requestFocus()
            return
        }

        if (TextUtils.isEmpty(password)) {
            passwordAkun.error = getString(R.string.password_empty_error)
            passwordAkun.requestFocus()
            return
        }

        if (password.length < 6) {
            passwordAkun.error = getString(R.string.password_min_length_error)
            passwordAkun.requestFocus()
            return
        }

        if (TextUtils.isEmpty(noHP)) {
            nohpakun.error = getString(R.string.phone_validation_error)
            nohpakun.requestFocus()
            return
        }

        if (TextUtils.isEmpty(cabang)) {
            inputcabang.error = getString(R.string.branch_empty_error)
            inputcabang.requestFocus()
            return
        }

        // Check if phone number is changed
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val oldNoHP = prefs.getString("noHP", "") ?: ""

        if (noHP != oldNoHP) {
            // Phone number changed, check if new number already exists
            checkPhoneNumberExistsForUpdate(nama, password, noHP, cabang, oldNoHP)
        } else {
            // Phone number not changed, update directly
            updateUserInDatabase(nama, password, noHP, cabang)
        }
    }

    private fun checkPhoneNumberExistsForUpdate(nama: String, password: String, noHP: String, cabang: String, oldNoHP: String) {
        databaseReference.orderByChild("noHP").equalTo(noHP)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@Akun, getString(R.string.phone_already_used), Toast.LENGTH_SHORT).show()
                    } else {
                        // New phone number is available
                        // Delete old record and create new one
                        val oldKey = oldNoHP.replace(Regex("[^0-9]"), "")
                        val newKey = noHP.replace(Regex("[^0-9]"), "")

                        // Only update if keys are different
                        if (oldKey != newKey) {
                            // Delete old record
                            databaseReference.child(oldKey).removeValue()

                            // Update current user ID
                            currentUserId = newKey
                        }

                        // Create/Update record with new key
                        val user = ModelAkun(nama, password, noHP, cabang)
                        databaseReference.child(currentUserId).setValue(user)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this@Akun, getString(R.string.update_success), Toast.LENGTH_SHORT).show()

                                    // Update SharedPreferences
                                    updateSharedPreferences(user)
                                } else {
                                    Toast.makeText(this@Akun, getString(R.string.update_failed, task.exception?.message), Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Akun, getString(R.string.error_message, error.message), Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateUserInDatabase(nama: String, password: String, noHP: String, cabang: String) {
        val user = ModelAkun(nama, password, noHP, cabang)

        databaseReference.child(currentUserId).setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@Akun, getString(R.string.update_success), Toast.LENGTH_SHORT).show()

                    // Update SharedPreferences
                    updateSharedPreferences(user)
                } else {
                    Toast.makeText(this@Akun, getString(R.string.update_failed, task.exception?.message), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateSharedPreferences(user: ModelAkun) {
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("userId", currentUserId)
        editor.putString("nama", user.nama)
        editor.putString("password", user.password)
        editor.putString("noHP", user.noHP)
        editor.putString("cabang", user.cabang)
        editor.apply()
    }

    private fun logoutUser() {
        // Clear SharedPreferences
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()

        // Redirect to login
        val intent = Intent(this@Akun, login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
    }
}