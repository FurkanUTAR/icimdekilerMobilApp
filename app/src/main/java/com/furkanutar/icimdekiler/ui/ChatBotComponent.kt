package com.furkanutar.icimdekiler.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkanutar.icimdekiler.R
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

data class Message(val text: String, val isUser: Boolean)

@Composable
fun ChatBotFab(modifier: Modifier = Modifier) {
    var isChatOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        if (!isChatOpen) {
            FloatingActionButton(
                onClick = { isChatOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.network_intelligence),
                    contentDescription = "AI Chat",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isChatOpen,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            ChatWindow(onClose = { isChatOpen = false })
        }
    }
}

@Composable
fun ChatWindow(onClose: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>(
        Message("Merhaba! Ben İçimdekiler asistanı. Uygulama hakkında her şeyi bana sorabilirsin.", false)
    ) }
    val coroutineScope = rememberCoroutineScope()

    // Gemini Model Kurulumu - Hata düzeltildi: modelName güncellendi
    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-2.5-flash", // Hata düzeltildi: Güncel model sürümü kullanıldı
            apiKey = "AIzaSyBb_QjxmzMa0ZrREzZwGEwkYuLXM4ZozMk", 
            systemInstruction = content {
                text("Sen 'İçimdekiler' uygulamasının yardımcı asistanısın. " +
                        "Bu uygulama, kullanıcıların gıda ürünlerinin barkodlarını tarayarak veya isimle aratarak içeriklerini öğrenmelerini sağlar. " +
                        "Kullanıcılara uygulama içi konularda yardımcı ol. İşte kullanım rehberi:\n" +
                        "1. Ayarlar, Hesap Ayarları, Dil veya Tema değişimi için: Sol üstteki 3 çizgi (hamburger menü) ikonuna basılmalıdır.\n" +
                        "2. Ürün okutmak için: Ana sayfadaki barkod/kamera simgesine dokunulmalıdır.\n" +
                        "3. Arama yapmak için: Ana sayfadaki arama kutusuna ürün ismi yazılabilir.\n" +
                        "Kullanıcı 'ayarlara nasıl giderim?' gibi sorular sorarsa bu rehbere göre 'Sol üstteki 3 çizgiye tıkla...' gibi net, kısa ve anlaşılır cevaplar ver. Kibar ve yardımcı ol.")
            }
        )
    }

    // Sohbet hafızası (Context) için Chat başlatıyoruz
    val chat = remember {
        generativeModel.startChat()
    }

    Card(
        modifier = Modifier
            .padding(16.dp)
            .width(300.dp)
            .height(450.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "İçimdekiler AI Asistan",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }

            // Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    ChatBubble(message)
                }
            }

            // Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Sorunuzu yazın...", fontSize = 14.sp) },
                    maxLines = 2,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val userMsg = inputText
                            messages.add(Message(userMsg, true))
                            inputText = ""
                            
                            coroutineScope.launch {
                                try {
                                    // Hafızalı sohbet (Chat) üzerinden mesaj gönderimi
                                    val response = chat.sendMessage(userMsg)
                                    messages.add(Message(response.text ?: "Üzgünüm, şu an yanıt veremiyorum.", false))
                                } catch (e: Exception) {
                                    messages.add(Message("Bir hata oluştu: ${e.localizedMessage}", false))
                                }
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Gönder", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val color = if (message.isUser) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0)
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            color = color,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (message.isUser) 12.dp else 0.dp,
                bottomEnd = if (message.isUser) 0.dp else 12.dp
            ),
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                modifier = Modifier.padding(8.dp),
                fontSize = 14.sp
            )
        }
    }
}
