package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.ui.theme.IcimdekilerTheme

data class KullaniciCategory(
    val titleRes: Int,
    val imageRes: Int,
    val onClick: () -> Unit,
)

@Composable
fun KullaniciAnaSayfaScreen(
    urunAdi: String,
    onUrunAdiChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onAddClick: () -> Unit,
    onBarcodeClick: () -> Unit,
    onTumUrunlerClick: () -> Unit,
    onAtistirmalikClick: () -> Unit,
    onTemelGidaClick: () -> Unit,
    onSutUrunleriClick: () -> Unit,
    onIceceklerClick: () -> Unit,
    onSignOutConfirm: () -> Unit, // Çıkış onayı için callback
    onAyarlarClick: () -> Unit   // Ayarlar navigasyonu için callback
) {
    val categories = listOf(
        AdminCategory(R.string.tumUrunler, R.drawable.tum_urunler, onTumUrunlerClick),
        AdminCategory(R.string.temelGida, R.drawable.temel_gida, onTemelGidaClick),
        AdminCategory(R.string.sutVeSutUrunleri, R.drawable.sut_urunleri, onSutUrunleriClick),
        AdminCategory(R.string.icecekler, R.drawable.icecekler, onIceceklerClick),
        AdminCategory(R.string.atistirmaliklar, R.drawable.atistirmalik, onAtistirmalikClick)
    )

    var mExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box { // DropdownMenu'yü konumlandırmak için Box şart
                    IconButton(
                        onClick = { mExpanded = true }, // Menüyü aç
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.menu),
                            contentDescription = stringResource(R.string.menu),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    DropdownMenu(
                        modifier = Modifier
                            .padding(top = 10.dp),
                        expanded = mExpanded,
                        onDismissRequest = { mExpanded = false }, // Dışarı basınca kapansın
                        offset = DpOffset(x = (0).dp, y = 8.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.ayarlar)) },
                            onClick = {
                                mExpanded = false
                                onAyarlarClick() // Fragment'a haber ver
                            },
                            leadingIcon = { Icon(painterResource(R.drawable.settings), contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.cikisYap)) },
                            onClick = {
                                mExpanded = false
                                onSignOutConfirm() // Çıkış diyaloğunu tetikle
                            },
                            leadingIcon = { Icon(painterResource(R.drawable.logout), contentDescription = null) }
                        )
                    }
                }
            }

            IconButton(
                onClick = onBarcodeClick,
                modifier = Modifier
                    .size(width = 170.dp, height = 120.dp) // Görselin sığması için buton boyutunu belirledik
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.barcode_reader),
                    contentDescription = stringResource(R.string.barkodOku),
                    modifier = Modifier
                        .size(width = 160.dp, height = 110.dp)
                        .padding(top = 10.dp),
                    tint = Color.Unspecified // Orijinal görsel renklerini korumak için (siyah olmaması için)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = urunAdi,
                    onValueChange = onUrunAdiChange,
                    placeholder = { Text(stringResource(R.string.urunAra)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    trailingIcon = {
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = stringResource(R.string.urunAra)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories) { category ->
                    KullaniciCategoryCard(category = category)
                }
            }
        }
    }
}

@Composable
private fun KullaniciCategoryCard(category: AdminCategory) {
    Card(
        onClick = category.onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(142.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = category.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = stringResource(id = category.titleRes),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun KullaniciAnaSayfaPreview() {
    IcimdekilerTheme() {
        KullaniciAnaSayfaScreen(
            urunAdi = "",
            onUrunAdiChange = {},
            onSearchClick = {},
            onAddClick = {},
            onBarcodeClick = {},
            onTumUrunlerClick = {},
            onAtistirmalikClick = {},
            onTemelGidaClick = {},
            onSutUrunleriClick = {},
            onIceceklerClick = {},
            onSignOutConfirm = {},
            onAyarlarClick = {}
        )
    }
}