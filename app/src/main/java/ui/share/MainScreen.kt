package com.example.hibe7.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.hibe7.data.model.Product
import com.example.hibe7.ui.auth.HibeOrange

@Composable
fun MainScreen(
    onSignOutRequest: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    var currentScreen by remember { mutableStateOf("home") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var selectedChannelId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            BottomAppBar(containerColor = HibeOrange, contentColor = Color.White) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(onClick = { currentScreen = "home"; selectedProduct = null; selectedChannelId = null }) { Icon(Icons.Outlined.Home, "Home") }
                    IconButton(onClick = { currentScreen = "favorites"; selectedProduct = null; selectedChannelId = null }) { Icon(Icons.Outlined.FavoriteBorder, "Fav") }
                    IconButton(onClick = { currentScreen = "add"; selectedProduct = null; selectedChannelId = null }) { Icon(Icons.Default.Add, "Ekle") }
                    IconButton(onClick = { currentScreen = "cart"; selectedProduct = null; selectedChannelId = null }) { Icon(Icons.Outlined.ShoppingCart, "Sepet") }
                    IconButton(onClick = { currentScreen = "profile"; selectedProduct = null; selectedChannelId = null }) { Icon(Icons.Outlined.Person, "Profil") }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (selectedChannelId != null) {
                ChatDetailScreen(
                    channelId = selectedChannelId!!,
                    otherUserId = "",
                    onBackClick = { selectedChannelId = null }
                )
            } else if (selectedProduct != null) {
                ProductDetailScreen(
                    product = selectedProduct!!,
                    onBackClick = { selectedProduct = null },
                    onTalepEtClicked = { onNavigateToChat() }
                )
            } else {
                when (currentScreen) {
                    "home" -> HomeScreen(onProductClick = { selectedProduct = it })
                    "add" -> AddProductScreen()
                    "cart" -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFDFBF7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sepet Yakında...", color = Color.Black)
                    }

                    "profile" -> ProfileScreen(
                        onSignOut = onSignOutRequest,
                        onSettingsClick = { currentScreen = "settings" },
                        onDemandsClick = { currentScreen = "demands" },
                        onConversationsClick = { currentScreen = "chat_list" },
                        onMyProductsClick = { currentScreen = "my_products" },
                        onReceivedClick = { currentScreen = "received_products" },
                        onFeedbackClick = { currentScreen = "feedback" }
                    )

                    "settings" -> SettingsScreen(
                        onBackClick = { currentScreen = "profile" },
                        onProfileSettingsClick = { currentScreen = "profile_edit" },
                        onAccountDeleted = onSignOutRequest
                    )

                    "profile_edit" -> EditProfileScreen(onBackClick = { currentScreen = "settings" })
                    "demands" -> DemandsScreen(onBackClick = { currentScreen = "profile" })
                    "favorites" -> FavoritesScreen(onProductClick = { product -> selectedProduct = product })

                    "chat_list" -> ChatScreen(
                        onBackClick = { currentScreen = "profile" },
                        onChatClick = { channelId -> selectedChannelId = channelId }
                    )

                    "my_products" -> MyProductsScreen(onBackClick = { currentScreen = "profile" })
                    "received_products" -> ReceivedProductsScreen(onBackClick = { currentScreen = "profile" })
                    "feedback" -> FeedbackScreen(onBackClick = { currentScreen = "profile" })
                }
            }
        }
    }
}