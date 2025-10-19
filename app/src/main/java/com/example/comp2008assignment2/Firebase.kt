package com.example.comp2008assignment2

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseService {
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun favouritesRef() =
        db.collection("users")
            .document(auth.currentUser?.uid ?: "anonymous")
            .collection("favourites")

    fun libraryRef() =
        db.collection("users")
            .document(auth.currentUser?.uid ?: "anonymous")
            .collection("library")
}
