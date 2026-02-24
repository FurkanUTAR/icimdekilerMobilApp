package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.ui.theme.EmeraldGreen

@Composable
fun HesapAyarlariScreen(
    kullaniciAdi: String,
    isimSoyisim: String,
    ePosta: String,
    telNo: String,
    onKullaniciAdiChange: (String) -> Unit,
    onIsimSoyisimChange: (String) -> Unit,
    onEPostaChange: (String) -> Unit,
    onTelNoChange: (String) -> Unit,
    onParolaDegistirClick: () -> Unit,
    onGuncelleClick: () -> Unit,
    onHesabiSilClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = stringResource(id = R.string.hesapAyarlari),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OzelTextField(
            value = kullaniciAdi,
            onValueChange = onKullaniciAdiChange,
            hint = stringResource(id = R.string.kullaniciAdi)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OzelTextField(
            value = isimSoyisim,
            onValueChange = onIsimSoyisimChange,
            hint = stringResource(id = R.string.isimSoyisim)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OzelTextField(
            value = ePosta,
            onValueChange = onEPostaChange,
            hint = stringResource(id = R.string.ePosta),
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        OzelTextField(
            value = telNo,
            onValueChange = onTelNoChange,
            hint = stringResource(id = R.string.telNo),
            keyboardType = KeyboardType.Phone
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onParolaDegistirClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = stringResource(id = R.string.parolayiDegistir), color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onGuncelleClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = stringResource(id = R.string.guncelle), color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onHesabiSilClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)), // buttonRed
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = stringResource(id = R.string.hesabiSil), color = Color.White)
        }
    }
}

@Composable
fun OzelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = hint) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(2.dp, EmeraldGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = EmeraldGreen
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}