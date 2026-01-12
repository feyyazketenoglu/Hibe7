package com.example.hibe7.ui.share

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hibe7.ui.auth.HibeOrange
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onProfileSettingsClick: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    var currentView by remember { mutableStateOf("menu") }

    if (currentView == "password_change") {
        PasswordChangeDesign(onBack = { currentView = "menu" })
    } else {
        SettingsMenuContent(
            onBackClick = onBackClick,
            onProfileSettingsClick = onProfileSettingsClick,
            onPasswordChangeClick = { currentView = "password_change" },
            onAccountDeleted = onAccountDeleted
        )
    }
}

// --- 1. AYARLAR MENÜSÜ ---
@Composable
fun SettingsMenuContent(
    onBackClick: () -> Unit,
    onProfileSettingsClick: () -> Unit,
    onPasswordChangeClick: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val menuItems = listOf(
        "Profil Ayarları",
        "Şifremi Değiştir",
        "Hibeleş Hakkında",
        "Bildirim Ayarları",
        "Hesabımı Kapat"
    )

    // ANA KOLON ARKA PLANI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFBF7)) // <-- ARKA PLAN DÜZELTİLDİ
            .padding(16.dp)
    ) {
        // Üst Bar
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.Black) }
            Text(text = "Ayarlar", fontSize = 22.sp, modifier = Modifier.padding(start = 8.dp), color = Color.Black)
        }

        HorizontalDivider(color = Color.Black, thickness = 1.dp)

        menuItems.forEach { item ->
            val textColor = if (item == "Hesabımı Kapat") Color.Red else Color.Black
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        when (item) {
                            "Profil Ayarları" -> onProfileSettingsClick()
                            "Şifremi Değiştir" -> onPasswordChangeClick()
                            "Hesabımı Kapat" -> showDeleteAccountDialog = true
                            "Hibeleş Hakkında" -> Toast.makeText(context, "Hibe7 v1.0", Toast.LENGTH_SHORT).show()
                            "Bildirim Ayarları" -> Toast.makeText(context, "Yakında...", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = item, fontSize = 16.sp, color = textColor)
            }
            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
        }
    }

    // HESAP SİLME DİYALOĞU
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Hesabı Sil", color = Color.Red) },
            text = { Text("Hesabınızı kalıcı olarak silmek istediğinize emin misiniz?", color = Color.Black) },
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        val uid = user?.uid
                        if (uid != null) {
                            firestore.collection("users").document(uid).delete()
                                .addOnSuccessListener {
                                    user.delete().addOnSuccessListener { onAccountDeleted() }
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Evet, Sil") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Vazgeç") }
            }
        )
    }
}

// --- 2. YENİ ŞİFRE DEĞİŞTİRME TASARIMI ---
@Composable
fun PasswordChangeDesign(onBack: () -> Unit) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser

    val CreamBackground = Color(0xFFFAF9F6)
    val InputBorderColor = HibeOrange

    // Yazı renkleri için ayar
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = InputBorderColor,
        unfocusedBorderColor = InputBorderColor,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        cursorColor = HibeOrange,
        focusedTextColor = Color.Black,   // <-- YAZI SİYAH
        unfocusedTextColor = Color.Black  // <-- YAZI SİYAH
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        // Üst Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.Black)
            }
            Text(
                text = "Şifreyi Değiştir",
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider(color = Color.Black, thickness = 1.dp)

        Column(modifier = Modifier.padding(24.dp)) {

            Text("Yeni Şifre", fontSize = 16.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = textFieldColors, // <-- Renkler atandı
                shape = RoundedCornerShape(4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Yeni Şifreyi Tekrar Giriniz", fontSize = 16.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = textFieldColors, // <-- Renkler atandı
                shape = RoundedCornerShape(4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = {
                        if (newPassword.isNotEmpty() && newPassword == confirmPassword) {
                            if (newPassword.length >= 6) {
                                user?.updatePassword(newPassword)
                                    ?.addOnSuccessListener { showSuccessDialog = true }
                                    ?.addOnFailureListener { Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_SHORT).show() }
                            } else {
                                Toast.makeText(context, "Şifre en az 6 karakter olmalı", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Şifreler eşleşmiyor!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HibeOrange),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.width(160.dp).height(48.dp)
                ) {
                    Text("Değiştir", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }

    if (showSuccessDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.width(300.dp),
                shape = RoundedCornerShape(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC0C0C0))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Şifreniz değiştirildi.", fontSize = 18.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showSuccessDialog = false; onBack() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA0522D)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text("Tamam", color = Color.White)
                    }
                }
            }
        }
    }
}