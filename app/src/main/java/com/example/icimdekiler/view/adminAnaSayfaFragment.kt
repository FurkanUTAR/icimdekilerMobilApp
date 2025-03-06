package com.example.icimdekiler.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.icimdekiler.R
import com.example.icimdekiler.databinding.FragmentAdminAnaSayfaBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class adminAnaSayfaFragment : Fragment() {

    //Binding
    private var _binding: FragmentAdminAnaSayfaBinding? = null
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
        _binding = FragmentAdminAnaSayfaBinding.inflate(inflater, container, false)
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

                    val navOptions = NavOptions.Builder().setPopUpTo(R.id.adminAnaSayfaFragment, true).setLaunchSingleTop(true).build()
                    findNavController().navigate(R.id.action_adminAnaSayfaFragment_to_girisYapFragment, null, navOptions)

                    true
                }
                else -> false
            }
        }

        binding.popupMenu.setOnClickListener { popupMenu.show() }

        binding.ekleImage.setOnClickListener {
            val action = adminAnaSayfaFragmentDirections.actionAdminAnaSayfaFragmentToUrunEkleFragment(durum = "yeni", barkodNo = "", urunAdi = "", icindekiler = "")
            Navigation.findNavController(view).navigate(action)
        }

        binding.tumUrunlerButton.setOnClickListener {
            val action = adminAnaSayfaFragmentDirections.actionAdminAnaSayfaFragmentToAdminTumUrunlerFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}