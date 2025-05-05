package com.example.icimdekiler.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import androidx.navigation.fragment.findNavController
import com.example.icimdekiler.R

class kayitOlFragment : Fragment() {

    private var _binding: FragmentKayitOlBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    private val handler = Handler(Looper.getMainLooper())
    private var isRedirected = false // ✅ eklendi: yönlendirme kontrolü

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.kayitOlButton.setOnClickListener {
            kayitOl()
        }

        binding.girisYapLabel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
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
        if (isRedirected) {
            Log.d("KayitOlFragment", "Zaten yönlendirildi, tekrar kontrol etmiyor.")
            return
        }

        val user = Firebase.auth.currentUser
        user?.reload()?.addOnSuccessListener {
            if (user.isEmailVerified) {
                val kullaniciMap = hashMapOf(
                    "kullaniciAdi" to binding.kullaniciAdiText.text.toString().trim(),
                    "isimSoyisim" to binding.isimSoyisimText.text.toString().trim(),
                    "ePosta" to user.email,
                    "telNo" to binding.telNoText.text.toString().trim(),
                    "parola" to binding.parolaText.text.toString().trim(),
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
        _binding = null
    }
}
