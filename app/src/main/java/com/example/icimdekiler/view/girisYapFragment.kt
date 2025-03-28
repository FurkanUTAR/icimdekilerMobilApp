package com.example.icimdekiler.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.icimdekiler.databinding.FragmentGirisYapBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import androidx.navigation.findNavController
import com.example.icimdekiler.R

class girisYapFragment : Fragment() {

    private var _binding: FragmentGirisYapBinding? = null
    private val binding get() = _binding!!

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
    ): View? {
        _binding = FragmentGirisYapBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

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

    private fun girisYap() {
        try {
            val ePosta = binding.ePostaText.text.toString().trim()
            val parola = binding.parolaText.text.toString().trim()

            if (ePosta.isNotEmpty() && parola.isNotEmpty()) {
                auth.signInWithEmailAndPassword(ePosta, parola)
                    .addOnCompleteListener { task ->
                        try {
                            if (task.isSuccessful) {
                                val guncelKullanici = auth.currentUser
                                if (guncelKullanici != null) {
                                    db.collection("kullaniciBilgileri")
                                        .whereEqualTo("ePosta", ePosta)
                                        .whereEqualTo("parola", parola)
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
                                                            val action = girisYapFragmentDirections.actionGirisYapFragmentToAdminAnaSayfaFragment()
                                                            navController?.navigate(action)
                                                            Toast.makeText(requireContext(), "${getString(R.string.hosgeldin)} Admin $kullaniciAdi", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            val action = girisYapFragmentDirections.actionGirisYapFragmentToKullaniciAnaSayfaFragment()
                                                            navController?.navigate(action)
                                                            Toast.makeText(requireContext(), "${getString(R.string.hosgeldin)} $kullaniciAdi", Toast.LENGTH_SHORT).show()
                                                        }
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
        _binding = null
    }
}