package com.example.fitiq

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val googleButton = findViewById<ImageButton>(R.id.googleSignIn)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)

        val facebookBtn = findViewById<ImageButton>(R.id.facebookSignIn)
        facebookBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // If already signed in, go to MainActivity
        if (auth.currentUser != null) {
            checkUserAndRedirect(auth.currentUser!!.uid)
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleButton.setOnClickListener {
            // DON'T signOut here! It causes repeated prompts.
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        forgotPassword.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Reset link sent to email", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    checkUserAndRedirect(user!!.uid)
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    checkUserAndRedirect(user!!.uid)
                } else {
                    Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserAndRedirect(userId: String) {
        val userRef = database.getReference("users").child(userId)
        userRef.get().addOnSuccessListener {
            if (it.exists() && it.hasChild("dailyGoal") && it.hasChild("gender") && it.hasChild("age")) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, UserDetailsActivity::class.java))
            }
            finish()
        }
    }

    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
