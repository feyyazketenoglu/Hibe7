package com.example.hibe7.ui.share

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.hibe7.data.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

val HibeBeige = Color(0xFFFAF9F6)
val HibeOrange = Color(0xFFFF8A65) // Turuncu tonu
val HibeBrown = Color(0xFF5D4037)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen() {

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Kitap", "Kırtasiye", "Giyim", "Elektronik", "Diğer")
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    val usageOptions = listOf("Sıfır", "Kullanılmış")
    var selectedUsage by remember { mutableStateOf(usageOptions[1]) }

    val selectedImages = remember { mutableStateListOf<Uri?>(null, null, null) }
    var isLoading by remember { mutableStateOf(false) }
    var clickedBoxIndex by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val auth = FirebaseAuth.getInstance()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) selectedImages[clickedBoxIndex] = uri
    }

    // --- DÜZELTME: Metin Kutuları İçin Sabit Siyah Renk ---
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedTextColor = Color.Black,   // Yazı Siyah
        unfocusedTextColor = Color.Black  // Yazı Siyah
    )

    Scaffold(
        containerColor = HibeBeige,
        topBar = {
            TopAppBar(
                title = { Text("Ürün Bilgileri", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { /* Opsiyonel */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HibeBeige)
            )
        }
    ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            HorizontalDivider(thickness = 1.dp, color = Color.Black)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Fotoğraf & Video", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.SemiBold, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.5.dp, HibeBrown, shape = RoundedCornerShape(8.dp))
                                .clickable {
                                    clickedBoxIndex = index
                                    if (selectedImages[index] == null) photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    else showDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImages[index] != null) AsyncImage(model = selectedImages[index], contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            else Icon(Icons.Default.Add, contentDescription = "Ekle", tint = HibeBrown)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Lütfen en az 1 fotoğraf ekleyin", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                Text("Ürün Başlığı", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Medium, color = Color.Black)
                OutlinedTextField(
                    value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors // Renk Düzeltmesi
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Kategori", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Medium, color = Color.Black)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = Color.Black) },
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                        colors = textFieldColors // Renk Düzeltmesi
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { expanded = true })

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, color = Color.Black) },
                                onClick = { selectedCategory = category; expanded = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Kullanım Durumu", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Medium, color = Color.Black)
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    usageOptions.forEach { text ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedUsage = text }.padding(end = 16.dp)
                        ) {
                            RadioButton(
                                selected = (text == selectedUsage),
                                onClick = { selectedUsage = text },
                                colors = RadioButtonDefaults.colors(selectedColor = HibeOrange)
                            )
                            Text(text = text, fontSize = 16.sp, color = Color.Black)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Açıklama", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Medium, color = Color.Black)
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = textFieldColors // Renk Düzeltmesi
                )
                Spacer(modifier = Modifier.height(32.dp))

                // BUTON
                Button(
                    onClick = {
                        val currentUser = auth.currentUser
                        if (currentUser == null) {
                            Toast.makeText(context, "Lütfen giriş yapın!", Toast.LENGTH_SHORT).show()
                        } else if (title.isEmpty() || description.isEmpty()) {
                            Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                        } else {
                            isLoading = true
                            scope.launch {
                                val uploadedImageUrls = mutableListOf<String>()
                                val storageRef = Firebase.storage.reference
                                selectedImages.forEach { uri ->
                                    if (uri != null) {
                                        val fileName = "images/${UUID.randomUUID()}.jpg"
                                        val imageRef = storageRef.child(fileName)
                                        try {
                                            imageRef.putFile(uri).await()
                                            val downloadUrl = imageRef.downloadUrl.await()
                                            uploadedImageUrls.add(downloadUrl.toString())
                                        } catch (e: Exception) { }
                                    }
                                }

                                if (uploadedImageUrls.isNotEmpty()) {
                                    val product = Product(
                                        id = UUID.randomUUID().toString(),
                                        title = title,
                                        description = description,
                                        category = "$selectedCategory, $selectedUsage",
                                        imageUrl = uploadedImageUrls[0],
                                        images = uploadedImageUrls,
                                        ownerId = currentUser.uid,
                                        isAvailable = true,
                                        timestamp = System.currentTimeMillis()
                                    )

                                    Firebase.firestore.collection("products").add(product)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            Toast.makeText(context, "Ürün Hibe Edildi! 🎉", Toast.LENGTH_LONG).show()
                                            // Alanları temizle
                                            title = ""; description = ""; selectedImages.fill(null)
                                        }
                                        .addOnFailureListener {
                                            isLoading = false
                                            Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_LONG).show()
                                        }
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Lütfen en az bir fotoğraf seçin!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HibeOrange),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if(isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("HİBE ET", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {}
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Fotoğraf İşlemi") },
                confirmButton = {
                    Button(onClick = { showDialog = false; photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, colors = ButtonDefaults.buttonColors(containerColor = HibeOrange)) { Text("Değiştir", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { selectedImages[clickedBoxIndex] = null; showDialog = false }) { Text("Kaldır", color = Color.Red) }
                }
            )
        }
    }
}

suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    }
}