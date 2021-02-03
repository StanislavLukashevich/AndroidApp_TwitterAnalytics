package gwu.softwaredesign.twitteranalytics

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.jetbrains.anko.find

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var logInButton: Button
    private lateinit var signUpButton: Button

    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        logInButton = findViewById(R.id.buttonLogin)
        signUpButton = findViewById(R.id.buttonSignUp)

        logInButton.isEnabled = false
        signUpButton.isEnabled = false

        val sharedPreferences : SharedPreferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)



        val textWatcher : TextWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val inputtedEmail : String = emailEditText.text.toString()
                val inputtedPassword : String = passwordEditText.text.toString()
                val enableButton = inputtedEmail.isNotEmpty() && inputtedPassword.isNotEmpty()
                logInButton.isEnabled = enableButton
                signUpButton.isEnabled = enableButton
            }
        }
        emailEditText.addTextChangedListener(textWatcher)
        passwordEditText.addTextChangedListener(textWatcher)






        logInButton.setOnClickListener{ /*view ->*/

            val inputtedEmail: String = emailEditText.text.toString()
            val inputtedPassword: String = passwordEditText.text.toString()

            firebaseAuth
                .signInWithEmailAndPassword(inputtedEmail, inputtedPassword)
                .addOnCompleteListener{task: Task<AuthResult> ->

                    if(task.isSuccessful){
                        val user: FirebaseUser = firebaseAuth.currentUser!!
                        val email = user.email
                        Toast.makeText(this, "Logged In as $email", Toast.LENGTH_LONG)
                            .show()

                        Log.d("LoginActivity", "login button called ")
                        sharedPreferences
                            .edit()
                            .putString("SAVED_EMAIL", email)
                            .apply()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                    else{
                        val exception = task.exception
                        Toast.makeText(this, "Failed To Log In $exception", Toast.LENGTH_LONG)
                            .show()
                    }
                }
        }





        signUpButton.setOnClickListener{
            val inputtedEmail: String = emailEditText.text.toString()
            val inputtedPassword: String = passwordEditText.text.toString()

            firebaseAuth
                .createUserWithEmailAndPassword(inputtedEmail, inputtedPassword)
                .addOnCompleteListener{ task: Task<AuthResult> ->

                    if(task.isSuccessful){
                        val user: FirebaseUser = firebaseAuth.currentUser!!
                        val email = user.email
                        Toast.makeText(this, "Signed Up as $email", Toast.LENGTH_LONG)
                            .show()

                        Log.d("LoginActivity", "signup button called ")
                        sharedPreferences
                            .edit()
                            .putString("SAVED_EMAIL", email)
                            .apply()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                    else {
                        val exception = task.exception
                        Toast.makeText(this, "Failed to sign up $exception", Toast.LENGTH_LONG)
                            .show()
                    }
             }

        }
    }
}