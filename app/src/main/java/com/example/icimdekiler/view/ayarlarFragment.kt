package com.example.icimdekiler.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.icimdekiler.R
import com.example.icimdekiler.databinding.FragmentAyarlarBinding
import java.util.Locale
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog

class ayarlarFragment : Fragment() {

    private var _binding: FragmentAyarlarBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySavedTheme()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAyarlarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNavigation()
        setupLanguageSelection()
        setupThemeSelection()
    }

    private fun setupNavigation() {
        binding.hesapButton.setOnClickListener {
            val action = ayarlarFragmentDirections.actionAyarlarFragmentToHesapAyarlariFragment()
            requireView().findNavController().navigate(action)
        }
    }

    private fun setupLanguageSelection() {
        binding.dilButton.setOnClickListener {
            showLanguageSelectionDialog()
        }
    }
    private fun showLanguageSelectionDialog(){
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_choose_language, null)

        val turkceRadioButton = dialogView.findViewById<RadioButton>(R.id.turkceRadioButton)
        val ingilizceRadioButton = dialogView.findViewById<RadioButton>(R.id.ingilizceRadioButton)
        val varsayilanRadioButton = dialogView.findViewById<RadioButton>(R.id.varsayilanRadioButton)

        val sharedPreferences = requireContext().getSharedPreferences("DilAyar", Context.MODE_PRIVATE)
        val secilenDil = sharedPreferences.getString("secilenDil", "varsayilan")

        when (secilenDil) {
            "tr" -> turkceRadioButton.isChecked = true
            "en" -> ingilizceRadioButton.isChecked = true
            "varsayilan" -> varsayilanRadioButton.isChecked = true
            else -> {
                val systemLanguage = Locale.getDefault().language
                if (systemLanguage == "tr") {
                    turkceRadioButton.isChecked = true
                } else {
                    ingilizceRadioButton.isChecked = true
                }
            }
        }

        turkceRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateLanguage("tr")
                dialog.dismiss()
            }
        }

        ingilizceRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateLanguage("en")
                dialog.dismiss()
            }
        }

        varsayilanRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateLanguage("varsayilan")
                dialog.dismiss()
            }
        }

        dialog.setContentView(dialogView)
        dialog.show()
    }

    private fun setupThemeSelection() {
        binding.temaButton.setOnClickListener {
            showThemeSelectionDialog()
        }
    }
    private fun showThemeSelectionDialog(){
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_choose_theme, null)

        val varsayilanRadioButton = dialogView.findViewById<RadioButton>(R.id.varsayilanRadioButton)
        val karanlikRadioButton = dialogView.findViewById<RadioButton>(R.id.karanlikRadioButton)
        val aydinlikRadioButton = dialogView.findViewById<RadioButton>(R.id.aydinlikRadioButton)

        val sharedPreferences = requireContext().getSharedPreferences("TemaAyar", Context.MODE_PRIVATE)
        val secilenTema = sharedPreferences.getInt("secilenTema", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        when (secilenTema) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> varsayilanRadioButton.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> karanlikRadioButton.isChecked = true
            AppCompatDelegate.MODE_NIGHT_NO -> aydinlikRadioButton.isChecked = true
        }

        varsayilanRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                dialog.dismiss()
            }
        }

        karanlikRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyTheme(AppCompatDelegate.MODE_NIGHT_YES)
                dialog.dismiss()
            }
        }

        aydinlikRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                applyTheme(AppCompatDelegate.MODE_NIGHT_NO)
                dialog.dismiss()
            }
        }

        dialog.setContentView(dialogView)
        dialog.show()
    }

    private fun updateLanguage(languageCode: String) {
        val sharedPreferences = requireContext().getSharedPreferences("DilAyar", Context.MODE_PRIVATE)
        val locale: LocaleListCompat = if (languageCode == "varsayilan") {
            val systemLanguage = Locale.getDefault().language
            LocaleListCompat.forLanguageTags(systemLanguage)
        } else {
            LocaleListCompat.forLanguageTags(languageCode)
        }
        AppCompatDelegate.setApplicationLocales(locale)
        sharedPreferences.edit {
            putString("secilenDil", languageCode)
        }
    }

    private fun applyTheme(themeMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)
        val sharedPreferences = requireContext().getSharedPreferences("TemaAyar", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putInt("secilenTema", themeMode)
        }
    }

    private fun applySavedTheme() {
        val sharedPreferences = requireContext().getSharedPreferences("TemaAyar", Context.MODE_PRIVATE)
        // Kayıtlı temayı al, yoksa varsayılan olarak "aydınlık" kullan
        val secilenTema = sharedPreferences.getInt("secilenTema", AppCompatDelegate.MODE_NIGHT_NO)

        // Kaydedilen tema sistem varsayılanı değilse, hemen uygula
        if (secilenTema != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            AppCompatDelegate.setDefaultNightMode(secilenTema)
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}