package com.example.icimdekiler.view

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
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

        //Tema rengini hatırlama kodları
        val sharedPreferences = getSharedPreferences("TemaAyar", Context.MODE_PRIVATE)
        val secilenTema = sharedPreferences.getInt("secilenTema", AppCompatDelegate.MODE_NIGHT_NO)

        AppCompatDelegate.setDefaultNightMode(secilenTema)

        applySavedLanguage() // Önce dili uygula

        setContentView(R.layout.activity_main) // Sonra layout'u yükle

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE
                setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
            }
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        }

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