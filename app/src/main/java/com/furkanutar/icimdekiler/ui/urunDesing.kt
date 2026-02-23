package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkanutar.icimdekiler.R
import coil.compose.AsyncImage

// ... importlar aynı ...

@Composable
fun UrunScreen(
    urunAdi: String,
    gorselUrl: String,
    icindekilerListesi: List<String>,
    onIngredientClick: (String) -> Unit
) {
    // Box kullanarak içeriği sağlama alıyoruz
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Burası çok kritik
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Görsel Alanı
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary), // Vurgu için yeşil çerçeve
                shadowElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                AsyncImage(
                    model = gorselUrl,
                    contentDescription = urunAdi,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    error = painterResource(id = R.drawable.insert_photo),
                    placeholder = painterResource(id = R.drawable.insert_photo)
                )
            }

            Text(
                text = urunAdi,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Text(
                text = stringResource(R.string.icindekiler),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp)
            )

            // EĞER HALA ÇÖKÜYORSA: weight(1f) yerine sabit bir height vererek test et
            // Örneğin: .heightIn(min = 200.dp, max = 800.dp)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(17.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(icindekilerListesi) { madde ->
                        Text(
                            text = madde,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onIngredientClick(madde) }
                                .padding(16.dp),
                            fontSize = 16.sp
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }
                }
            }
        }
    }
}