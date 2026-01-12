package com.example.hibe7.ui.share

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.hibe7.ui.auth.HibeOrange
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var currentImageUrl by remember { mutableStateOf("") }
    var currentName by remember { mutableStateOf("") } // YENİ: İsim State'i
    var isLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Verileri Çek
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    currentImageUrl = document.getString("profileImageUrl") ?: ""
                    currentName = document.getString("name") ?: "" // İsmi çek
                }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Üst Kısım
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
            }
            Text("Profili Düzenle", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        // RESİM ALANI
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(painter = rememberAsyncImagePainter(imageUri), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else if (currentImageUrl.isNotEmpty()) {
                Image(painter = rememberAsyncImagePainter(currentImageUrl), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
            }
        }
        Text("Fotoğrafı değiştirmek için tıkla", color = Color.Gray, modifier = Modifier.padding(top = 8.dp), fontSize = 12.sp)

        Spacer(modifier = Modifier.height(30.dp))

        // YENİ: İSİM DEĞİŞTİRME ALANI
        OutlinedTextField(
            value = currentName,
            onValueChange = { currentName = it },
            label = { Text("Ad Soyad") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HibeOrange,
                focusedLabelColor = HibeOrange,
                cursorColor = HibeOrange
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // KAYDET BUTONU
        Button(
            onClick = {
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    isLoading = true

                    // Güncellenecek verileri hazırla
                    val updates = hashMapOf<String, Any>(
                        "name" to currentName
                    )

                    // 1. Durum: Yeni resim seçildiyse önce yükle, sonra güncelle
                    if (imageUri != null) {
                        val storageRef = storage.reference.child("profile_images/$uid/${UUID.randomUUID()}.jpg")
                        storageRef.putFile(imageUri!!)
                            .addOnSuccessListener {
                                storageRef.downloadUrl.addOnSuccessListener { uri ->
                                    updates["profileImageUrl"] = uri.toString()
                                    // Güncellemeyi yap
                                    updateUserFirestore(uid, updates, firestore, context, onBackClick) { isLoading = false }
                                }
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Resim yüklenemedi: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // 2. Durum: Sadece isim değiştiyse direkt güncelle
                        updateUserFirestore(uid, updates, firestore, context, onBackClick) { isLoading = false }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HibeOrange)
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            else Text("Kaydet", fontSize = 18.sp, color = Color.White)
        }
    }
}

// Yardımcı Fonksiyon: Firestore Güncelleme
fun updateUserFirestore(
    uid: String,
    updates: Map<String, Any>,
    firestore: FirebaseFirestore,
    context: android.content.Context,
    onSuccess: () -> Unit,
    onComplete: () -> Unit
) {
    firestore.collection("users").document(uid).update(updates)
        .addOnSuccessListener {
            onComplete()
            Toast.makeText(context, "Profil güncellendi!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener {
            onComplete()
            Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
        }
}