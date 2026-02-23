package com.furkanutar.icimdekiler.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.navArgs
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.ui.UrunScreen
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class urunFragment : Fragment() {

    private val db = Firebase.firestore
    private val args: urunFragmentArgs by navArgs()

    private val icindekilerListesi = mutableListOf<String>()
    private lateinit var icindekilerAdapter: ArrayAdapter<String>

    var barkodNo: String=""

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {

            setContent {

                val icindekilerListesi = args.icindekiler
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                UrunScreen(
                    urunAdi = args.urunAdi,
                    gorselUrl = args.gorselUrl,
                    icindekilerListesi = icindekilerListesi,
                    onIngredientClick = { secilenMadde ->
                        aciklamaGetir(secilenMadde)
                    }
                )
            }
        }
    }

    private fun aciklamaGetir(urun: String) {

        db.collection("icerik")
            .whereEqualTo("urun", urun)
            .get()
            .addOnSuccessListener { querySnapshot ->

                val document = querySnapshot.documents.firstOrNull()

                if (document != null) {
                    val aciklama = document.getString("aciklama")
                        ?: getString(R.string.sonucBulunamadi)

                    AlertDialog.Builder(requireContext())
                        .setMessage(aciklama)
                        .setPositiveButton(R.string.tamam, null)
                        .show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        R.string.sonucBulunamadi,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Veritabanı hatası",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}