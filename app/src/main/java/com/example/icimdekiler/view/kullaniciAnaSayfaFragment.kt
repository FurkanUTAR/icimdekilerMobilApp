package com.example.icimdekiler.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.icimdekiler.R
import com.example.icimdekiler.databinding.FragmentKullaniciAnaSayfaBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class kullaniciAnaSayfaFragment : Fragment() {


    //Binding
    private var _binding: FragmentKullaniciAnaSayfaBinding? = null
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
        _binding = FragmentKullaniciAnaSayfaBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val popupMenu = PopupMenu(requireContext(), binding.popupMenu)
        popupMenu.menuInflater.inflate(R.menu.menu_fab, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.cikisYap -> {
                    auth.signOut()

                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.kullaniciAnaSayfaFragment, true) // kullanıcıAnaSayfaFragment'i yığından kaldır
                        .setLaunchSingleTop(true) // Tek seferlik aç
                        .build()
                    findNavController().navigate(R.id.action_kullaniciAnaSayfaFragment_to_girisYapFragment, null, navOptions)

                    true
                }
                else -> false
            }
        }

        binding.popupMenu.setOnClickListener {
            popupMenu.show()
        }

        /*binding.barkodOkuImage3.setOnClickListener {
            val action = adminAnaSayfaFragmentDirections.actionAdminAnaSayfaFragmentToUrunEkleFragment()
            Navigation.findNavController(view).navigate(action)
        }*/

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}