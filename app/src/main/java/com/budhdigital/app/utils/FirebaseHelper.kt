package com.budhdigital.app.utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
object FirebaseHelper {
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }
    val usersRef: DatabaseReference by lazy {
        database.reference.child("users")
    }
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    fun getCurrentUserRef(): DatabaseReference? {
        return getCurrentUserId()?.let { uid ->
            usersRef.child(uid)
        }
    }
    fun signOut() {
        auth.signOut()
    }
}
