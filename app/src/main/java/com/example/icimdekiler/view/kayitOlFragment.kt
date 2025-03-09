package com.example.icimdekiler.view

import android.os.Bundle
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

class kayitOlFragment : Fragment() {

    //Binding
    private var _binding: FragmentKayitOlBinding? = null
    private val binding get() = _binding!!

    //Firebase
    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

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

        binding.kayitOlButton.setOnClickListener { kayitOl() }

        binding.girisYapLabel.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    fun kayitOl() {
        val kullaniciAdi = binding.kullaniciAdiText.text.toString().trim()
        val isimSoyisim = binding.isimSoyisimText.text.toString().trim()
        val ePosta = binding.ePostaText.text.toString().trim()
        val telNo = binding.telNoText.text.toString().trim()
        val parola = binding.parolaText.text.toString().trim()

        // Boş alan kontrolü yap
        if (kullaniciAdi.isNotEmpty() && isimSoyisim.isNotEmpty() && ePosta.isNotEmpty() && telNo.isNotEmpty() && parola.isNotEmpty()) {
            // Aynı kullanıcı adına sahip başka bir kullanıcı var mı kontrol et
            db.collection("kullaniciBilgileri")
                .whereEqualTo("kullaniciAdi", kullaniciAdi)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        // Aynı kullanıcı adı yok, kayıt işlemini başlat
                        auth.createUserWithEmailAndPassword(ePosta, parola)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
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
                                                // Kullanıcı başarıyla kaydedildiyse, kullanıcı anasayfasına yönlendir
                                                val action = kayitOlFragmentDirections.actionKayitOlFragmentToKullaniciAnaSayfaFragment()
                                                requireView().findNavController().navigate(action)
                                            }.addOnFailureListener { exception -> Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show() }
                                    }
                                }
                            }.addOnFailureListener { exception -> Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show() }
                    } else Toast.makeText(requireContext(), R.string.buKullaniciAdiZatenKullaniliyor, Toast.LENGTH_LONG).show()
                }.addOnFailureListener { exception -> Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show() }
        } else Toast.makeText(requireContext(), R.string.lutfenBosAlanBirakmayiniz, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}