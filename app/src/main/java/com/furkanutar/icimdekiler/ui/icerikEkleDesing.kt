package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IcerikEkleScreen(
    onKaydetClick: (String, String) -> Unit
) {
    var urunAdi by remember { mutableStateOf("") }
    var aciklama by remember { mutableStateOf("") }
    // scrollState kalsın ama weight kullandığımızda Column içinde kaydırma mantığı değişebilir
    val scrollState = rememberScrollState()

    val anaYesil = EmeraldGreen
    val metinRengi = Color.Black

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = anaYesil,
        unfocusedBorderColor = anaYesil.copy(alpha = 0.5f),
        focusedLabelColor = anaYesil,
        unfocusedLabelColor = Color.Gray,
        cursorColor = anaYesil,
        focusedTextColor = metinRengi,
        unfocusedTextColor = metinRengi,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // 1. Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Ürün Adı
        OutlinedTextField(
            value = urunAdi,
            onValueChange = { urunAdi = it },
            label = { Text(stringResource(R.string.urunAdi)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Açıklama - ARADAKİ TÜM BOŞLUĞU DOLDURAN KISIM
        OutlinedTextField(
            value = aciklama,
            onValueChange = { aciklama = it },
            label = { Text(stringResource(R.string.aciklama)) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Butona kadar olan tüm boşluğu bu alan doldurur
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors
            // minLines silebilirsin çünkü weight zaten alanı maksimuma çıkaracak
        )

        Spacer(modifier = Modifier.height(24.dp)) // Alan ile buton arasında küçük bir boşluk

        // 4. Kaydet Butonu
        Button(
            onClick = { onKaydetClick(urunAdi, aciklama) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = anaYesil),
            shape = RoundedCornerShape(24.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.kaydet),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}