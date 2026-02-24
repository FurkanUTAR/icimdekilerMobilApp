package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.ui.theme.EmeraldGreen
import com.furkanutar.icimdekiler.ui.theme.LightBackground

@Composable
fun AyarlarScreen(
    onHesapClick: () -> Unit,
    onTemaClick: () -> Unit,
    onDilClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header - Ayarlar Başlığı
        Text(
            text = stringResource(R.string.ayarlar),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(top = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Ayar Seçenekleri Kartı
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(17.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                // Hesap Ayarı
                AyarButonu(
                    text = stringResource(R.string.hesap),
                    onClick = onHesapClick
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = Color.Black.copy(alpha = 0.1f) // Saf siyah yerine hafif şeffaf daha modern durur
                )

                // Tema Ayarı
                AyarButonu(
                    text = stringResource(R.string.temaDegistirme),
                    onClick = onTemaClick
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = Color.Black.copy(alpha = 0.1f)
                )

                // Dil Ayarı
                AyarButonu(
                    text = stringResource(R.string.dil),
                    onClick = onDilClick
                )
            }
        }
    }
}

@Composable
fun AyarButonu(
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        shape = RoundedCornerShape(0.dp) // Kartın köşelerine uyum sağlaması için
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // İstersen buraya Icon(painter = ...) ekleyerek XML'deki app:icon özelliğini canlandırabiliriz
            Text(
                text = text,
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}