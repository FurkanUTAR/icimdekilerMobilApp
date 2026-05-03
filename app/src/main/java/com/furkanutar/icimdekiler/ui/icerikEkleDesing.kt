package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.model.Icerikler
import com.furkanutar.icimdekiler.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IcerikEkleScreen(
    mevcutIcerikler: List<Icerikler>,
    onKaydetClick: (String, String) -> Unit,
    onSilClick: (String) -> Unit
) {
    // Bottom Sheet (Aşağıdan açılan menü) durumunu kontrol eden değişkenler
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Form alanları
    var urunAdi by remember { mutableStateOf("") }
    var aciklama by remember { mutableStateOf("") }

    val anaYesil = EmeraldGreen // Tema rengin
    val metinRengi = MaterialTheme.colorScheme.onBackground

    // TextField stil ayarları
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = anaYesil,
        unfocusedBorderColor = anaYesil.copy(alpha = 0.5f),
        focusedLabelColor = anaYesil,
        unfocusedLabelColor = Color.Gray,
        cursorColor = anaYesil,
        focusedTextColor = metinRengi,
        unfocusedTextColor = metinRengi,
    )

    // Scaffold, FAB (Yüzen Buton) gibi yapıları kolayca eklememizi sağlar
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = anaYesil,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.yeniIcerikEkle), modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        // --- ANA EKRAN: LİSTE ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues) // Scaffold'un FAB ve diğer barlara göre ayırdığı boşluk
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.icerikYonetimi),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = anaYesil,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Mevcut içeriklerin listesi (Tam ekran)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp), // Listenin en altındaki eleman FAB'ın altında kalmasın diye
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mevcutIcerikler) { icerik ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = icerik.ad,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = metinRengi
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = icerik.aciklama,
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { onSilClick(icerik.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.sil), tint = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }
    }

    // --- AŞAĞIDAN AÇILAN PANEL (BOTTOM SHEET): EKLEME FORMU ---
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background, // Formun arka planı
            dragHandle = { BottomSheetDefaults.DragHandle() } // Üstteki küçük gri çekme çubuğu
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp), // Klavyeden ve navigasyon barından uzak tutmak için
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.yeniIcerikEkle),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = anaYesil,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = urunAdi,
                    onValueChange = { urunAdi = it },
                    label = { Text(stringResource(R.string.urunAdi)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = aciklama,
                    onValueChange = { aciklama = it },
                    label = { Text(stringResource(R.string.aciklama)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onKaydetClick(urunAdi, aciklama)
                        // İşlem bitince kutuları temizle ve menüyü kapat
                        urunAdi = ""
                        aciklama = ""
                        showBottomSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = anaYesil),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(text = stringResource(R.string.kaydet), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}