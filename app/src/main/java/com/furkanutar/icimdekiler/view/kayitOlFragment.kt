package com.furkanutar.icimdekiler.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.ui.KayitOlScreen
import com.furkanutar.icimdekiler.ui.theme.IcimdekilerTheme

class kayitOlFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    private val handler = Handler(Looper.getMainLooper())
    private var isRedirected = false // ✅ eklendi: yönlendirme kontrolü

    private var tempKullaniciBilgileri: Map<String, String>? = null

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
    ): View {
        return ComposeView(requireContext()).apply {
            setContent{
                IcimdekilerTheme() {
                    KayitOlScreen(
                        kayitOlTiklandi = {kullaniciAdi,isimSoyisim,ePosta,telNo,parola ->

                            tempKullaniciBilgileri = mapOf(
                                "kullaniciAdi" to kullaniciAdi,
                                "isimSoyisim" to isimSoyisim,
                                "telNo" to telNo,
                                "parola" to parola
                            )

                            kayitOl(kullaniciAdi,isimSoyisim,ePosta,telNo,parola)
                        },
                        girisYapTiklandi = {
                            try {
                                findNavController().popBackStack()
                            } catch (e: Exception){
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

        binding.kayitOlButton.setOnClickListener {
            kayitOl()
        }

        binding.girisYapLabel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
    */

    override fun onResume() {
        super.onResume()
       kontrolEtVeYonlendir()
    }

    fun kayitOl(kullaniciAdi: String, isimSoyisim: String, ePosta: String, telNo: String, parola:String) {

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

                auth.createUserWithEmailAndPassword(ePosta, parola)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            user?.sendEmailVerification()
                                ?.addOnCompleteListener { verifyTask ->
                                    if (verifyTask.isSuccessful) {
                                        Toast.makeText(requireContext(), R.string.lutfenePostaniziDogrulayin, Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(requireContext(), "Doğrulama e-postası gönderilemedi: ${verifyTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_LONG).show()
                        }
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_LONG).show()
            }
    }

    fun kontrolEtVeYonlendir() {
        if (isRedirected) return

        val user = Firebase.auth.currentUser
        user?.reload()?.addOnSuccessListener {
            if (user.isEmailVerified && tempKullaniciBilgileri != null) {
                val kullaniciMap = hashMapOf(
                    "kullaniciAdi" to tempKullaniciBilgileri!!["kullaniciAdi"]?.trim(),
                    "isimSoyisim" to tempKullaniciBilgileri!!["isimSoyisim"]?.trim(),
                    "ePosta" to user.email,
                    "telNo" to tempKullaniciBilgileri!!["telNo"]?.trim(),
                    "parola" to tempKullaniciBilgileri!!["parola"]?.trim(),
                    "isAdmin" to false,
                    "kullaniciUID" to user.uid
                )

                db.collection("kullaniciBilgileri")
                    .document(user.uid)
                    .set(kullaniciMap)
                    .addOnSuccessListener {
                        if (!isRedirected) {
                            isRedirected = true // ✅ yönlendirildi
                            handler.removeCallbacks(runnable) // ✅ handler iptal
                            Toast.makeText(requireContext(), R.string.dogrulamaTamamlandiAnaSayfayaYonlendiriliyorsunuz, Toast.LENGTH_SHORT).show()
                            val action = kayitOlFragmentDirections.actionKayitOlFragmentToKullaniciAnaSayfaFragment()
                            findNavController().navigate(action)
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(requireContext(), R.string.lutfenePostaniziDogrulayin, Toast.LENGTH_LONG).show()
            }
        }?.addOnFailureListener {
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable) // ✅ ek güvenlik için handler iptal
    }
}
