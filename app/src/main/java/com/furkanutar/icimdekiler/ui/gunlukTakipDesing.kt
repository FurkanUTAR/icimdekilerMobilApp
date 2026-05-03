package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.furkanutar.icimdekiler.R

val ProteinColor = Color(0xFFE91E63)
val KarbonhidratColor = Color(0xFFFF9800)
val YagColor = Color(0xFFF44336)
val GrayColor = Color.LightGray.copy(alpha = 0.5f)

data class GunlukHedef(
    var kalori: Int = 2000,
    var protein: Int = 150,
    var karbonhidrat: Int = 200,
    var yag: Int = 60
)

data class TuketimKaydi(
    val urunAdi: String,
    val kalori: Int,
    val protein: Float,
    val karbonhidrat: Float,
    val yag: Float,
    val miktarGr: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GunlukTakipScreen(
    tarih: LocalDate = LocalDate.now(),
    hedef: GunlukHedef,
    kayitlar: List<TuketimKaydi>,
    onTarihSecildi: (LocalDate) -> Unit = {},
    onHedefGuncelle: (GunlukHedef) -> Unit,
    onBackClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val toplamKalori = kayitlar.sumOf { it.kalori }
    val toplamProtein = kayitlar.sumOf { it.protein.toDouble() }.toFloat()
    val toplamKarb = kayitlar.sumOf { it.karbonhidrat.toDouble() }.toFloat()
    val toplamYag = kayitlar.sumOf { it.yag.toDouble() }.toFloat()

    val kalanKalori = (hedef.kalori - toplamKalori).coerceAtLeast(0)
    val progress = (toplamKalori.toFloat() / hedef.kalori.toFloat()).coerceIn(0f, 1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.gunlukTakip), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.geri))
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.hedefDuzenle))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showDatePicker = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tarih.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("tr"))),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.tarihSec),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    // Kalori Özeti
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.kaloriHedefi), fontSize = 14.sp, color = Color.Gray)
                            Text("$toplamKalori / ${hedef.kalori} kcal", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).height(10.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.LightGray.copy(alpha = 0.3f)
                            )
                            Text(stringResource(R.string.kalanKcal, kalanKalori), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                item {
                    // Makro Pie Chart
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.makroDagilimi), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                PieChart(
                                    protein = toplamProtein,
                                    karb = toplamKarb,
                                    yag = toplamYag,
                                    modifier = Modifier.size(120.dp)
                                )
                                Spacer(modifier = Modifier.width(24.dp))
                                Column {
                                    MakroGostergesi(stringResource(R.string.protein), toplamProtein, hedef.protein, ProteinColor)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    MakroGostergesi(stringResource(R.string.karbonhidrat), toplamKarb, hedef.karbonhidrat, KarbonhidratColor)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    MakroGostergesi(stringResource(R.string.yag), toplamYag, hedef.yag, YagColor)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(stringResource(R.string.tuketilenOgunler), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
                }

                if (kayitlar.isEmpty()) {
                    item {
                        Text(stringResource(R.string.henuzUrunEklemediniz), color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                } else {
                    items(kayitlar) { kayit ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(kayit.urunAdi, fontWeight = FontWeight.Bold)
                                    Text("${kayit.miktarGr}g • P:${String.format(Locale.US, "%.1f", kayit.protein)}g K:${String.format(Locale.US, "%.1f", kayit.karbonhidrat)}g Y:${String.format(Locale.US, "%.1f", kayit.yag)}g", fontSize = 12.sp, color = Color.Gray)
                                }
                                Text("${kayit.kalori} kcal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        HedefBelirleDialog(
            mevcutHedef = hedef,
            onDismiss = { showDialog = false },
            onSave = { yeniHedef ->
                onHedefGuncelle(yeniHedef)
                showDialog = false
            }
        )
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tarih.atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val secilenTarih = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.of("UTC"))
                            .toLocalDate()
                        onTarihSecildi(secilenTarih)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.tamam))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.iptal))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun PieChart(protein: Float, karb: Float, yag: Float, modifier: Modifier = Modifier) {
    val total = (protein + karb + yag).coerceAtLeast(1f) // 0'a bölme hatasını önle
    
    val proteinAngle = (protein / total) * 360f
    val karbAngle = (karb / total) * 360f
    val yagAngle = (yag / total) * 360f

    Canvas(modifier = modifier) {
        val strokeWidth = 24.dp.toPx()
        val size = Size(size.width - strokeWidth, size.height - strokeWidth)
        val offset = Offset(strokeWidth / 2, strokeWidth / 2)

        if (total == 1f && protein == 0f && karb == 0f && yag == 0f) {
            drawArc(color = GrayColor, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(strokeWidth, cap = StrokeCap.Round), size = size, topLeft = offset)
        } else {
            var currentStartAngle = -90f
            
            if (protein > 0) {
                drawArc(color = ProteinColor, startAngle = currentStartAngle, sweepAngle = proteinAngle, useCenter = false, style = Stroke(strokeWidth, cap = StrokeCap.Butt), size = size, topLeft = offset)
                currentStartAngle += proteinAngle
            }
            if (karb > 0) {
                drawArc(color = KarbonhidratColor, startAngle = currentStartAngle, sweepAngle = karbAngle, useCenter = false, style = Stroke(strokeWidth, cap = StrokeCap.Butt), size = size, topLeft = offset)
                currentStartAngle += karbAngle
            }
            if (yag > 0) {
                drawArc(color = YagColor, startAngle = currentStartAngle, sweepAngle = yagAngle, useCenter = false, style = Stroke(strokeWidth, cap = StrokeCap.Butt), size = size, topLeft = offset)
            }
        }
    }
}

@Composable
fun MakroGostergesi(isim: String, alinan: Float, hedef: Int, renk: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(renk))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(isim, fontSize = 12.sp, color = Color.Gray)
            Text("${String.format(Locale.US, "%.1f", alinan)} / ${hedef}g", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun HedefBelirleDialog(mevcutHedef: GunlukHedef, onDismiss: () -> Unit, onSave: (GunlukHedef) -> Unit) {
    var kalori by remember { mutableStateOf(mevcutHedef.kalori.toString()) }
    var protein by remember { mutableStateOf(mevcutHedef.protein.toString()) }
    var karb by remember { mutableStateOf(mevcutHedef.karbonhidrat.toString()) }
    var yag by remember { mutableStateOf(mevcutHedef.yag.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.hedefleriGuncelle), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(value = kalori, onValueChange = { kalori = it }, label = { Text(stringResource(R.string.gunlukKaloriKcal)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = protein, onValueChange = { protein = it }, label = { Text(stringResource(R.string.proteinGr)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = karb, onValueChange = { karb = it }, label = { Text(stringResource(R.string.karbonhidratGr)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = yag, onValueChange = { yag = it }, label = { Text(stringResource(R.string.yagGr)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.iptal)) }
                    Button(onClick = {
                        onSave(GunlukHedef(
                            kalori = kalori.toIntOrNull() ?: 2000,
                            protein = protein.toIntOrNull() ?: 150,
                            karbonhidrat = karb.toIntOrNull() ?: 200,
                            yag = yag.toIntOrNull() ?: 60
                        ))
                    }) { Text(stringResource(R.string.kaydet)) }
                }
            }
        }
    }
}
