package com.example.icimdekiler.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import com.example.icimdekiler.R
import com.example.icimdekiler.databinding.FragmentHesapAyarlariBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.toString

class hesapAyarlariFragment : Fragment() {

    private var _binding: FragmentHesapAyarlariBinding? = null
    private val binding get() = _binding!!

    // Firebase
    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore
    val storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHesapAyarlariBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.hesabiSilButton.setOnClickListener {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle("Hesabı silmek istediğinizden emin misin?")
            alert.setPositiveButton(R.string.evet) { dialog, value -> hesabiSil() }
            alert.setNegativeButton(R.string.hayir, null).show()
        }

        binding.guncelleButton.setOnClickListener {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle("Hesap bilgilerini güncellemek istediğinizden emin misin?")
            alert.setPositiveButton(R.string.evet) { dialog, value -> guncelle() }
            alert.setNegativeButton(R.string.hayir, null).show()
        }
    }

    private fun guncelle(){
        val kullaniciAdi = binding.kullaniciAdiText.text.toString().trim()
        val isimSoyisim = binding.isimSoyisimText.text.toString().trim()
        val ePosta = binding.ePostaText.text.toString().trim()
        val telNo = binding.telNoText.text.toString().trim()
        val parola = binding.parolaText.text.toString().trim()

        if (kullaniciAdi.isNotEmpty() && isimSoyisim.isNotEmpty() && ePosta.isNotEmpty() && telNo.isNotEmpty() && parola.isNotEmpty()) {
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val kullaniciUID = currentUser.uid

                // Firestore'da kullaniciBilgileri koleksiyonunda bu UID'ye sahip belgeyi bul
                db.collection("kullaniciBilgileri")
                    .whereEqualTo("kullaniciUID", kullaniciUID)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val belge = documents.documents.first()
                            val documentId = belge.id

                            val guncellenenKullaniciMap: MutableMap<String, Any> = mutableMapOf(
                                "kullaniciAdi" to kullaniciAdi,
                                "isimSoyisim" to isimSoyisim,
                                "ePosta" to ePosta,
                                "telNo" to telNo,
                                "parola" to parola
                            )

                            // Firestore'daki belgeyi sil
                            db.collection("kullaniciBilgileri").document(documentId)
                                .update(guncellenenKullaniciMap)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Başarıyla güncellindi", Toast.LENGTH_SHORT).show()
                                }.addOnFailureListener { e ->
                                    println(e.localizedMessage)
                                }
                        } else {
                            println("Kullanıcı bulunamadı.")
                        }
                    }.addOnFailureListener { e ->
                        println(e.localizedMessage)
                    }
            }
        } else Toast.makeText(requireContext(), R.string.lutfenBosAlanBirakmayiniz, Toast.LENGTH_SHORT).show()
    }

    private fun hesabiSil() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val kullaniciUID = currentUser.uid

            // Firestore'da kullaniciBilgileri koleksiyonunda bu UID'ye sahip belgeyi bul
            db.collection("kullaniciBilgileri")
                .whereEqualTo("kullaniciUID", kullaniciUID)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val belge = documents.documents.first()
                        val documentId = belge.id

                        // Firestore'daki belgeyi sil
                        db.collection("kullaniciBilgileri").document(documentId)
                            .delete()
                            .addOnSuccessListener {
                                // Firebase Auth hesabını da sil
                                currentUser.delete()
                                    .addOnSuccessListener {
                                        // Kullanıcı başarıyla silindi, giriş ekranına yönlendir
                                        if (isAdded && view != null) {
                                            val action = hesapAyarlariFragmentDirections.actionHesapAyarlariFragmentToSplashFragment()
                                            requireView().findNavController().navigate(action)
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        // Auth silinemedi
                                        println("Auth silinemedi: ${e.localizedMessage}")
                                    }
                            }
                            .addOnFailureListener { e ->
                                println("Firestore belgesi silinemedi: ${e.localizedMessage}")
                            }
                    } else {
                        println("Kullanıcı belgesi bulunamadı.")
                    }
                }
                .addOnFailureListener { e ->
                    println("Belge sorgulama hatası: ${e.localizedMessage}")
                }
        }
    }
}