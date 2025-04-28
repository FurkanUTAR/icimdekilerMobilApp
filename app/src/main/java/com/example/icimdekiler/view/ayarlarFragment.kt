package com.example.icimdekiler.view

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat.recreate
import androidx.core.os.LocaleListCompat
import com.example.icimdekiler.R
import com.example.icimdekiler.databinding.FragmentAdminAnaSayfaBinding
import com.example.icimdekiler.databinding.FragmentAyarlarBinding
import java.util.Locale
import androidx.core.content.edit

class ayarlarFragment : Fragment() {

    private var _binding: FragmentAyarlarBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAyarlarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.temaRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.aydinlikRadioButton -> { // Türkçe seçildi
                    updateLanguage("tr")
                }
                R.id.koyuRadioButton -> { // İngilizce seçildi
                    updateLanguage("en")
                }
            }
        }
    }

    private fun updateLanguage(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)

        // Seçilen dili SharedPreferences ile kaydet
        val sharedPreferences = requireContext().getSharedPreferences("DilAyar", Context.MODE_PRIVATE)
        sharedPreferences.edit() { putString("secilenDil", languageCode) }
        //requireActivity().recreate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

