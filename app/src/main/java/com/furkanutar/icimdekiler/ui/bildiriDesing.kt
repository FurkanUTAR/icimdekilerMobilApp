package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.furkanutar.icimdekiler.model.Bildiri
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun BildiriListesi(
    bildiriListesi: List<Bildiri>,
    onBildiriClick : (Bildiri) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(bildiriListesi) { bildiri ->
            BildiriKarti(bildiri = bildiri, onClick = { onBildiriClick(bildiri) })
        }
    }
}

@Composable
fun BildiriKarti(bildiri: Bildiri, onClick: () -> Unit) {

    var checked by remember { mutableStateOf(true) }

    Card(
        onClick = onClick, // Tıklama olayı buraya bağlandı
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = bildiri.urunAdi.ifBlank { "Bilinmeyen Ürün" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Tarih formatlama (Firebase Timestamp kontrolü ile)
                Text(
                    text = bildiri.tarih?.toDate()?.let {
                        SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(it)
                    } ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (bildiri.aramaTerimi.isNotBlank()) {
                Text(
                    text = "Bulunamayan: ${bildiri.aramaTerimi}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (bildiri.barkodNo.isNotBlank()) {
                Text(
                    text = "Barkod: ${bildiri.barkodNo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

            Text(
                text = bildiri.mesaj.ifBlank { "Mesaj belirtilmemiş." },
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}