package com.example.hibe7.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.example.hibe7.data.model.Demand
import java.util.UUID

class DemandRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "Hibe7Demand"

    fun sendDemand(
        productId: String,
        ownerId: String,
        productName: String,
        productImage: String,
        requesterName: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val myUid = auth.currentUser?.uid

        if (myUid == null) {
            onResult(false, "Hata: Giriş yapmamışsınız.")
            return
        }

        firestore.collection("demands")
            .whereEqualTo("productId", productId)
            .whereEqualTo("requesterId", myUid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    Log.d(TAG, "Zaten talep var, yenisi oluşturulmadı.")
                    onResult(false, "Bu ürün için zaten bir talebiniz var. Sohbetlerden devam edebilirsiniz.")
                } else {
                    createNewDemand(myUid, productId, ownerId, productName, productImage, requesterName, onResult)
                }
            }
            .addOnFailureListener {
                onResult(false, "Kontrol hatası: ${it.message}")
            }
    }

    private fun createNewDemand(
        myUid: String,
        productId: String,
        ownerId: String,
        productName: String,
        productImage: String,
        requesterName: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val demandId = UUID.randomUUID().toString()
        val newDemand = Demand(
            id = demandId,
            productId = productId,
            requesterId = myUid,
            ownerId = ownerId,
            productName = productName,
            productImage = productImage,
            requesterName = requesterName,
            status = "Sohbet Başladı",
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("demands").document(demandId)
            .set(newDemand)
            .addOnSuccessListener {
                createOrUpdateChatChannel(myUid, ownerId, onResult)
            }
            .addOnFailureListener { e ->
                onResult(false, "Talep gönderilemedi: ${e.message}")
            }
    }

    private fun createOrUpdateChatChannel(
        myUid: String,
        otherUserId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val channelId = if (myUid < otherUserId) "${myUid}_$otherUserId" else "${otherUserId}_$myUid"

        val channelData = hashMapOf(
            "id" to channelId,
            "userIds" to listOf(myUid, otherUserId),
            "lastActivity" to System.currentTimeMillis()
        )

        firestore.collection("chat_channels").document(channelId)
            .set(channelData, SetOptions.merge())
            .addOnSuccessListener {
                onResult(true, "Talep alındı!")
            }
            .addOnFailureListener {
                onResult(true, "Talep gitti ama sohbet hatası.")
            }
    }
}