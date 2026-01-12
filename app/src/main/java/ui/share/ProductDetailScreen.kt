package com.example.hibe7.ui.share

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.hibe7.data.model.Favorite
import com.example.hibe7.data.model.Product
import com.example.hibe7.data.repository.DemandRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

val DemandGreen = Color(0xFF66BB6A)
val DialogOrange = Color(0xFFFF8A65)
val BackgroundBeige = Color(0xFFFAF9F6)

@Composable
fun ProductDetailScreen(
    product: Product,
    onBackClick: () -> Unit,
    onTalepEtClicked: () -> Unit
) {
    val context = LocalContext.current
    val demandRepository = remember { DemandRepository() }
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val myUid = auth.currentUser?.uid

    val scrollState = rememberScrollState()

    var showDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var ownerName by remember { mutableStateOf("Yükleniyor...") }
    var myName by remember { mutableStateOf("") }

    var isFavorite by remember { mutableStateOf(false) }
    var favoriteDocId by remember { mutableStateOf<String?>(null) }

    val isMyProduct = (myUid != null && myUid == product.ownerId)

    LaunchedEffect(product.ownerId, myUid) {
        if (product.ownerId.isNotEmpty()) {
            firestore.collection("users").document(product.ownerId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val nameFromDb = doc.getString("name")
                        val emailFromDb = doc.getString("email") ?: ""
                        ownerName = if (!nameFromDb.isNullOrEmpty()) nameFromDb else "@${emailFromDb.split("@")[0]}"
                    } else {
                        ownerName = "Bilinmeyen Sahip"
                    }
                }
        }

        if (myUid != null) {
            firestore.collection("users").document(myUid).get()
                .addOnSuccessListener { doc ->
                    val nameFromDb = doc.getString("name")
                    val emailFromDb = doc.getString("email") ?: ""
                    myName = if (!nameFromDb.isNullOrEmpty()) nameFromDb else "@${emailFromDb.split("@")[0]}"
                }

            firestore.collection("favorites")
                .whereEqualTo("userId", myUid)
                .whereEqualTo("productId", product.id)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        isFavorite = true
                        favoriteDocId = snapshot.documents[0].id
                    } else {
                        isFavorite = false
                        favoriteDocId = null
                    }
                }
        }
    }

    Scaffold(
        containerColor = BackgroundBeige
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 80.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    val imagesToShow = if (product.images.isNotEmpty()) product.images else listOf(product.imageUrl)

                    LazyRow(modifier = Modifier.fillMaxSize()) {
                        items(imagesToShow) { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .fillMaxHeight()
                            )
                        }
                    }

                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart)
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.Black)
                    }

                    IconButton(
                        onClick = {
                            if (myUid != null) {
                                if (isFavorite && favoriteDocId != null) {
                                    firestore.collection("favorites").document(favoriteDocId!!).delete()
                                    Toast.makeText(context, "Favorilerden çıkarıldı", Toast.LENGTH_SHORT).show()
                                } else {
                                    val newFav = Favorite(
                                        id = UUID.randomUUID().toString(),
                                        productId = product.id,
                                        userId = myUid
                                    )
                                    firestore.collection("favorites").document(newFav.id).set(newFav)
                                    Toast.makeText(context, "Favorilere eklendi ❤️", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd)
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (isFavorite) Color.Red else Color.Black
                        )
                    }

                    if (imagesToShow.size > 1) {
                        Text(
                            text = "Fotoğrafları Kaydır ->",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .background(Color.Black.copy(0.5f), RoundedCornerShape(4.dp))
                                .padding(4.dp)
                        )
                    }
                }

                Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                    Text(text = product.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = product.description, fontSize = 16.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = product.category, fontSize = 14.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color.LightGray, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column { Text(text = ownerName, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                if (isMyProduct) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = false
                    ) {
                        Text(text = "Bu Senin Ürünün", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            isLoading = true
                            demandRepository.sendDemand(
                                productId = product.id,
                                ownerId = product.ownerId,
                                productName = product.title,
                                productImage = product.imageUrl,
                                requesterName = myName
                            ) { success, message ->
                                isLoading = false
                                if (success) {
                                    showDialog = true
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DemandGreen),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(text = "Talep Et", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    onBackClick()
                },
                containerColor = Color(0xFFFFF8F6),
                title = null,
                text = {
                    Text(
                        "Talep gönderildi.\nMesajlaşma ekranına yönlendiriliyorsunuz.",
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            onTalepEtClicked()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DialogOrange)
                    ) {
                        Text("Tamam", color = Color.White)
                    }
                }
            )
        }
    }
}