package com.furkanutar.icimdekiler.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.LocaleListCompat
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.databinding.FragmentAyarlarBinding
import java.util.Locale
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.furkanutar.icimdekiler.ui.AyarlarScreen
import com.furkanutar.icimdekiler.ui.SecimBottomSheet
import com.furkanutar.icimdekiler.ui.TemaBottomSheet
import com.furkanutar.icimdekiler.ui.theme.IcimdekilerTheme
import com.google.android.material.bottomsheet.BottomSheetDialog

class ayarlarFragment : Fragment() {

    private var currentThemeMode by mutableIntStateOf(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    // Dil takibi için state
    private var currentLanguage by mutableStateOf("varsayilan")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                IcimdekilerTheme {
                    var showThemeSheet by remember { mutableStateOf(false) }
                    var showLanguageSheet by remember { mutableStateOf(false) }

                    // Başlangıç değerlerini yükle
                    val themePrefs = remember { requireContext().getSharedPreferences("TemaAyar", Context.MODE_PRIVATE) }
                    val langPrefs = remember { requireContext().getSharedPreferences("DilAyar", Context.MODE_PRIVATE) }

                    currentThemeMode = themePrefs.getInt("secilenTema", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    currentLanguage = langPrefs.getString("secilenDil", "varsayilan") ?: "varsayilan"

                    AyarlarScreen(
                        onHesapClick = { hesapAyarlari() },
                        onTemaClick = { showThemeSheet = true },
                        onDilClick = { showLanguageSheet = true }
                    )

                    // TEMA SEÇİMİ
                    if (showThemeSheet) {
                        val temaMetinleri = listOf(
                            getString(R.string.varsayilan),
                            getString(R.string.karanlik),
                            getString(R.string.aydinlik)
                        )
                        val suAnkiMetin = when(currentThemeMode) {
                            AppCompatDelegate.MODE_NIGHT_YES -> getString(R.string.karanlik)
                            AppCompatDelegate.MODE_NIGHT_NO -> getString(R.string.aydinlik)
                            else -> getString(R.string.varsayilan)
                        }

                        SecimBottomSheet(
                            baslik = getString(R.string.temaDegistirme),
                            secenekler = temaMetinleri,
                            suAnkiSecili = suAnkiMetin,
                            onDismiss = { showThemeSheet = false },
                            onSecildi = { secilen ->
                                val mode = when (secilen) {
                                    getString(R.string.karanlik) -> AppCompatDelegate.MODE_NIGHT_YES
                                    getString(R.string.aydinlik) -> AppCompatDelegate.MODE_NIGHT_NO
                                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                                }
                                applyTheme(mode)
                            }
                        )
                    }

                    // DİL SEÇİMİ
                    if (showLanguageSheet) {
                        val dilMetinleri = listOf(
                            getString(R.string.varsayilan),
                            getString(R.string.turkce),
                            getString(R.string.ingilizce)
                        )
                        val suAnkiDilMetni = when(currentLanguage) {
                            "tr" -> getString(R.string.turkce)
                            "en" -> getString(R.string.ingilizce)
                            else -> getString(R.string.varsayilan)
                        }

                        SecimBottomSheet(
                            baslik = getString(R.string.dil),
                            secenekler = dilMetinleri,
                            suAnkiSecili = suAnkiDilMetni,
                            onDismiss = { showLanguageSheet = false },
                            onSecildi = { secilen ->
                                val code = when (secilen) {
                                    getString(R.string.turkce) -> "tr"
                                    getString(R.string.ingilizce) -> "en"
                                    else -> "varsayilan"
                                }
                                updateLanguage(code)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun updateLanguage(languageCode: String) {
        val locale: LocaleListCompat = if (languageCode == "varsayilan") {
            LocaleListCompat.getEmptyLocaleList() // Sistem diline döner
        } else {
            LocaleListCompat.forLanguageTags(languageCode)
        }

        AppCompatDelegate.setApplicationLocales(locale)

        requireContext().getSharedPreferences("DilAyar", Context.MODE_PRIVATE).edit {
            putString("secilenDil", languageCode)
        }
        currentLanguage = languageCode
    }

    private fun applyTheme(themeMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)
        requireContext().getSharedPreferences("TemaAyar", Context.MODE_PRIVATE).edit {
            putInt("secilenTema", themeMode)
        }
        currentThemeMode = themeMode
    }

    private fun applySavedTheme() {
        val sharedPreferences = requireContext().getSharedPreferences("TemaAyar", Context.MODE_PRIVATE)
        val secilenTema = sharedPreferences.getInt("secilenTema", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(secilenTema)
    }

    private fun hesapAyarlari() {
//        val action = ayarlarFragmentDirections.actionAyarlarFragmentToHesapAyarlariFragment()
//        requireView().findNavController().navigate(action)
        Toast.makeText(requireContext(), R.string.gelistirmeAsamasında, Toast.LENGTH_SHORT).show()
    }
}