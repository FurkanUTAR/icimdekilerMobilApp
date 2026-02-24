package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecimBottomSheet(
    baslik: String,
    secenekler: List<String>,
    suAnkiSecili: String,
    onDismiss: () -> Unit,
    onSecildi: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp, top = 8.dp)
        ) {
            // Başlık (İsteğe bağlı)
            Text(
                text = baslik,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
            )

            secenekler.forEach { secenek ->
                TemaSecimSatiri( // Daha önce yazdığın satır tasarımını kullanıyoruz
                    label = secenek,
                    isSelected = (secenek == suAnkiSecili),
                    onSelect = {
                        onSecildi(secenek)
                        onDismiss()
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}