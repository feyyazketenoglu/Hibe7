package com.example.hibe7.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.hibe7.data.model.Product
import com.example.hibe7.ui.auth.HibeOrange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

val BackgroundColor = Color(0xFFFDFBF7)
val SearchBarColor = Color(0xFFF5F5F5) // Hafif gri arka plan
val TitleColor = Color(0xFF1A1C29)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onProductClick: (Product) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    var productList by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }


    var searchText by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()


    val categories = listOf("Tümü", "Kitap", "Kırtasiye", "Giyim", "Elektronik", "Diğer")
    val usages = listOf("Farketmez", "Sıfır", "Kullanılmış")


    var selectedCategory by remember { mutableStateOf("Tümü") }
    var selectedUsage by remember { mutableStateOf("Farketmez") }


    LaunchedEffect(Unit) {
        firestore.collection("products")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    productList = snapshot.toObjects(Product::class.java)
                        .filter { it.isAvailable }
                    isLoading = false
                }
            }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 10.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    TextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        placeholder = { Text("", fontSize = 14.sp) },

                        leadingIcon = {
                            IconButton(onClick = { showFilterSheet = true }) {
                                Icon(Icons.Default.Menu, contentDescription = "Filtre", tint = Color.DarkGray)
                            }
                        },
                        trailingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Ara", tint = Color.DarkGray)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SearchBarColor,
                            unfocusedContainerColor = SearchBarColor,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = HibeOrange,
                            // --- DÜZELTME: Yazı Rengi Siyah ---
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        singleLine = true
                    )


                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Bildirimler",
                        tint = Color.Black,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))


                val catTitle = if (selectedCategory == "Tümü") "Ürün Kategorisi" else selectedCategory
                val usageTitle = if (selectedUsage != "Farketmez") "($selectedUsage)" else ""

                Text(
                    text = "$catTitle $usageTitle",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TitleColor
                )
            }
        }
    ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = HibeOrange)
                }
            } else {
                val filteredList = productList.filter { product ->
                    val matchesSearch = product.title.contains(searchText, ignoreCase = true) ||
                            product.description.contains(searchText, ignoreCase = true)

                    val matchesCategory = if (selectedCategory == "Tümü") true
                    else product.category.contains(selectedCategory, ignoreCase = true)

                    val matchesUsage = if (selectedUsage == "Farketmez") true
                    else product.category.contains(selectedUsage, ignoreCase = true)

                    matchesSearch && matchesCategory && matchesUsage
                }

                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kriterlere uygun ilan bulunamadı.", color = Color.Gray)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredList) { product ->
                            ProductCard(product = product, onClick = { onProductClick(product) })
                        }
                    }
                }
            }
        }


        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                var tempCategory by remember { mutableStateOf(selectedCategory) }
                var tempUsage by remember { mutableStateOf(selectedUsage) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Filtrele", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = HibeOrange)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Kategori", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    categories.forEach { cat ->
                        FilterOptionRow(
                            text = cat,
                            isSelected = tempCategory == cat,
                            onSelect = { tempCategory = cat }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Kullanım Durumu", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    usages.forEach { use ->
                        FilterOptionRow(
                            text = use,
                            isSelected = tempUsage == use,
                            onSelect = { tempUsage = use }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            selectedCategory = tempCategory
                            selectedUsage = tempUsage
                            showFilterSheet = false
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HibeOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sonuçları Göster", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}


@Composable
fun FilterOptionRow(text: String, isSelected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelect() },
            colors = RadioButtonDefaults.colors(selectedColor = HibeOrange)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Text(text = text, fontSize = 16.sp, color = Color.Black)
    }
}


@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(240.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFEEEEEE)).padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.category.split(",")[0],
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF81C784)).padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("HİBE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.White)) {
                val imageToShow = if (product.images.isNotEmpty()) product.images[0] else product.imageUrl
                AsyncImage(
                    model = imageToShow,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
            }
        }
    }
}