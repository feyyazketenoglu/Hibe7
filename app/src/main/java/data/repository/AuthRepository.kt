package com.example.hibe7.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.hibe7.data.model.AppUser

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()


    fun registerUser(
        email: String,
        pass: String,
        name: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        // 1. Firebase Auth ile kullanıcı oluştur
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    // 2. Modeli doldur
                    val newUser = AppUser(
                        uid = uid,
                        email = email,
                        name = name,
                        profileImageUrl = ""
                    )

                    // 3. Firestore'a kaydet
                    firestore.collection("users").document(uid)
                        .set(newUser)
                        .addOnSuccessListener {
                            onResult(true, "Kayıt Başarılı!")
                        }
                        .addOnFailureListener {
                            onResult(false, "Veritabanı hatası: ${it.message}")
                        }
                }
            }
            .addOnFailureListener {
                onResult(false, it.message)
            }
    }

    // GİRİŞ YAPMA
    fun loginUser(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                onResult(true, "Giriş Başarılı")
            }
            .addOnFailureListener {
                onResult(false, it.message)
            }
    }

    // ÇIKIŞ YAPMA
    fun signOut() {
        auth.signOut()
    }

    // MEVCUT KULLANICI VAR MI?
    fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }
}