package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkanutar.icimdekiler.R

data class AdminCategory(
    val titleRes: Int,
    val imageRes: Int,
    val onClick: () -> Unit,
    val color: Color = Color.Transparent
)

@Composable
fun AdminAnaSayfaScreen(
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
    onSignOutConfirm: () -> Unit,
    onAyarlarClick: () -> Unit,
    onBildirilerClick: () -> Unit
) {
    val categories = listOf(
        AdminCategory(R.string.tumUrunler, R.drawable.tum_urunler, onTumUrunlerClick, Color(0xFF4CAF50)),
        AdminCategory(R.string.temelGida, R.drawable.temel_gida, onTemelGidaClick, Color(0xFFFF9800)),
        AdminCategory(R.string.sutVeSutUrunleri, R.drawable.sut_urunleri, onSutUrunleriClick, Color(0xFF2196F3)),
        AdminCategory(R.string.icecekler, R.drawable.icecekler, onIceceklerClick, Color(0xFF00BCD4)),
        AdminCategory(R.string.atistirmaliklar, R.drawable.atistirmalik, onAtistirmalikClick, Color(0xFFE91E63))
    )

    var mExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Üst Gradiyent Arka Plan
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.hosgeldin),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Admin Panel",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Box {
                    IconButton(
                        onClick = { mExpanded = true },
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(4.dp, CircleShape)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = mExpanded,
                        onDismissRequest = { mExpanded = false },
                        offset = DpOffset(x = (0).dp, y = 8.dp),
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.bildiriler)) },
                            onClick = { mExpanded = false; onBildirilerClick() },
                            leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.ayarlar)) },
                            onClick = { mExpanded = false; onAyarlarClick() },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.cikisYap), color = Color.Red) },
                            onClick = { mExpanded = false; onSignOutConfirm() },
                            leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red) }
                        )
                    }
                }
            }

            // Hızlı Aksiyonlar Kartı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Hızlı İşlemler",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ActionIconButton(
                            icon = Icons.Default.QrCodeScanner,
                            label = stringResource(R.string.barkodOku),
                            onClick = onBarcodeClick
                        )
                        ActionIconButton(
                            icon = Icons.Default.PostAdd,
                            label = stringResource(R.string.urunEkle),
                            onClick = onAddClick
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Arama ve Kategoriler
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = urunAdi,
                    onValueChange = onUrunAdiChange,
                    placeholder = { Text(stringResource(R.string.urunAra)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true
                )
                
                Button(
                    onClick = onSearchClick,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Ara")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Kategoriler",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories) { category ->
                    AdminCategoryCard(category = category)
                }
            }
        }
    }
}

@Composable
fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun AdminCategoryCard(category: AdminCategory) {
    Card(
        onClick = category.onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Arka Plan Hafif Renk
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(category.color.copy(alpha = 0.05f), category.color.copy(alpha = 0.2f))
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = category.imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(id = category.titleRes),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}