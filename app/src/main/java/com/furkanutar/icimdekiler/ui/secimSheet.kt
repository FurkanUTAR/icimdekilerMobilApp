package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkanutar.icimdekiler.R

@Composable
fun KaynakSecimDialog(
    isBarcodeAction: Boolean, // Barkod için mi yoksa Ekleme için mi?
    onDismiss: () -> Unit,
    onOption1: () -> Unit, // Kamera veya Ürün Ekle
    onOption2: () -> Unit  // Galeri veya İçerik Ekle
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = stringResource(R.string.secimYap),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // SEÇENEK 1: Kamera ya da Ürün Ekle
                TextButton(
                    onClick = { onOption1(); onDismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isBarcodeAction) stringResource(R.string.kamera)
                        else stringResource(R.string.urunEkle),
                        color = Color.Black,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // SEÇENEK 2: Galeri ya da İçerik Ekle
                TextButton(
                    onClick = { onOption2(); onDismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isBarcodeAction) stringResource(R.string.galeri)
                        else stringResource(R.string.icerikEkle),
                        color = Color.Black,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {}
    )
}