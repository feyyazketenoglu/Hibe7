package com.example.hibe7.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hibe7.data.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun FavoritesScreen(
    onProductClick: (Product) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val myUid = auth.currentUser?.uid

    var favoriteProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(myUid) {
        if (myUid != null) {
            firestore.collection("favorites")
                .whereEqualTo("userId", myUid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val productIds = snapshot.documents.mapNotNull { it.getString("productId") }

                        if (productIds.isEmpty()) {
                            favoriteProducts = emptyList()
                            isLoading = false
                        } else {
                            val tempProducts = mutableListOf<Product>()
                            var fetchCount = 0

                            for (pid in productIds) {
                                firestore.collection("products")
                                    .whereEqualTo("id", pid)
                                    .get()
                                    .addOnSuccessListener { querySnapshot ->
                                        if (!querySnapshot.isEmpty) {
                                            val product = querySnapshot.documents[0].toObject(Product::class.java)
                                            if (product != null) {
                                                tempProducts.add(product)
                                            }
                                        }
                                        fetchCount++
                                        if (fetchCount == productIds.size) {
                                            favoriteProducts = tempProducts
                                            isLoading = false
                                        }
                                    }
                                    .addOnFailureListener {
                                        fetchCount++
                                        if (fetchCount == productIds.size) {
                                            favoriteProducts = tempProducts
                                            isLoading = false
                                        }
                                    }
                            }
                        }
                    } else {
                        isLoading = false
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFBF7))
            .padding(16.dp)
    ) {
        Text(
            text = "Favorilerim",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Black)
            }
        } else if (favoriteProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Henüz favorin yok.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(favoriteProducts) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product) }
                    )
                }
            }
        }
    }
}