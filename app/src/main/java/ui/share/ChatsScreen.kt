package com.example.hibe7.ui.share

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.hibe7.ui.auth.HibeOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatChannelItem(
    val id: String,
    val otherUserId: String,
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBackClick: () -> Unit,
    onChatClick: (String) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val myUid = auth.currentUser?.uid

    val BackgroundColor = Color(0xFFFDFBF7)

    var channels by remember { mutableStateOf<List<ChatChannelItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(myUid) {
        if (myUid != null) {
            firestore.collection("chat_channels")
                .whereArrayContains("userIds", myUid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val list = snapshot.documents.map { doc ->
                            val userIds = doc.get("userIds") as? List<String> ?: emptyList()
                            val otherId = userIds.find { it != myUid } ?: ""
                            val lastMsg = doc.getString("lastMessage") ?: ""
                            val lastTime = doc.getLong("lastMessageTimestamp") ?: 0L

                            ChatChannelItem(
                                id = doc.id,
                                otherUserId = otherId,
                                lastMessage = lastMsg,
                                lastMessageTimestamp = lastTime
                            )
                        }
                        channels = list.sortedByDescending { it.lastMessageTimestamp }
                        isLoading = false
                    }
                }
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Konuşmalarım", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HibeOrange
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (channels.isEmpty()) {
                Text(
                    text = "Henüz bir mesajlaşma yok.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(channels) { channel ->
                        ChatListItem(
                            channel = channel,
                            onClick = { onChatClick(channel.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    channel: ChatChannelItem,
    onClick: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    var otherUserName by remember { mutableStateOf("Yükleniyor...") }

    LaunchedEffect(channel.otherUserId) {
        if (channel.otherUserId.isNotEmpty() && channel.otherUserId != "Bilinmeyen") {
            firestore.collection("users").document(channel.otherUserId).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name")
                    val email = doc.getString("email") ?: ""
                    otherUserName = if (!name.isNullOrEmpty()) name else "@${email.split("@")[0]}"
                }
        } else {
            otherUserName = "Bilinmeyen Kullanıcı"
        }
    }

    val timeString = if (channel.lastMessageTimestamp > 0) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(channel.lastMessageTimestamp))
    } else {
        ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.LightGray,
                modifier = Modifier.size(50.dp),
                contentColor = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = otherUserName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = otherUserName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black, // <-- YAZI RENGİ SİYAH
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (timeString.isNotEmpty()) {
                        Text(
                            text = timeString,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (channel.lastMessage.isNotEmpty()) channel.lastMessage else "Mesaj yok",
                    color = if (channel.lastMessage.isNotEmpty()) Color.DarkGray else Color.LightGray,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}