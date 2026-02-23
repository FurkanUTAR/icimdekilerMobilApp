package com.furkanutar.icimdekiler.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.furkanutar.icimdekiler.databinding.FragmentSplashBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import androidx.navigation.findNavController

class splashFragment : Fragment() {

    //Binding
    private var _binding: FragmentSplashBinding? = null
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
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        kontrol()
    }

    fun kontrol(){
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("kullaniciBilgileri")
                .whereEqualTo("kullaniciUID", currentUser.uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (isAdded && view != null) {
                        if (!documents.isEmpty) {
                            val kullanici = documents.documents.first()
                            val isAdmin = kullanici.getBoolean("isAdmin") ?: false

                            if (isAdmin){
                                val action = splashFragmentDirections.actionSplashFragmentToAdminAnaSayfaFragment()
                                requireView().findNavController().navigate(action)
                            } else {
                                val action = splashFragmentDirections.actionSplashFragmentToKullaniciAnaSayfaFragment()
                                requireView().findNavController().navigate(action)
                            }
                        } else {
                            val action = splashFragmentDirections.actionSplashFragmentToGirisYapFragment()
                            requireView().findNavController().navigate(action)
                        }
                    }
                }.addOnFailureListener {
                    if (isAdded && view != null) {
                        val action = splashFragmentDirections.actionSplashFragmentToGirisYapFragment()
                        requireView().findNavController().navigate(action)
                    }
                }
        } else {
            if (isAdded && view != null) {
                val action = splashFragmentDirections.actionSplashFragmentToGirisYapFragment()
                requireView().findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}