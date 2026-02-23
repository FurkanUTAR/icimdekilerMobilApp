
package com.example.icimdekiler.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.icimdekiler.R
import com.example.icimdekiler.model.Urunler
import com.example.icimdekiler.ui.theme.IcimdekilerTheme

@Composable
fun KullaniciTumUrunlerScreen(
    urunListesi: List<Urunler>,
    onSearchClick: (String) -> Unit,
    onUrunClick: (Urunler) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Arama Çubuğu
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text(stringResource(R.string.urunAra)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    IconButton(onClick = { onSearchClick(searchText) }) {
                        Icon(painterResource(id = R.drawable.search), contentDescription = null)
                    }
                },
                singleLine = true
            )

            Text(
                text = " ${stringResource(R.string.urunSayisi)} ${urunListesi.size}",
                modifier = Modifier.align(Alignment.End).padding(vertical = 8.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(urunListesi) { urun ->
                    UrunKarti(urun = urun, onClick = { onUrunClick(urun) })
                }
            }
        }
    }


}

@Composable
fun UrunKarti(urun: Urunler, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp), // BÜTÜN CARDLARIN AYNI BOY OLMASI İÇİN SABİT YÜKSEKLİK
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, // İçeriği dikeyde ortalar
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
        ) {
            // GÖRSELİN YUVARLAK OLMASI
            AsyncImage(
                model = urun.gorselUrl,
                contentDescription = urun.urunAdi,
                modifier = Modifier
                    .size(120.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape) // Tam yuvarlak yapar
                    .background(Color.LightGray.copy(alpha = 0.2f))
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary, // Buraya istediğin rengi verebilirsin
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentScale = ContentScale.Crop, // Görseli yuvarlağın içine düzgünce yayar
                error = painterResource(id = android.R.drawable.ic_menu_report_image),
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = urun.urunAdi ?: "İsimsiz Ürün",
                maxLines = 2,
                minLines = 2, // Metin kısa olsa bile 2 satırlık yer kaplar, böylece hizalama bozulmaz
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun KullaniciTumUrunlerPreview(){
    IcimdekilerTheme() {

    }
}