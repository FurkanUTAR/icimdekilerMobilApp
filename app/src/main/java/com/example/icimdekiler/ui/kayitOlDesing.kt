package com.example.icimdekiler.ui

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.icimdekiler.R
import com.example.icimdekiler.ui.theme.IcimdekilerTheme

@Composable
fun KayitOlScreen(kayitOlTiklandi: (String,String,String,String,String) -> Unit, girisYapTiklandi: () -> Unit ){

    var kullaniciAdi = remember { mutableStateOf("") }
    var isimSoyisim = remember { mutableStateOf("") }
    var ePosta = remember { mutableStateOf("") }
    var telNo = remember { mutableStateOf("") }
    var parola = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            modifier = Modifier
                .padding(top = 24.dp)
                .size(120.dp),
            shape = CircleShape
        ) {
            Image(
                bitmap = ImageBitmap.imageResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.padding(30.dp))

        // --- KULLANICI ADI  ---
        OutlinedTextField(
            value = kullaniciAdi.value,
            onValueChange = { kullaniciAdi.value = it },
            placeholder = {Text(stringResource(R.string.kullaniciAdi))},
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.padding(5.dp))

        // --- İsim Soyisim  ---
        OutlinedTextField(
            value = isimSoyisim.value,
            onValueChange = { isimSoyisim.value = it },
            placeholder = {Text(stringResource(R.string.isimSoyisim))},
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.padding(5.dp))

        // --- e-Posta ---
        OutlinedTextField(
            value = ePosta.value,
            onValueChange = { ePosta.value = it },
            placeholder = {Text(stringResource(R.string.ePosta))},
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.padding(5.dp))

        // --- Telefon Numarası ---
        OutlinedTextField(
            value = telNo.value,
            onValueChange = { telNo.value = it },
            placeholder = {Text(stringResource(R.string.telNo))},
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.padding(5.dp))

        // --- Parola ---
        OutlinedTextField(
            value = parola.value,
            onValueChange = { parola.value = it },
            placeholder = {Text(stringResource(R.string.parola))},
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // --- Giriş Yap Text ---
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.zatenHesabinVarMi),
                color = Color.Black,
                fontSize = 14.sp
            )
            TextButton(onClick = girisYapTiklandi) {
                Text(
                    text = stringResource(R.string.girisYap),
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- Kayıt Ol Buton ---
        Button(
            onClick = { kayitOlTiklandi(kullaniciAdi.value, isimSoyisim.value, ePosta.value, telNo.value, parola.value) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = stringResource(R.string.kayitOl),
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun previewKayitOlScreen(){
    IcimdekilerTheme() {
        KayitOlScreen({_,_,_,_,_ ->}, girisYapTiklandı = {})
    }
}
*/