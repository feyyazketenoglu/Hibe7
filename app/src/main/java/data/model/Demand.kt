package com.example.hibe7.data.model

data class Demand(
    val id: String = "",             // Talebin benzersiz kimliği
    val productId: String = "",      // Hangi ürün isteniyor?
    val requesterId: String = "",    // İsteyen kişi (Sen)
    val ownerId: String = "",        // Ürünün sahibi (Karşı taraf)
    val productName: String = "",    // Ürünün adı (Listede kolay göstermek için)
    val productImage: String = "",   // Ürünün resmi
    val requesterName: String = "",  // İsteyen kişinin adı
    val status: String = "pending",  // Durum: pending (bekliyor), accepted (kabul), rejected (red)
    val timestamp: Long = System.currentTimeMillis() // Ne zaman istendi?
)