package com.example.icimdekiler.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import com.example.icimdekiler.databinding.FragmentGirisYapBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.icimdekiler.R
import com.example.icimdekiler.ui.GirisYapScreen
import com.example.icimdekiler.ui.theme.IcimdekilerTheme

class girisYapFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    var kullaniciAdi:String = ""
    var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // XML'i (binding) tamamen devreden çıkarıp ComposeView kullanıyoruz
        return ComposeView(requireContext()).apply {
            setContent {
                IcimdekilerTheme() {
                    // Senin oluşturduğun tasarım fonksiyonu (Resimdeki isme göre)
                    GirisYapScreen(
                        girisYapTiklandi = { kullaniciAdi, ePosta, parola ->
                            // Compose'dan gelen verilerle login fonksiyonunu çağırıyoruz
                            girisYap(ePosta, parola)
                        },
                        kayitOlTiklandi = {
                            // Navigasyon işlemi
                            try {
                                val action = girisYapFragmentDirections.actionGirisYapFragmentToKayitOlFragment()
                                findNavController().navigate(action)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                }
            }
        }
    }
/*
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.kayitOlLabel.setOnClickListener {
            try {
                val action = girisYapFragmentDirections.actionGirisYapFragmentToKayitOlFragment()
                requireView().findNavController().navigate(action)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.girisYapButton.setOnClickListener {
            try {
                girisYap()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

 */

    private fun girisYap(ePosta: String, parola: String) {
        try {
            if (ePosta.isNotEmpty() && parola.isNotEmpty()) {
                auth.signInWithEmailAndPassword(ePosta, parola)
                    .addOnCompleteListener { task ->
                        try {
                            if (task.isSuccessful) {
                                val guncelKullanici = auth.currentUser
                                if (guncelKullanici != null) {
                                    db.collection("kullaniciBilgileri")
                                        .whereEqualTo("kullaniciUID", guncelKullanici.uid)
                                        .get()
                                        .addOnSuccessListener { documents ->
                                            try {
                                                if (!documents.isEmpty) {
                                                    val kullanici = documents.documents.first()
                                                    isAdmin = kullanici.getBoolean("isAdmin") ?: false
                                                    kullaniciAdi = kullanici.getString("kullaniciAdi") ?: "Bilinmiyor"

                                                    if (isAdded) {
                                                        val navController = view?.findNavController()
                                                        if (isAdmin) {
                                                            findNavController().navigate(girisYapFragmentDirections.actionGirisYapFragmentToAdminAnaSayfaFragment())
                                                        } else {
                                                            findNavController().navigate(girisYapFragmentDirections.actionGirisYapFragmentToKullaniciAnaSayfaFragment())
                                                        }
                                                        Toast.makeText(requireContext(), "Hoş geldin $kullaniciAdi", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    Toast.makeText(requireContext(), R.string.kullaniciBilgileriYanlis, Toast.LENGTH_LONG).show()
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }.addOnFailureListener { exception ->
                                            try {
                                                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                } else {
                                    Toast.makeText(requireContext(), R.string.kullaniciBulunamadi, Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), R.string.girisBasarisizEpostaVeyaParolaHatali, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), R.string.lutfenBosAlanBirakmayiniz, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}