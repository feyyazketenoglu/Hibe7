package com.example.hibe7.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hibe7.data.model.Demand
import com.example.hibe7.ui.auth.HibeOrange // Eğer bu yoksa: val HibeOrange = Color(0xFFFF7043) ekle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Renkler
val TabSelectedColor = Color(0xFFFF7043)
val InfoBackgroundColor = Color(0xFFE0F2F1) // Mesaj kutusu için açık yeşil
val InfoTextColor = Color(0xFF00695C) // Mesaj yazısı için koyu yeşil

@Composable
fun DemandsScreen(
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Gelen Talepler", "Yaptığım Teklifler")

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val myUid = auth.currentUser?.uid

    var incomingDemands by remember { mutableStateOf<List<Demand>>(emptyList()) }
    var outgoingDemands by remember { mutableStateOf<List<Demand>>(emptyList()) }

    // Verileri Dinle
    LaunchedEffect(myUid) {
        if (myUid != null) {
            // Gelenler (Benim Ürünüm)
            firestore.collection("demands")
                .whereEqualTo("ownerId", myUid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        incomingDemands = snapshot.toObjects(Demand::class.java)
                            .sortedByDescending { it.timestamp }
                    }
                }

            // Gidenler (Benim İstediklerim)
            firestore.collection("demands")
                .whereEqualTo("requesterId", myUid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        outgoingDemands = snapshot.toObjects(Demand::class.java)
                            .sortedByDescending { it.timestamp }
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            // Üst Bar Tasarımı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White) // Veya HibeOrange yapabilirsin
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.Black)
                }
                Text("Tekliflerim", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color(0xFFFAF9F6))) {

            // --- SEKMELER (TABS) ---
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = TabSelectedColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TabSelectedColor
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) },
                        selectedContentColor = TabSelectedColor,
                        unselectedContentColor = Color.Gray
                    )
                }
            }

            // --- LİSTE ---
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val listToShow = if (selectedTab == 0) incomingDemands else outgoingDemands

                if (listToShow.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Bu kategoride henüz bir işlem yok.", color = Color.Gray)
                        }
                    }
                } else {
                    items(listToShow) { demand ->
                        DemandItemCard(
                            demand = demand,
                            isIncoming = (selectedTab == 0)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DemandItemCard(
    demand: Demand,
    isIncoming: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Ürün Adı
            Text(
                text = demand.productName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Kim İstedi / Kime İstedim?
            if (isIncoming) {
                Text("Talep Eden: ${demand.requesterName}", color = Color.DarkGray, fontSize = 14.sp)
            } else {
                Text("Talep Ettiğin Ürün", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BİLGİ KUTUSU (BUTONLAR YERİNE) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InfoBackgroundColor, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📢 Görüşme sohbet ekranında devam ediyor",
                    fontSize = 13.sp,
                    color = InfoTextColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}