package com.example.icimdekiler.view

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.icimdekiler.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applySavedLanguage() // Önce dili uygula

        setContentView(R.layout.activity_main) // Sonra layout'u yükle

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun applySavedLanguage() {
        val sharedPreferences = getSharedPreferences("DilAyar", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("secilenDil", "tr") // default Türkçe
        val appLocale = LocaleListCompat.forLanguageTags(languageCode!!)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}