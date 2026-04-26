package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import coil.compose.AsyncImage
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.ui.theme.EmeraldGreen

@Composable
fun UrunScreen(
    urunAdi: String,
    gorselUrl: String,
    kalori: Int,
    protein: Float,
    karbonhidrat: Float,
    yag: Float,
    icindekilerListesi: List<String>,
    onIngredientClick: (String) -> Unit,
    onEkleClick: (Int) -> Unit
) {
    var miktar by remember { mutableStateOf("100") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ── Görsel ──────────────────────────────────────────────────────────────
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
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        // ── Besin Değerleri Özet Kartı ───────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BesinOgesi(
                    etiket = "Kalori",
                    deger = if (kalori > 0) "$kalori" else "—",
                    birim = "kcal",
                    renk = EmeraldGreen
                )
                BesinOgesiDivider()
                BesinOgesi(
                    etiket = "Protein",
                    deger = if (protein > 0f) "%.1f".format(protein) else "—",
                    birim = "g",
                    renk = Color(0xFFE91E63)
                )
                BesinOgesiDivider()
                BesinOgesi(
                    etiket = "Karb",
                    deger = if (karbonhidrat > 0f) "%.1f".format(karbonhidrat) else "—",
                    birim = "g",
                    renk = Color(0xFFFF9800)
                )
                BesinOgesiDivider()
                BesinOgesi(
                    etiket = "Yağ",
                    deger = if (yag > 0f) "%.1f".format(yag) else "—",
                    birim = "g",
                    renk = Color(0xFFF44336)
                )
            }
        }

        // ── Miktar + Günlüğe Ekle ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = miktar,
                onValueChange = { if (it.all { c -> c.isDigit() }) miktar = it },
                label = { Text("Miktar (gr)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Button(
                onClick = { onEkleClick(miktar.toIntOrNull() ?: 0) },
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Günlüğe Ekle")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── İçindekiler Listesi ──────────────────────────────────────────────────
        Text(
            text = stringResource(R.string.icindekiler),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (icindekilerListesi.isEmpty()) {
            Text(
                text = "İçindekiler bilgisi bulunamadı.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        } else {
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

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Besin Ögesi Göstergesi ───────────────────────────────────────────────────
@Composable
fun BesinOgesi(etiket: String, deger: String, birim: String, renk: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = etiket,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = deger,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = renk
        )
        Text(
            text = birim,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// Kart içi dikey ayırıcı
@Composable
private fun BesinOgesiDivider() {
    HorizontalDivider(
        modifier = Modifier
            .height(40.dp)
            .width(1.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    )
}