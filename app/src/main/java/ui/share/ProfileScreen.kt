package com.example.hibe7.ui.share

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onSettingsClick: () -> Unit,
    onDemandsClick: () -> Unit,
    onConversationsClick: () -> Unit,
    onMyProductsClick: () -> Unit,
    onReceivedClick: () -> Unit,
    onFeedbackClick: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var userName by remember { mutableStateOf("Yükleniyor...") }
    var profileImageUrl by remember { mutableStateOf("") }

    var givenCount by remember { mutableIntStateOf(0) }
    var receivedCount by remember { mutableIntStateOf(0) }

    val HibeGreen = Color(0xFF2E7D32)
    val HibeOrange = Color(0xFFFF7043)
    val BackgroundColor = Color(0xFFF9F5F2)

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            firestore.collection("users").document(uid).addSnapshotListener { document, _ ->
                if (document != null) {
                    val email = document.getString("email") ?: ""
                    val nameFromEmail = email.split("@")[0]
                    userName = if(document.getString("name") != null) document.getString("name")!! else "@$nameFromEmail"
                    profileImageUrl = document.getString("profileImageUrl") ?: ""
                }
            }

            firestore.collection("demands")
                .whereEqualTo("ownerId", uid)
                .whereEqualTo("status", "completed")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) givenCount = snapshot.size()
                }

            firestore.collection("demands")
                .whereEqualTo("requesterId", uid)
                .whereEqualTo("status", "completed")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) receivedCount = snapshot.size()
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(45.dp).clip(CircleShape).border(1.dp, Color.Gray, CircleShape), contentAlignment = Alignment.Center) {
                if (profileImageUrl.isNotEmpty()) Image(painter = rememberAsyncImagePainter(profileImageUrl), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                else Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.padding(4.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(text = userName, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black)

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onSettingsClick) { Icon(Icons.Outlined.Settings, "Ayarlar", tint = Color.Black) }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$givenCount",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = HibeOrange
                )
                Text(text = "Verilen Hibe", fontSize = 14.sp, color = Color.Gray)
            }

            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .background(Color.LightGray)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$receivedCount",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = HibeOrange
                )
                Text(text = "Alınan Hibe", fontSize = 14.sp, color = Color.Gray)
            }
        }

        Divider(color = Color.Black, thickness = 1.dp)

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatCircleItem(label = "Aldıklarım", color = HibeGreen, onClick = onReceivedClick) {
                Canvas(modifier = Modifier.fillMaxSize()) { drawCircle(color = HibeGreen, style = Stroke(width = 8f)) }
            }

            StatCircleItem(label = "Hibelerim", color = HibeGreen, onClick = onMyProductsClick) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = HibeGreen, style = Stroke(width = 8f))
                    drawCircle(color = HibeGreen, radius = size.minDimension / 4, style = Stroke(width = 8f))
                }
            }

            StatCircleItem(label = "Needed", color = HibeGreen, onClick = {}) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2.5f
                    drawCircle(color = HibeGreen, radius = radius, center = center.copy(x = center.x - 15f), style = Stroke(width = 6f))
                    drawCircle(color = HibeGreen, radius = radius, center = center.copy(x = center.x + 15f), style = Stroke(width = 6f))
                }
            }
        }
        Divider(color = Color.Black, thickness = 1.dp)

        // MENÜ
        Column(modifier = Modifier.fillMaxWidth()) {
            ProfileMenuItem(text = "Tekliflerim", onClick = onDemandsClick)
            ProfileMenuItem(text = "Geri Bildirim Ver", onClick = onFeedbackClick)
            ProfileMenuItem(text = "Konuşmalarım", onClick = onConversationsClick)
            ProfileMenuItem(text = "Çıkış Yap", onClick = onSignOut)
        }
    }
}

@Composable
fun StatCircleItem(label: String, color: Color, onClick: () -> Unit, iconContent: @Composable BoxScope.() -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(60.dp).clickable { onClick() }, contentAlignment = Alignment.Center, content = iconContent)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 14.sp, color = Color.Black)
    }
}

@Composable
fun ProfileMenuItem(text: String, onClick: () -> Unit = {}) {
    Column {
        Box(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 16.dp, horizontal = 24.dp)) { Text(text, fontSize = 16.sp, color = Color.Black) }
        Divider(color = Color.Black, thickness = 1.dp)
    }
}