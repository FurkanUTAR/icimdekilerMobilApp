package com.furkanutar.icimdekiler.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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

data class UrunEkleUiState(
    val barkodNo: String = "",
    val urunAdi: String = "",
    val seciliKategori: String = "",
    val seciliIcerik: String = "",
    val seciliGorselUrl: String? = null,
    val icindekilerListesi: List<String> = emptyList(),
    val kategoriler: List<String> = emptyList(),
    val icerikler: List<String> = emptyList(),
    val yeniMi: Boolean = true,
    // Besin Değerleri (100g başına)
    val kalori: String = "",
    val protein: String = "",
    val karbonhidrat: String = "",
    val yag: String = ""
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
    onSilClick: () -> Unit,
    onIcerikYerDegistir: (Int, Int) -> Unit,
    onIcerikMetinDegistir: (String) -> Unit,
    // Besin Değerleri Callback'leri
    onKaloriChange: (String) -> Unit = {},
    onProteinChange: (String) -> Unit = {},
    onKarbonhidratChange: (String) -> Unit = {},
    onYagChange: (String) -> Unit = {},
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Arka Plan Gradiyenti (Üst Kısım İçin)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Durum Rozeti (Yeni / Düzenleme)
            Surface(
                color = if (state.yeniMi) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = if (state.yeniMi) stringResource(R.string.yeniUrun) else stringResource(R.string.mevcutUrun),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (state.yeniMi) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // Başlık
            Text(
                text = if (state.yeniMi) stringResource(R.string.urunEkle) else stringResource(R.string.urunDuzenle),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ürün Görseli Kartı
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onGorselSecClick() }
                    .shadow(12.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = state.seciliGorselUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.insert_photo),
                    error = painterResource(R.drawable.insert_photo)
                )
                
                // Kamera İkonu (Görsel Seçme İpucu)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 8.dp, bottom = 8.dp)
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // TEMEL BİLGİLER BÖLÜMÜ
            SectionCard(title = stringResource(R.string.temelBilgiler)) {
                // Barkod Alanı
                OutlinedTextField(
                    value = state.barkodNo,
                    onValueChange = onBarkodChange,
                    label = { Text(stringResource(R.string.barkod)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = onBarkodOkuClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.barcode_reader),
                                contentDescription = stringResource(R.string.barkod),
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Ürün Adı
                OutlinedTextField(
                    value = state.urunAdi,
                    onValueChange = onUrunAdiChange,
                    label = { Text(stringResource(R.string.urunAdi)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Kategori Dropdown
                UrunEkleDropdown(
                    label = stringResource(R.string.kategori),
                    options = state.kategoriler,
                    selectedValue = state.seciliKategori,
                    onSelect = onKategoriSec
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BESİN DEĞERLERİ BÖLÜMÜ
            SectionCard(title = stringResource(R.string.besinDegerleri)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MacroInput(
                            value = state.kalori,
                            onValueChange = onKaloriChange,
                            label = stringResource(R.string.kaloriKcal100g),
                            icon = Icons.Default.LocalFireDepartment,
                            modifier = Modifier.weight(1f)
                        )
                        MacroInput(
                            value = state.protein,
                            onValueChange = onProteinChange,
                            label = stringResource(R.string.protein100g),
                            icon = Icons.Default.FitnessCenter,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MacroInput(
                            value = state.karbonhidrat,
                            onValueChange = onKarbonhidratChange,
                            label = stringResource(R.string.karbonhidrat100g),
                            icon = Icons.Default.Grain,
                            modifier = Modifier.weight(1f)
                        )
                        MacroInput(
                            value = state.yag,
                            onValueChange = onYagChange,
                            label = stringResource(R.string.yag100g),
                            icon = Icons.Default.Opacity,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // İÇİNDEKİLER BÖLÜMÜ
            SectionCard(title = stringResource(R.string.icindekiler)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        YazilabilirDropdown(
                            label = stringResource(R.string.icerikSecEkle),
                            options = state.icerikler,
                            selectedValue = state.seciliIcerik,
                            onValueChange = onIcerikMetinDegistir,
                            onSelect = onIcerikSec
                        )
                    }

                    FloatingActionButton(
                        onClick = onIcerikEkle,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Eklenen İçerikler Listesi
                AnimatedVisibility(
                    visible = state.icindekilerListesi.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            itemsIndexed(state.icindekilerListesi) { index, item ->
                                IngredientItem(
                                    name = item,
                                    index = index,
                                    isLast = index == state.icindekilerListesi.size - 1,
                                    isFirst = index == 0,
                                    onMoveUp = { onIcerikYerDegistir(index, index - 1) },
                                    onMoveDown = { onIcerikYerDegistir(index, index + 1) },
                                    onDelete = { onIcerikSil(index) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // AKSİYON BUTONLARI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!state.yeniMi) {
                    OutlinedButton(
                        onClick = onSilClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.sil), fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onKaydetClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.yeniMi) stringResource(R.string.kaydet) else stringResource(R.string.guncelle),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun MacroInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
    )
}

@Composable
fun IngredientItem(
    name: String,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sıralama Butonları
        Column {
            IconButton(
                onClick = onMoveUp,
                enabled = !isFirst,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.ArrowDropUp,
                    contentDescription = null,
                    tint = if (isFirst) Color.LightGray else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onMoveDown,
                enabled = !isLast,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (isLast) Color.LightGray else MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${index + 1}. $name",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.RemoveCircle, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UrunEkleDropdown(
    label: String,
    options: List<String>,
    selectedValue: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YazilabilirDropdown(
    label: String,
    options: List<String>,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = onValueChange,
            label = { Text(label) },
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true
        )

        if (options.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.filter { it.contains(selectedValue, ignoreCase = true) }.forEach { option ->
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
}