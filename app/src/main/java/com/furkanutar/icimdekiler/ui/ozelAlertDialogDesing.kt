package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.furkanutar.icimdekiler.ui.theme.EmeraldGreen

@Composable
fun OzelAlertDialog(
    baslik: String, // Dışarıdan gelecek metin (Silmek istiyor musunuz? vb.)
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onayButonMetni: String = "Evet", // Varsayılan değer atayabilirsin
    iptalButonMetni: String = "Hayır",
    onayButonRengi: Color = EmeraldGreen // Silme işlemi için bazen kırmızı gerekebilir
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = baslik,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = onayButonRengi),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)
            ) {
                Text(onayButonMetni, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(iptalButonMetni, color = Color.Gray)
            }
        }
    )
}