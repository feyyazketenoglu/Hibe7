package com.example.hibe7.ui.share

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.hibe7.ui.auth.HibeOrange
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Mesaj Modeli
data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val timestamp: Long = 0,
    val type: String = "text"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    channelId: String,
    otherUserId: String,
    onBackClick: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val myUid = auth.currentUser?.uid
    val context = LocalContext.current

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }

    // UI Durumları
    var targetUserName by remember { mutableStateOf("Yükleniyor...") }
    var targetUserImage by remember { mutableStateOf("") }

    // Ürün Teslim Mantığı
    var activeDemandId by remember { mutableStateOf<String?>(null) }
    var productIdToGive by remember { mutableStateOf<String?>(null) }
    var isOwner by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // 1. Karşı Tarafın Bilgilerini ve Aktif Talebi Bul
    LaunchedEffect(channelId) {
        firestore.collection("chat_channels").document(channelId).get()
            .addOnSuccessListener { doc ->
                val userIds = doc.get("userIds") as? List<String> ?: emptyList()
                val foundId = userIds.find { it != myUid }

                if (foundId != null) {
                    firestore.collection("users").document(foundId).get()
                        .addOnSuccessListener { userDoc ->
                            targetUserName = userDoc.getString("name") ?: "Kullanıcı"
                            targetUserImage = userDoc.getString("profileImageUrl") ?: ""
                        }

                    if (myUid != null) {
                        firestore.collection("demands")
                            .whereEqualTo("ownerId", myUid)
                            .whereEqualTo("requesterId", foundId)
                            .get()
                            .addOnSuccessListener { demandSnapshot ->
                                if (!demandSnapshot.isEmpty) {
                                    val demandDoc = demandSnapshot.documents.firstOrNull {
                                        it.getString("status") != "completed"
                                    }
                                    if (demandDoc != null) {
                                        activeDemandId = demandDoc.id
                                        productIdToGive = demandDoc.getString("productId")
                                        isOwner = true
                                    }
                                }
                            }
                    }
                }
            }
    }

    // 2. Mesajları Dinle
    LaunchedEffect(channelId) {
        firestore.collection("chat_channels").document(channelId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages = snapshot.toObjects(Message::class.java)
                }
            }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.scrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp).border(1.dp, Color.LightGray, CircleShape),
                            color = Color.White
                        ) {
                            if (targetUserImage.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(targetUserImage),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = targetUserName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = HibeOrange)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = targetUserName, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Çevrimiçi", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(elevation = 1.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFFAF9F6))
        ) {
            // --- ÜRÜNÜ TESLİM ET BUTONU ---
            if (isOwner && activeDemandId != null) {
                Button(
                    onClick = {
                        // 1. Talebi tamamlandı yap
                        firestore.collection("demands").document(activeDemandId!!)
                            .update("status", "completed")

                        // 2. Ürünü pasif yap
                        if (productIdToGive != null) {
                            firestore.collection("products")
                                .whereEqualTo("id", productIdToGive)
                                .get()
                                .addOnSuccessListener { productSnapshot ->
                                    if (!productSnapshot.isEmpty) {
                                        val realDoc = productSnapshot.documents[0]
                                        realDoc.reference.update("available", false)
                                    } else {
                                        firestore.collection("products").document(productIdToGive!!)
                                            .update("available", false)
                                    }
                                }
                        }

                        // 3. Sistem mesajı gönder
                        val sysMsg = hashMapOf(
                            "text" to "✅ Ürün teslim edildi! İşlem tamamlandı.",
                            "senderId" to "SYSTEM",
                            "timestamp" to System.currentTimeMillis(),
                            "type" to "system"
                        )
                        firestore.collection("chat_channels").document(channelId).collection("messages").add(sysMsg)

                        isOwner = false
                        Toast.makeText(context, "Teslim edildi!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HibeOrange),
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("✅ Bu Ürünü Teslim Et")
                }
            }

            // Mesaj Listesi
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { message ->
                    val isMe = message.senderId == myUid
                    ChatBubbleDesign(message = message, isMe = isMe)
                }
            }

            // Mesaj Yazma Alanı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Mesaj yaz...", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HibeOrange,
                        unfocusedBorderColor = HibeOrange.copy(alpha = 0.5f),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = HibeOrange,
                        // --- DÜZELTME: Yazı rengini siyaha sabitledik ---
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (messageText.isNotBlank() && myUid != null) {
                            val timestamp = System.currentTimeMillis()
                            val textToSend = messageText
                            val newMessage = hashMapOf(
                                "text" to textToSend,
                                "senderId" to myUid,
                                "timestamp" to timestamp,
                                "type" to "text"
                            )

                            firestore.collection("chat_channels").document(channelId)
                                .collection("messages").add(newMessage)

                            val updates = hashMapOf<String, Any>(
                                "lastActivity" to timestamp,
                                "lastMessage" to textToSend,
                                "lastMessageTimestamp" to timestamp
                            )
                            firestore.collection("chat_channels").document(channelId).update(updates)

                            messageText = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Gönder",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubbleDesign(message: Message, isMe: Boolean) {
    if (message.type == "system") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50))
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    } else {
        val alignment = if (isMe) Alignment.End else Alignment.Start
        val borderColor = if (isMe) HibeOrange else Color.LightGray
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = sdf.format(Date(message.timestamp))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(
                    topStart = 12.dp, topEnd = 12.dp,
                    bottomStart = if (isMe) 12.dp else 0.dp,
                    bottomEnd = if (isMe) 0.dp else 12.dp
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp).widthIn(max = 280.dp)) {
                    Text(text = message.text, color = Color.Black, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = time, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.align(Alignment.End))
                }
            }
        }
    }
}

fun Modifier.shadow(elevation: androidx.compose.ui.unit.Dp) = this.then(
    Modifier.drawBehind {
        drawRect(
            color = Color.Black.copy(alpha = 0.1f),
            size = size.copy(height = size.height + elevation.toPx())
        )
    }
)