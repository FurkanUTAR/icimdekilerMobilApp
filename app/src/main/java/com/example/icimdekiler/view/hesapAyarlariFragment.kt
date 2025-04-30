package com.example.icimdekiler.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import com.example.icimdekiler.R
import com.example.icimdekiler.databinding.FragmentHesapAyarlariBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

//        binding.guncelleButton.setOnClickListener {
//            val alert = AlertDialog.Builder(requireContext())
//            alert.setTitle("Hesap bilgilerini güncellemek istediğinizden emin misin?")
//            alert.setPositiveButton(R.string.evet) { dialog, value -> guncelle() }
//            alert.setNegativeButton(R.string.hayir, null).show()
//        }

        binding.parolaDegistirButton.setOnClickListener {
            parolaGuncelle()
        }
    }

    private fun parolaGuncelle() {
        val currentUser = auth.currentUser
        var parola = ""
        var kullaniciUID = ""

        if (currentUser != null) {
            kullaniciUID = currentUser.uid

            db.collection("kullaniciBilgileri")
                .whereEqualTo("kullaniciUID", kullaniciUID)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.documents.first()
                        parola = document.getString("parola").toString()

                        // Dialog'u parola çekildikten sonra açıyoruz
                        showPasswordDialog(currentUser, parola, kullaniciUID)
                    } else {
                        Toast.makeText(requireContext(), R.string.belgeBulunamadi, Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    println(e.localizedMessage)
                }
        }
    }

    private fun showPasswordDialog(currentUser: FirebaseUser, eskiParola: String, kullaniciUID: String) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val guncelParolaEditText = view.findViewById<EditText>(R.id.guncelParolaText)
        val yeniParolaEditText = view.findViewById<EditText>(R.id.yeniParolaText)
        val parolaDogrulaEditText = view.findViewById<EditText>(R.id.parolaDogrulaText)
        val guncelleButton = view.findViewById<Button>(R.id.parolaGuncelleButton)

        // Başlangıçta disable
        yeniParolaEditText.isEnabled = false
        parolaDogrulaEditText.isEnabled = false
        guncelleButton.isEnabled = false

        // Her yazı değiştiğinde kontrol et
        guncelParolaEditText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val girilenParola = s.toString()
                val eslesme = girilenParola == eskiParola
                yeniParolaEditText.isEnabled = eslesme
                parolaDogrulaEditText.isEnabled = eslesme
                guncelleButton.isEnabled = eslesme
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        guncelleButton.setOnClickListener {
            val yeniParola = yeniParolaEditText.text.toString()
            val parolaDogrula = parolaDogrulaEditText.text.toString()

            if (yeniParola.isNotEmpty() && yeniParola == parolaDogrula) {
                // Önce Firebase Authentication şifresini güncelle
                currentUser.updatePassword(yeniParola)
                    .addOnSuccessListener {
                        // Firestore'da da güncelle
                        db.collection("kullaniciBilgileri")
                            .whereEqualTo("kullaniciUID", kullaniciUID)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (!documents.isEmpty) {
                                    val belge = documents.documents.first()
                                    val documentId = belge.id

                                    val guncellenenKullaniciMap: MutableMap<String, Any> = mutableMapOf(
                                        "parola" to yeniParola
                                    )

                                    db.collection("kullaniciBilgileri").document(documentId)
                                        .update(guncellenenKullaniciMap)
                                        .addOnSuccessListener {
                                            Toast.makeText(requireContext(), R.string.parolabaşarıylagüncellendi, Toast.LENGTH_SHORT).show()
                                            dialog.dismiss()
                                        }.addOnFailureListener { e ->
                                            println("Firestore güncelleme hatası: ${e.localizedMessage}")
                                        }
                                }
                            }
                    }.addOnFailureListener { e ->
                        println("Authentication parola güncelleme hatası: ${e.localizedMessage}")
                        Toast.makeText(requireContext(), "Authentication hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), R.string.yeniparolalareşleşmiyor, Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }








    private fun guncelle(){
        val kullaniciAdi = binding.kullaniciAdiText.text.toString().trim()
        val isimSoyisim = binding.isimSoyisimText.text.toString().trim()
        val ePosta = binding.ePostaText.text.toString().trim()
        val telNo = binding.telNoText.text.toString().trim()

        if (kullaniciAdi.isNotEmpty() && isimSoyisim.isNotEmpty() && ePosta.isNotEmpty() && telNo.isNotEmpty()) {
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