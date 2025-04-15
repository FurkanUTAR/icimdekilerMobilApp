package com.example.icimdekiler.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.icimdekiler.databinding.FragmentKayitOlBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import androidx.navigation.findNavController
import com.example.icimdekiler.R
import com.google.firebase.auth.actionCodeSettings

class kayitOlFragment : Fragment() {

    //Binding
    private var _binding: FragmentKayitOlBinding? = null
    private val binding get() = _binding!!

    //Firebase
    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    private val handler = Handler(Looper.getMainLooper())

    private val runnable = object : Runnable {
        override fun run() {
            kontrolEtVeYonlendir()
            handler.postDelayed(this, 10000) // 10 saniyede bir kontrol
        }
    }

    override fun onStart() {
        super.onStart()
        handler.post(runnable)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(runnable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKayitOlBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            binding.kayitOlButton.setOnClickListener {
                try {
                    kayitOl()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            binding.girisYapLabel.setOnClickListener {
                try {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()

        kontrolEtVeYonlendir()
    }

    fun kayitOl() {
        val kullaniciAdi = binding.kullaniciAdiText.text.toString().trim()
        val isimSoyisim = binding.isimSoyisimText.text.toString().trim()
        val ePosta = binding.ePostaText.text.toString().trim()
        val telNo = binding.telNoText.text.toString().trim()
        val parola = binding.parolaText.text.toString().trim()

        if (kullaniciAdi.isEmpty() || isimSoyisim.isEmpty() || ePosta.isEmpty() || telNo.isEmpty() || parola.isEmpty()) {
            Toast.makeText(requireContext(), R.string.lutfenBosAlanBirakmayiniz, Toast.LENGTH_LONG).show()
            return
        }

        if (parola.length < 6) {
            Toast.makeText(requireContext(), R.string.parolaEnAzAltiKarakterOlmali, Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("kullaniciBilgileri")
            .whereEqualTo("kullaniciAdi", kullaniciAdi)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    Toast.makeText(requireContext(), R.string.buKullaniciAdiZatenKullaniliyor, Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                Firebase.auth.createUserWithEmailAndPassword(ePosta, parola)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser

                            val kullaniciMap = hashMapOf(
                                "kullaniciAdi" to kullaniciAdi,
                                "isimSoyisim" to isimSoyisim,
                                "ePosta" to ePosta,
                                "telNo" to telNo,
                                "parola" to parola,
                                "isAdmin" to false,
                                "kullaniciUID" to (user?.uid ?: "")
                            )

                            if (user != null){
                                db.collection("kullaniciBilgileri")
                                    .document(user.uid)
                                    .set(kullaniciMap)
                                    .addOnSuccessListener {
                                        // Doğrulama maili gönder
                                        user.sendEmailVerification()
                                            .addOnCompleteListener { verifyTask ->
                                                if (verifyTask.isSuccessful) {
                                                    Toast.makeText(requireContext(), "Doğrulama e-postası gönderildi. Lütfen onaylayın!", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(requireContext(), "Doğrulama e-postası gönderilemedi: ${verifyTask.exception?.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                    }.addOnFailureListener { exception ->
                                        Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
                                    }
                            }
                        } else {
                            Toast.makeText(requireContext(), "Kayıt başarısız: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Hata: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    fun kontrolEtVeYonlendir() {
        val user = Firebase.auth.currentUser
        user?.reload()?.addOnSuccessListener {
            if (user.isEmailVerified) {
                Toast.makeText(requireContext(), "Doğrulama tamamlandı! Ana sayfaya yönlendiriliyorsunuz.", Toast.LENGTH_SHORT).show()
                val action = kayitOlFragmentDirections.actionKayitOlFragmentToKullaniciAnaSayfaFragment()
                requireView().findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Lütfen e-postanızı doğrulayın!", Toast.LENGTH_LONG).show()
            }
        }?.addOnFailureListener {
            Toast.makeText(requireContext(), "Doğrulama kontrolü sırasında hata: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }


    /*
    fun kayitOl() {
        val kullaniciAdi = binding.kullaniciAdiText.text.toString().trim()
        val isimSoyisim = binding.isimSoyisimText.text.toString().trim()
        val ePosta = binding.ePostaText.text.toString().trim()
        val telNo = binding.telNoText.text.toString().trim()
        val sifre = "GeçiciBirSifre123"  // Kullanıcıdan şifre alman lazım!

        if (kullaniciAdi.isEmpty() || isimSoyisim.isEmpty() || ePosta.isEmpty() || telNo.isEmpty()) {
            Toast.makeText(requireContext(), R.string.lutfenBosAlanBirakmayiniz, Toast.LENGTH_LONG).show()
            return
        }

        // Kullanıcı adı kontrolü
        db.collection("kullaniciBilgileri")
            .whereEqualTo("kullaniciAdi", kullaniciAdi)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    Toast.makeText(requireContext(), R.string.buKullaniciAdiZatenKullaniliyor, Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                // Firebase Auth'a kullanıcı kaydı
                Firebase.auth.createUserWithEmailAndPassword(ePosta, sifre)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = Firebase.auth.currentUser

                            // Firestore'a kayıt
                            val kullaniciMap = hashMapOf(
                                "kullaniciAdi" to kullaniciAdi,
                                "isimSoyisim" to isimSoyisim,
                                "ePosta" to ePosta,
                                "telNo" to telNo,
                                "isAdmin" to false
                            )

                            user?.let {
                                db.collection("kullaniciBilgileri").document(it.uid).set(kullaniciMap)
                            }

                            // Doğrulama maili gönder
                            user?.sendEmailVerification()
                                ?.addOnCompleteListener { verifyTask ->
                                    if (verifyTask.isSuccessful) {
                                        Toast.makeText(requireContext(), "Doğrulama e-postası gönderildi.", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(requireContext(), "Doğrulama e-postası gönderilemedi: ${verifyTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            Toast.makeText(requireContext(), "Kayıt başarısız: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Hata: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }



    fun kayitOl() {
        try {
            val kullaniciAdi = binding.kullaniciAdiText.text.toString().trim()
            val isimSoyisim = binding.isimSoyisimText.text.toString().trim()
            val ePosta = binding.ePostaText.text.toString().trim()
            val telNo = binding.telNoText.text.toString().trim()
            val parola = binding.parolaText.text.toString().trim()

            // Boş alan kontrolü yap
            if (kullaniciAdi.isNotEmpty() && isimSoyisim.isNotEmpty() && ePosta.isNotEmpty() && telNo.isNotEmpty() && parola.isNotEmpty()) {
                if (parola.length < 6) Toast.makeText(requireContext(), R.string.parolaEnAzAltiKarakterOlmali, Toast.LENGTH_SHORT).show()
                else {
                    // Aynı kullanıcı adına sahip başka bir kullanıcı var mı kontrol et
                    db.collection("kullaniciBilgileri")
                        .whereEqualTo("kullaniciAdi", kullaniciAdi)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            try {
                                if (querySnapshot.isEmpty) {
                                    // Aynı kullanıcı adı yok, kayıt işlemini başlat
                                    auth.createUserWithEmailAndPassword(ePosta, parola)
                                        .addOnCompleteListener { task ->
                                            try {
                                                if (task.isSuccessful) {

                                                    val actionCodeSettings = actionCodeSettings {
                                                        url = "https://icimdekiler-8a95b.firebaseapp.com"  // "https://" eklenmeli
                                                        handleCodeInApp = true
                                                        setAndroidPackageName(
                                                            "com.example.icimdekiler",  // Gerçek uygulama package adınızla değiştirin
                                                            true,  // installIfNotAvailable
                                                            "12"   // minimumVersion (opsiyonel)
                                                        )
                                                    }

                                                    Firebase.auth.sendSignInLinkToEmail(ePosta, actionCodeSettings)
                                                        .addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                Toast.makeText(requireContext(),"E-Posta Gönderildi", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }

                                                    val guncelKullanici = auth.currentUser
                                                    if (guncelKullanici != null) {
                                                        // Kullanıcı bilgilerini bir Map'e koy
                                                        val kullaniciMap = hashMapOf<String, Any>()
                                                        kullaniciMap["kullaniciAdi"] = kullaniciAdi
                                                        kullaniciMap["isimSoyisim"] = isimSoyisim
                                                        kullaniciMap["ePosta"] = ePosta
                                                        kullaniciMap["telNo"] = telNo
                                                        kullaniciMap["parola"] = parola
                                                        kullaniciMap["isAdmin"] = false // Kullanıcı admin değil
                                                        kullaniciMap["kullaniciUID"] = guncelKullanici.uid // Kullanıcı UID'sini ekle

                                                        // Kullanıcı bilgilerini Firestore'a kaydet
                                                        db.collection("kullaniciBilgileri")
                                                            .add(kullaniciMap) // Firestore'a ekle
                                                            .addOnSuccessListener {
                                                                try {
                                                                    // Kullanıcı başarıyla kaydedildiyse, kullanıcı anasayfasına yönlendir
                                                                    val action = kayitOlFragmentDirections.actionKayitOlFragmentToKullaniciAnaSayfaFragment()
                                                                    requireView().findNavController().navigate(action)
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
                                                    }
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
                                    try {
                                        Toast.makeText(requireContext(), R.string.buKullaniciAdiZatenKullaniliyor, Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
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
                }
            } else {
                try {
                    Toast.makeText(requireContext(), R.string.lutfenBosAlanBirakmayiniz, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

     */

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}