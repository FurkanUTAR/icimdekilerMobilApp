
package com.furkanutar.icimdekiler.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkanutar.icimdekiler.model.Urunler
import com.furkanutar.icimdekiler.ui.theme.IcimdekilerTheme
import com.furkanutar.icimdekiler.R

@Composable
fun AdminTumUrunlerScreen(
    urunListesi: List<Urunler>,
    onSearchClick: (String) -> Unit,
    onUrunClick: (Urunler) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Arama Çubuğu
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text(stringResource(R.string.urunAra)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    IconButton(onClick = { onSearchClick(searchText) }) {
                        Icon(painterResource(id = R.drawable.search), contentDescription = null)
                    }
                },
                singleLine = true
            )

            Text(
                text = " ${stringResource(R.string.urunSayisi)} ${urunListesi.size}",
                modifier = Modifier.align(Alignment.End).padding(vertical = 8.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(urunListesi) { urun ->
                    UrunKarti(urun = urun, onClick = { onUrunClick(urun) })
                }
            }
        }
    }


}

@Preview(showBackground = true)
@Composable
fun AdminTumUrunlerPreview(){
    IcimdekilerTheme() {

    }
}