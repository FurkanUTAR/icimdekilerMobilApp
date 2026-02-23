package com.example.icimdekiler.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.icimdekiler.R

data class UrunEkleUiState(
    val barkodNo: String = "",
    val urunAdi: String = "",
    val seciliKategori: String = "",
    val seciliIcerik: String = "",
    val seciliGorselUrl: String? = null,
    val icindekilerListesi: List<String> = emptyList(),
    val kategoriler: List<String> = emptyList(),
    val icerikler: List<String> = emptyList(),
    val yeniMi: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrunEkleScreen(
    state: UrunEkleUiState,
    onBarkodChange: (String) -> Unit,
    onBarkodOkuClick: () -> Unit,
    onUrunAdiChange: (String) -> Unit,
    onGorselSecClick: () -> Unit,
    onKategoriSec: (String) -> Unit,
    onIcerikSec: (String) -> Unit,
    onIcerikEkle: () -> Unit,
    onIcerikSil: (Int) -> Unit,
    onKaydetClick: () -> Unit,
    onSilClick: () -> Unit
) {
    // Klavye açıldığında ekranın kaydırılabilmesi için scroll state
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState) // Klavye odaklı kaydırma için eklendi
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Ürün Görseli
        Surface(
            modifier = Modifier
                .size(120.dp)
                .clickable { onGorselSecClick() },
            shape = CircleShape,
            shadowElevation = 4.dp,
            color = Color.White,
            border = BorderStroke(2.dp, Color(0xFF4CAF50)) // Vurgu için yeşil çerçeve
        ) {
            AsyncImage(
                model = state.seciliGorselUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.insert_photo),
                error = painterResource(R.drawable.insert_photo)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Barkod Alanı
        OutlinedTextField(
            value = state.barkodNo,
            onValueChange = onBarkodChange,
            label = { Text(stringResource(R.string.barkod)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = onBarkodOkuClick) {
                    // DÜZELTME: R.drawable doğrudan verilmez, painterResource kullanılır
                    Icon(
                        painter = painterResource(id = R.drawable.barcode_reader),
                        contentDescription = stringResource(R.string.barkod),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Ürün Adı
        OutlinedTextField(
            value = state.urunAdi,
            onValueChange = onUrunAdiChange,
            label = { Text(stringResource(R.string.urunAdi)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Kategori Dropdown
        UrunEkleDropdown(
            label = stringResource(R.string.kategori),
            options = state.kategoriler,
            selectedValue = state.seciliKategori,
            onSelect = onKategoriSec
        )

        Spacer(modifier = Modifier.height(12.dp))

        // İçerik Ekleme Satırı
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                UrunEkleDropdown(
                    label = stringResource(R.string.icerik),
                    options = state.icerikler,
                    selectedValue = state.seciliIcerik,
                    onSelect = onIcerikSec
                )
            }

            IconButton(
                onClick = onIcerikEkle,
                modifier = Modifier
                    .background(Color(0xFF4CAF50), CircleShape) // Daha belirgin buton
                    .size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // İçindekiler Listesi (LazyColumn bir Box veya sabit yükseklik içinde olmalı)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp, max = 300.dp), // OnMeasure hatasını önlemek için sınır koyduk
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFF4CAF50))
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(state.icindekilerListesi) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item, modifier = Modifier.weight(1f))
                        // Küçük silme butonu
                        IconButton(onClick = { onIcerikSil(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Aksiyon Butonları
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!state.yeniMi) {
                Button(
                    onClick = onSilClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = stringResource(R.string.sil), fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onKaydetClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = stringResource(R.string.kaydet), color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UrunEkleDropdown(
    label: String,
    options: List<String>,
    selectedValue: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
