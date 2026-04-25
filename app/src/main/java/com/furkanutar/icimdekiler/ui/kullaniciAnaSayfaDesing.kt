package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkanutar.icimdekiler.R
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KullaniciAnaSayfaScreen(
    urunAdi: String,
    isLoggedIn: Boolean, // Firebase'den gelecek giriş durumu
    kullaniciAdSoyad: String = "",
    kullaniciEmail: String = "",
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
    val tumUrunlerCategory = AdminCategory(R.string.tumUrunler, R.drawable.tum_urunler, onTumUrunlerClick)
    val categories = listOf(
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
                // Tüm içeriği kapsayan ana sütun
                Column(modifier = Modifier.fillMaxHeight()) {

                    // --- ÜST KISIM: PROFİL BAŞLIĞI ---
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

                    // --- ORTA KISIM: MENÜ ÖĞELERİ ---
                    if (isLoggedIn) {
                        DrawerItem(R.drawable.settings, label = stringResource(R.string.ayarlar), onClick = onAyarlarClick)
                    }

                    // BU SPACER HER ŞEYİ AŞAĞI İTER
                    Spacer(modifier = Modifier.weight(1f))

                    // --- ALT KISIM: SABİT BUTONLAR ---
                    if (!isLoggedIn) {
                        // Giriş ve Kayıt Yan Yana
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Giriş Yap Butonu
                            OutlinedButton(
                                onClick = onLoginClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp) // İç boşluğu daralttık
                            ) {
                                Icon(painterResource(R.drawable.login), contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.girisYap), fontSize = 12.sp, maxLines = 1)
                            }

                            // Kayıt Ol Butonu
                            Button(
                                onClick = onRegisterClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(painterResource(R.drawable.person_add), contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.kayitOl), fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    } else {
                        // Çıkış Yap En Altta Sabit
                        Column {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), thickness = 0.5.dp)
                            DrawerItem(R.drawable.logout,  label = stringResource(R.string.cikisYap), onClick = onSignOutConfirm, textColor = Color.Red)
                            Spacer(modifier = Modifier.height(12.dp)) // En alttan biraz boşluk
                        }
                    }
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(painterResource(id = R.drawable.menu), contentDescription = "Menü", tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("İçimdekiler", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = onBarcodeClick,
                        modifier = Modifier
                            .size(90.dp)
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.barcode_reader),
                            contentDescription = "Barkod Oku",
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.Unspecified // Logonun kendi renklerini korur
                        )
                    }

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
                    if (isLoggedIn) {
                        /* Şimdilik kalori takibi rafa kaldırıldığı için gizlendi
                        KaloriTakipKarti(
                            gunlukKaloriHedefi = gunlukKaloriHedefi,
                            harcananKalori = harcananKalori
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        */
                    }

                    KategoriBolumu(
                        tumUrunlerCategory = tumUrunlerCategory,
                        digerCategories = categories,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }
            }
            
            // AI Chat Bot FAB
            ChatBotFab(modifier = Modifier.align(Alignment.BottomEnd))
        }
    }
}

@Composable
private fun KategoriBolumu(
    tumUrunlerCategory: AdminCategory,
    digerCategories: List<AdminCategory>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KullaniciCategoryCard(
            category = tumUrunlerCategory,
            modifier = Modifier
                .fillMaxWidth()
                .height(152.dp)
        )

        digerCategories.chunked(2).forEach { satir ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                satir.forEach { category ->
                    KullaniciCategoryCard(
                        category = category,
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                    )
                }

                if (satir.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun KaloriTakipKarti(
    gunlukKaloriHedefi: Int,
    harcananKalori: Int
) {
    val kalanKalori = (gunlukKaloriHedefi - harcananKalori).coerceAtLeast(0)
    val progress = (harcananKalori.toFloat() / gunlukKaloriHedefi.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Kalori Takibi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Kalan", color = Color.Gray)
                Text("$kalanKalori kcal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.LightGray.copy(alpha = 0.4f)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Alınan: $harcananKalori kcal", fontSize = 12.sp, color = Color.Gray)
                Text("Hedef: $gunlukKaloriHedefi kcal", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun DrawerItem(iconRes: Int? = null, iconVector: ImageVector? = null, label: String, onClick: () -> Unit, textColor: Color = Color.Black) {
    NavigationDrawerItem(
        icon = {
            val iconTintColor = if (textColor == Color.Red) Color.Red else MaterialTheme.colorScheme.primary

            if (iconRes != null) {
                Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = iconTintColor)
            } else if (iconVector != null) {
                Icon(imageVector = iconVector, contentDescription = null, tint = iconTintColor)
            }
        },
        label = { Text(label, color = textColor, fontWeight = FontWeight.Medium) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun KullaniciCategoryCard(
    category: AdminCategory,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = category.onClick,
        modifier = modifier,
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