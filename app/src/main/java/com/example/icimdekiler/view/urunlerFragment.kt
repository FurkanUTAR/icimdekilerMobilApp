package com.example.icimdekiler.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.icimdekiler.R
import com.example.icimdekiler.databinding.FragmentUrunlerBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class urunlerFragment : Fragment() {

    //Binding
    private var _binding: FragmentUrunlerBinding? = null
    private val binding get() = _binding!!

    //Firebase
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUrunlerBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tumUrunlerButton.setOnClickListener { kontrol("tumUrunler") }

        binding.iceceklerButton.setOnClickListener { kontrol("İçecek") }

        binding.atistirmaliklarButton.setOnClickListener { kontrol("Atıştırmalık") }

        binding.temelGidaButton.setOnClickListener { kontrol("Temel Gıda") }

        binding.sutVeSutUrunleriButton.setOnClickListener { kontrol("Süt ve Süt Ürünleri") }
    }

    fun kontrol(kategori : String){
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Kullanıcı bilgileri Firestore'dan çek
            db.collection("kullaniciBilgileri")
                .whereEqualTo("kullaniciUID", currentUser.uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val kullanici = documents.documents.first()
                        val isAdmin = kullanici.getBoolean("isAdmin") ?: false

                        // Yönlendirme işlemi
                        if (isAdmin){
                            val action = urunlerFragmentDirections.actionUrunlerFragmentToAdminTumUrunlerFragment(kategori)
                            requireView().findNavController().navigate(action)
                        } else {
                            val action = urunlerFragmentDirections.actionUrunlerFragmentToKullaniciTumUrunlerFragment(kategori)
                            requireView().findNavController().navigate(action)
                        }
                    } else {
                        Toast.makeText(requireContext(), R.string.beklenmedikBirHataOlustu, Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), R.string.beklenmedikBirHataOlustu, Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), R.string.beklenmedikBirHataOlustu, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}