package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkanutar.icimdekiler.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KullaniciAnaSayfaScreen(
    urunAdi: String,
    isLoggedIn: Boolean, // Firebase'den gelecek giriş durumu
    kullaniciAdSoyad: String = "Furkan Utar", // Örnek isim
    kullaniciEmail: String = "furkan@example.com",
    gunlukKaloriHedefi: Int = 2000,
    harcananKalori: Int = 450,
    onUrunAdiChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onBarcodeClick: () -> Unit,
    onTumUrunlerClick: () -> Unit,
    onAtistirmalikClick: () -> Unit,
    onTemelGidaClick: () -> Unit,
    onSutUrunleriClick: () -> Unit,
    onIceceklerClick: () -> Unit,
    onSignOutConfirm: () -> Unit,
    onAyarlarClick: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val categories = listOf(
        AdminCategory(R.string.tumUrunler, R.drawable.tum_urunler, onTumUrunlerClick),
        AdminCategory(R.string.temelGida, R.drawable.temel_gida, onTemelGidaClick),
        AdminCategory(R.string.sutVeSutUrunleri, R.drawable.sut_urunleri, onSutUrunleriClick),
        AdminCategory(R.string.icecekler, R.drawable.icecekler, onIceceklerClick),
        AdminCategory(R.string.atistirmaliklar, R.drawable.atistirmalik, onAtistirmalikClick)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp).fillMaxHeight(),
                drawerContainerColor = Color.White,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                // --- PROFIL BAŞLIĞI ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(35.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = kullaniciAdSoyad, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = kullaniciEmail, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- KALORİ TAKİP ÖZETİ ---
                if (isLoggedIn) {
                    Text(
                        "Günlük Takip",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Kalan Kalori", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("${gunlukKaloriHedefi - harcananKalori} kcal", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { harcananKalori.toFloat() / gunlukKaloriHedefi.toFloat() },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).height(8.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.LightGray.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- MENÜ ÖĞELERİ ---
                if (!isLoggedIn) {
                    DrawerItem(Icons.Default.Settings, "Giriş Yap", onLoginClick)
                    DrawerItem(Icons.Default.Settings, "Kayıt Ol", onRegisterClick)
                } else {
                    DrawerItem(Icons.Default.Settings, "Ayarlar", onAyarlarClick)
                    DrawerItem(Icons.Default.Settings, "Geçmiş Veriler", { /* Navigasyon */ })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), thickness = 0.5.dp)
                    DrawerItem(Icons.Default.Settings, "Çıkış Yap", onSignOutConfirm, textColor = Color.Red)
                }

                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Versiyon 1.0.2",
                    modifier = Modifier.padding(20.dp).align(Alignment.CenterHorizontally),
                    fontSize = 11.sp, color = Color.Gray
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(painterResource(id = R.drawable.menu), contentDescription = "Menü", tint = MaterialTheme.colorScheme.primary)
                    }
                    Text("İçimdekiler", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = onBarcodeClick) {
                        Icon(painterResource(id = R.drawable.barcode_reader), contentDescription = "Barkod", modifier = Modifier.size(30.dp), tint = Color.Unspecified)
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Arama Çubuğu
                OutlinedTextField(
                    value = urunAdi,
                    onValueChange = onUrunAdiChange,
                    placeholder = { Text(stringResource(R.string.urunAra)) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    trailingIcon = {
                        IconButton(onClick = onSearchClick) {
                            Icon(painterResource(id = R.drawable.search), contentDescription = null)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Kategori Izgarası
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { category ->
                        KullaniciCategoryCard(category = category)
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit, textColor: Color = Color.Black) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null, tint = if (textColor == Color.Red) Color.Red else MaterialTheme.colorScheme.primary) },
        label = { Text(label, color = textColor, fontWeight = FontWeight.Medium) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun KullaniciCategoryCard(category: AdminCategory) {
    Card(
        onClick = category.onClick,
        modifier = Modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = category.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(15.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = stringResource(id = category.titleRes),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}