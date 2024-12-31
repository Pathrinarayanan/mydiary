package com.example.diary

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.diary.ui.theme.DiaryTheme
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.delay
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.Executor
import java.util.regex.Pattern


class MainActivity : FragmentActivity() {
    private lateinit var mAuth: FirebaseAuth;
    private lateinit var db : FirebaseFirestore
    private lateinit var sp : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor


    // ...
// Initialize Firebase Auth
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance()
        sp = this.getSharedPreferences("mySharedPref", MODE_PRIVATE)
        editor = sp.edit()
        enableEdgeToEdge()

        setContent {
            window.statusBarColor = resources.getColor(R.color.white)
            val navcontroller = rememberNavController()
            var biometricAuthenticated by remember { mutableStateOf(true) }

            DiaryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navcontroller,
                        startDestination =  "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("signup") {
                            SignupScreen(navcontroller)
                            { username, email, password, confirmPass->
                                signUp(username,email,password,  confirmPass, navcontroller)
                            }
                        }
                        composable("itempage/{title}/{message}/{date}"){
                            val title = it.arguments?.get("title").toString()
                            val message = it.arguments?.get("message").toString()
                            val date = it.arguments?.get("date").toString()

                            ItemViewPage(URLDecoder.decode( title,"UTF-8") ,URLDecoder.decode( message, "UTF-8"),
                                URLDecoder.decode( date, "UTF-8"),
                                navcontroller
                            )
                        }
                        composable("login") {
                            val email = sp.getString("email", null)
                            val password = sp.getString("password",null)

                            if(email != null && password != null){
                                    // Ensure context and activity are non-null
                                val biometricManager = BiometricManager.from(this@MainActivity)
                                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                    .setTitle("continue ${email}")
                                    .setSubtitle("Use your fingerprint to proceed")
                                    .setNegativeButtonText("Cancel")
                                    .build()

                                val executor = ContextCompat.getMainExecutor(this@MainActivity)

                                val biometricPrompt = BiometricPrompt(
                                    this@MainActivity as FragmentActivity,
                                    executor,
                                    object : BiometricPrompt.AuthenticationCallback() {

                                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                            if (result != null) {
                                                super.onAuthenticationSucceeded(result)
                                            }
                                            // Handle successful authentication here
                                            loginAccount(email, password,navcontroller)
                                            Toast.makeText(this@MainActivity, "Authentication succeeded", Toast.LENGTH_SHORT).show()
                                        }

                                        override fun onAuthenticationFailed() {
                                            super.onAuthenticationFailed()
                                            // Handle failed authentication here
                                            Toast.makeText(this@MainActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                                when (biometricManager.canAuthenticate()) {
                                    BiometricManager.BIOMETRIC_SUCCESS -> {
                                        // Biometrics are available and enrolled
                                        if(biometricAuthenticated){
                                            biometricPrompt.authenticate(promptInfo)
                                            biometricAuthenticated = false
                                        }
                                    }
                                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                                        Toast.makeText(this@MainActivity, "No biometric hardware found", Toast.LENGTH_SHORT).show()
                                    }
                                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                                        Toast.makeText(this@MainActivity, "Biometric hardware is unavailable", Toast.LENGTH_SHORT).show()
                                    }
                                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                                        Toast.makeText(this@MainActivity, "No biometrics enrolled", Toast.LENGTH_SHORT).show()
                                    }
                                }


                            }


                                LoginScreen(navcontroller) { email, pass ->
                                    loginAccount(email, pass, navcontroller)
                                }

                        }
                        composable("home") {
                            var data by remember { mutableStateOf<QuerySnapshot?>(null) }
                            var name by remember{ mutableStateOf<DocumentSnapshot?>(null) }
                            mAuth.currentUser?.uid?.let {
                                db.collection("Users")
                                    .document(it).collection("myDiary").get()
                                    .addOnCompleteListener(OnCompleteListener {
                                        if(it.isSuccessful){
                                             data = it.result
                                        }
                                    })

                            }
                            mAuth.currentUser?.uid?.let {
                                db.collection("Users")
                                    .document(it).collection("details").document("name").get()
                                    .addOnCompleteListener(OnCompleteListener {
                                        if(it.isSuccessful){
                                             name = it.result
                                        }
                                    })
                            }

                            HomeScreen(navcontroller, data, name,
                                onAddlick = { title, msg, date->
                                    saveData(title, msg, date, navcontroller)
                                },
                                signout = {
                                    mAuth.signOut()
                                    navcontroller.navigate("login")
                                },
                                onDeleteClick = {
                                    deleteAccount(navcontroller)
                                },
                                onLogoutClick = {
                                    mAuth.signOut()
                                    navcontroller.navigate("login")
                                },
                                updatePassword = {
                                    updatePassword(mail = it)
                                },
                                updateUsername = {
                                    updateUsername(username = it,navcontroller)
                                },
                                onDeleteItemClick = {
                                    deleteItem(it,navcontroller)
                                }

                            )

                        }
                        composable("add/{documentid}") {


                           val id : String = it.arguments?.get("documentid").toString()

                            var data by remember{ mutableStateOf<DocumentSnapshot?>(null) }

                            mAuth.currentUser?.uid?.let { userid ->
                                db.collection("Users").document(userid)
                                    .collection("myDiary").document(id).get().addOnCompleteListener(
                                        OnCompleteListener {
                                            if(it.isSuccessful){
                                               data = it.result
                                                Log.d("pathris", data.toString())

                                            }
                                            else{

                                            }
                                        })
                            }
                            if(id!=null && data !=null){ // move to update screen
                                AddScreen(data,navcontroller, isEdit= true,
                                    updateData = {title , msg , time ->
                                        updateData(id, title, msg, time,navcontroller)
                                    },
                                    addData = {
                                              title , msg , time ->
                                        saveData(title, msg, time,navcontroller)

                                    }
                                )
                            }
                            else if( id ==null){
                                AddScreen(data, navcontroller, isEdit = false,
                                    updateData = { title , msg , time ->
                                        updateData(id, title, msg, time,navcontroller)

                                    },
                                    addData = {
                                            title , msg , time ->
                                        saveData(title, msg, time,navcontroller)

                                    }
                                )
                            }

                        }
                    }
                }
            }
        }

        }
    private fun deleteItem(id :String, navcontroller: NavHostController){
        mAuth.currentUser?.let {
            db.collection("Users").document(it.uid)
                .collection("myDiary").document(id)
                .delete().addOnCompleteListener(
                    OnCompleteListener {
                        if(it.isSuccessful) {
                            Toast.makeText(
                                applicationContext,
                                "Deleted successfully",
                                Toast.LENGTH_LONG
                            ).show()
                            navcontroller.navigate("home")
                        }
                        else{
                            Toast.makeText(applicationContext, "${it.exception}", Toast.LENGTH_LONG).show()

                        }
                    }
                )
        }
    }
    private fun updateData(id : String, title :String, message: String, date : Long, navcontroller: NavHostController) {
        val mydata = hashMapOf(
            "title" to title,
            "message" to message,
            "date" to date
        )
        Log.d("pathris", "updateData: ${id}")
        mAuth?.currentUser?.uid?.let { uid ->
            val mycollection = db.collection("Users").document(uid)
            mycollection.collection("myDiary").document(id).set(mydata)
                .addOnCompleteListener(this, OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "successfully added", Toast.LENGTH_SHORT)
                            .show()
                        navcontroller.navigate("home")
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "failure ${task.exception}",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                })
        }
    }

    private fun updatePassword(mail : String){
                    mAuth.currentUser?.updatePassword(mail)?.addOnCompleteListener(OnCompleteListener {task->
                        if(task.isSuccessful){
                            Toast.makeText(applicationContext, "Password updated successfully", Toast.LENGTH_LONG).show()

                        }
                        else{
                            Toast.makeText(applicationContext, "${task.exception}", Toast.LENGTH_LONG).show()

                        }
                    })

    }

    private fun updateUsername(username: String, navcontroller: NavHostController ){
        val data = hashMapOf(
            "name" to username
        )

        mAuth.currentUser?.let {
            db.collection("Users").document(it.uid).collection("details").document("name").set(data).addOnCompleteListener(
                OnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(applicationContext,"successfully updated",  Toast.LENGTH_SHORT).show()
                        navcontroller.navigate("home")
                    }
                    else{
                        Toast.makeText(applicationContext,"fails",  Toast.LENGTH_SHORT).show()

                    }
                }
            )
        }
    }

    private fun saveData(title: String, message : String, date : Long,navcontroller: NavHostController) {
        Log.d("pathris, ","  ${title} ${message} ${date}")

        mAuth?.currentUser?.uid?.let {uid ->
            val mycollection = db.collection("Users").document(uid)
            mycollection.collection("myDiary") .add(
                hashMapOf(
                "title" to title,
                "message" to message,
                    "date" to date
            )
            ).addOnCompleteListener(this, OnCompleteListener {task ->
                if(task.isSuccessful){
                    Toast.makeText(applicationContext, "successfully added", Toast.LENGTH_SHORT).show()
                    navcontroller.navigate("home")
                }
                else{
                    Toast.makeText(applicationContext, "failure ${task.exception}", Toast.LENGTH_SHORT).show()

                }
            })
        }
    }

    fun isValidPassword(password: String ) : Boolean{
        return (password.length >=8)
    }

    fun isValidMail(email: String) :Boolean{
        val p :Pattern  = Patterns.EMAIL_ADDRESS
        return p.matcher(email).matches()
    }
    fun deleteAccount(navcontroller: NavHostController){
        mAuth.currentUser?.delete()?.addOnCompleteListener(
            OnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(applicationContext, "Successfully deleted", Toast.LENGTH_LONG).show()
                    navcontroller.navigate("login")
                }
                else{
                    Toast.makeText(applicationContext, "fail ${it.exception}", Toast.LENGTH_LONG).show()

                }
            }
        )

    }
    fun signUp(username:String,email: String, password: String,confirmPass: String, navcontroller: NavHostController) {
        if(username == ""){
            Toast.makeText(applicationContext, "username must not be empty", Toast.LENGTH_LONG).show()

        }
        else if(!isValidMail(email)){
            Toast.makeText(applicationContext, "enter valid mail", Toast.LENGTH_LONG).show()

        }
        else if(!isValidPassword(password)){
            Toast.makeText(applicationContext, "Password must be atleast 8 characters", Toast.LENGTH_LONG).show()
        }
        else if(password!= confirmPass){
            Toast.makeText(applicationContext, "Both does not match", Toast.LENGTH_LONG).show()

        }
        else {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "success", Toast.LENGTH_LONG).show()
                        val userdate  = hashMapOf(
                            "name" to username
                        )
                        mAuth.currentUser?.uid?.let {
                            db.collection("Users")
                                .document(it).collection("details"). document("name")
                                .set(userdate)
                                .addOnCompleteListener(OnCompleteListener {
                                    if(it.isSuccessful){
                                    }
                                    else{
                                        Toast.makeText(applicationContext, "fail ${it.exception}", Toast.LENGTH_LONG)  .show()
                                    }
                                })

                        }
                        navcontroller.navigate("home")
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "failure ${task.exception}",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                })


        }
    }

    fun loginAccount(email: String, password: String, navcontroller: NavHostController) {
        if(!isValidMail(email)){
            Toast.makeText(applicationContext, "enter valid mail", Toast.LENGTH_LONG).show()

        }
        else if(!isValidPassword(password)){
            Toast.makeText(applicationContext, "Password must be atleast 8 characters", Toast.LENGTH_LONG).show()

        }
        else {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "success", Toast.LENGTH_LONG).show()

                        //store to sharedpref
                        editor.putString("email", email)
                        editor.putString("password", password)
                        editor.apply()
                        navcontroller.navigate("home")

                    } else {
                        Toast.makeText(
                            applicationContext,
                            "failure ${task.exception}",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                })
        }
    }
}