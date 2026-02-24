package com.furkanutar.icimdekiler.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.ui.theme.IcimdekilerTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class splashFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // XML (Binding) yerine doğrudan ComposeView döndürüyoruz
        return ComposeView(requireContext()).apply {
            setContent {
                IcimdekilerTheme {
                    // Daha önce yazdığımız animasyonlu UI fonksiyonunu çağırıyoruz
                    SplashScreen(onAnimationFinished = {
                        kontrol()
                    })
                }
            }
        }
    }

    private fun kontrol() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            db.collection("kullaniciBilgileri")
                .whereEqualTo("kullaniciUID", currentUser.uid)
                .get()
                .addOnSuccessListener { documents ->
                    // Fragment hala ekrandaysa ve kullanıcı varsa işlemleri yap
                    if (isAdded) {
                        if (!documents.isEmpty) {
                            val kullanici = documents.documents.first()
                            val isAdmin = kullanici.getBoolean("isAdmin") ?: false

                            if (isAdmin) {
                                findNavController().navigate(R.id.action_splashFragment_to_adminAnaSayfaFragment)
                            } else {
                                findNavController().navigate(R.id.action_splashFragment_to_kullaniciAnaSayfaFragment)
                            }
                        } else {
                            findNavController().navigate(R.id.action_splashFragment_to_girisYapFragment)
                        }
                    }
                }
                .addOnFailureListener {
                    if (isAdded) {
                        findNavController().navigate(R.id.action_splashFragment_to_girisYapFragment)
                    }
                }
        } else {
            if (isAdded) {
                findNavController().navigate(R.id.action_splashFragment_to_girisYapFragment)
            }
        }
    }
}