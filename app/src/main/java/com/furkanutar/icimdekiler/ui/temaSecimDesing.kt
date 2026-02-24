package com.furkanutar.icimdekiler.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.R
import com.furkanutar.icimdekiler.ui.theme.EmeraldGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemaBottomSheet(
    currentMode: Int, // Mevcut modu alıyoruz
    onDismiss: () -> Unit,
    onTemaSecildi: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    // UI metinlerini mode ile eşleştirme
    val modToMetin = mapOf(
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM to "Varsayılan",
        AppCompatDelegate.MODE_NIGHT_YES to "Karanlık",
        AppCompatDelegate.MODE_NIGHT_NO to "Aydınlık"
    )

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
            val temalar = listOf("Varsayılan", "Karanlık", "Aydınlık")

            temalar.forEach { tema ->
                TemaSecimSatiri(
                    label = tema,
                    // Eğer bu satırdaki tema, mevcut modun metin karşılığıysa seçili göster
                    isSelected = (tema == modToMetin[currentMode]),
                    onSelect = {
                        onTemaSecildi(tema)
                        onDismiss()
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun TemaSecimSatiri(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    // Görseldeki o yeşil çerçeveli kutu tasarımı
    Surface(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, if (isSelected) EmeraldGreen else EmeraldGreen.copy(alpha = 0.3f)),
        color = if (isSelected) EmeraldGreen.copy(alpha = 0.05f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null, // Surface'ın tıklandığında çalışması yeterli
                colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, fontSize = 16.sp, color = Color.Black)
        }
    }
}