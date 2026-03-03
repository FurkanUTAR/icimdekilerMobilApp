package com.furkanutar.icimdekiler.view

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.Snackbar
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.navArgs
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.ui.UrunScreen
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
                        aciklamaGetir(secilenMadde, args.urunAdi)
                    }
                )
            }
        }
    }

    private fun aciklamaGetir(urun: String, urunAdi: String) {

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
                    Snackbar.make(requireView(), R.string.sonucBulunamadi, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.bildir) {
                            bildir("Açıklama Bulunamadı",urun, "Kullanıcı bu ürünün açıklamasını bulamadı ve bildirdi.", urunAdi)
                        }.show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Veritabanı hatası", Toast.LENGTH_SHORT).show()
            }
    }

    private fun bildir(durum: String, aramaTerimi: String, mesaj: String, urunAdi: String) {
       // val suanKiZaman = (LocalDateTime.now()).format(DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss"))

        val bildiriMap = hashMapOf(
            "durum" to durum,
            "urunAdi" to urunAdi,
            "aramaTerimi" to aramaTerimi,
            "mesaj" to mesaj,
            "zaman" to FieldValue.serverTimestamp()
        )

        db.collection("bildiriler")
            .whereEqualTo("aramaTerimi", aramaTerimi)
            .whereEqualTo("mesaj",mesaj)
            .get()
            .addOnSuccessListener { q ->
                if (q.isEmpty){
                    db.collection("bildiriler")
                        .add(bildiriMap)
                        .addOnSuccessListener { Toast.makeText(requireContext(), R.string.bildirinizIletildiTesekkurler, Toast.LENGTH_SHORT).show() }
                        .addOnFailureListener { Toast.makeText(requireContext(), R.string.bildiriGonderilemedi, Toast.LENGTH_SHORT).show() }
                } else Toast.makeText(requireContext(), R.string.bildiriDahaOnceYapilmis, Toast.LENGTH_SHORT).show()
            }
    }
}