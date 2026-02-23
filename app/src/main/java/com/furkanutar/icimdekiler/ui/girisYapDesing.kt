package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.ui.theme.IcimdekilerTheme

@Composable
fun GirisYapScreen(girisYapTiklandi: (String, String, String) -> Unit, kayitOlTiklandi: () -> Unit) {
    var kullaniciAdi = remember{ mutableStateOf("") }
    var ePosta = remember{ mutableStateOf("") }
    var parola = remember{ mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- LOGO ---
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

        Spacer(modifier = Modifier.padding(40.dp))

        // --- KULLANICI ADI  ---
        OutlinedTextField(
            value = kullaniciAdi.value,
            onValueChange = { kullaniciAdi.value = it },
            placeholder = {Text(stringResource(R.string.kullaniciAdi))},
            modifier = Modifier.fillMaxWidth().height(62.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- E-POSTA ---
        OutlinedTextField(
            value = ePosta.value,
            onValueChange = { ePosta.value = it },
            placeholder = { Text(stringResource(R.string.ePosta)) },
            modifier = Modifier.fillMaxWidth().height(62.dp),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // --- PAROLA ---
            OutlinedTextField(
                value = parola.value ,
                onValueChange = { parola.value = it },
                placeholder = { Text(stringResource(R.string.parola)) },
                modifier = Modifier.fillMaxWidth().height(62.dp),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (isPasswordVisible) R.drawable.visibility_on
                                else  R.drawable.visibility_off
                            ),
                            contentDescription = if (isPasswordVisible) "Şifreyi Gizle" else "Şifreyi Göster"
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )
        }


        Spacer(modifier = Modifier.height(10.dp))

        // --- KAYIT OL TEXT ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.halaKayitOlmadinMi),
                color = Color.Black,
                fontSize = 14.sp
            )
            TextButton(onClick = kayitOlTiklandi) {
                Text(
                    text = stringResource(R.string.kayitOl),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- GİRİŞ YAP BUTON ---
        Button(
            onClick = { girisYapTiklandi(kullaniciAdi.value, ePosta.value, parola.value) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = stringResource(R.string.girisYap),
                color = Color.White,
                fontSize = 16.sp
            )
        }

    }
}



@Preview(showBackground = true)
@Composable
fun previewGirisYapScreen() {
    IcimdekilerTheme {
        GirisYapScreen({_,_,_ ->}, {})
    }
}

