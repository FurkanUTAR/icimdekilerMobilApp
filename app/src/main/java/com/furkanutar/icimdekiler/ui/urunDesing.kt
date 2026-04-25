package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkanutar.icimdekiler.R
import coil.compose.AsyncImage
import com.furkanutar.icimdekiler.ui.theme.EmeraldGreen
import com.furkanutar.icimdekiler.ui.theme.LightBackground

@Composable
fun UrunScreen(
    urunAdi: String,
    gorselUrl: String,
    kalori: Int, // Yeni: Ürünün 100g kalori değeri
    protein: Float,
    karbonhidrat: Float,
    yag: Float,
    icindekilerListesi: List<String>,
    onIngredientClick: (String) -> Unit,
    onEkleClick: (Int) -> Unit // Hedefe ekleme tetikleyicisi
) {
    var miktar by remember { mutableStateOf("100") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()) // Tüm ekranın kaydırılabilir olması daha iyi
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Üst Kısım: Görsel ve İsim (Mevcut yapın)
        Surface(
            modifier = Modifier.size(140.dp),
            shape = CircleShape,
            border = BorderStroke(3.dp, EmeraldGreen),
            shadowElevation = 8.dp
        ) {
            AsyncImage(
                model = gorselUrl,
                contentDescription = urunAdi,
                contentScale = ContentScale.Crop,
                modifier = Modifier.clip(CircleShape),
                error = painterResource(id = R.drawable.insert_photo)
            )
        }

        Text(
            text = urunAdi,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

//        // --- YENİ: BESİN DEĞERLERİ ÖZET KARTI ---
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 16.dp),
//            shape = RoundedCornerShape(16.dp),
//            colors = CardDefaults.cardColors(containerColor = Color.White),
//            elevation = CardDefaults.cardElevation(4.dp)
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                BesinOgesi("Kalori", "$kalori", "kcal", EmeraldGreen)
//                BesinOgesi("Prot", "$protein", "g", Color(0xFFE91E63))
//                BesinOgesi("Karb", "$karbonhidrat", "g", Color(0xFFFF9800))
//                BesinOgesi("Yağ", "$yag", "g", Color(0xFFF44336))
//            }
//        }
//
//        // --- YENİ: MİKTAR VE EKLEME ALANI ---
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            OutlinedTextField(
//                value = miktar,
//                onValueChange = {
//                    if (it.all { char -> char.isDigit() }) {
//                    miktar = it }
//                },
//                label = { Text("Miktar (gr)") },
//                modifier = Modifier.weight(1f),
//                shape = RoundedCornerShape(12.dp),
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//            )
//
//            Button(
//                onClick = { onEkleClick(miktar.toIntOrNull() ?: 0) },
//                modifier = Modifier.height(56.dp).weight(1f),
//                shape = RoundedCornerShape(12.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
//            ) {
//                Icon(Icons.Default.Add, contentDescription = null)
//                Spacer(Modifier.width(4.dp))
//                Text("Günlüğe Ekle")
//            }
//        }

        // --- İÇİNDEKİLER LİSTESİ ---
        Text(
            text = stringResource(R.string.icindekiler),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        // İçindekiler için LazyColumn yerine Column kullanıyoruz
        // çünkü dışarıda bir scroll var (Performans için az eleman varsa sorun olmaz)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        ) {
            icindekilerListesi.forEach { madde ->
                Text(
                    text = madde,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onIngredientClick(madde) }
                        .padding(14.dp),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
fun BesinOgesi(etiket: String, deger: String, birim: String, renk: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = etiket, fontSize = 12.sp, color = Color.Gray)
        Text(text = deger, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = renk)
        Text(text = birim, fontSize = 10.sp, color = Color.Gray)
    }
}