package com.furkanutar.icimdekiler.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
import com.furkanutar.icimdekiler.model.Urunler
import com.furkanutar.icimdekiler.ui.AdminTumUrunlerScreen
import com.furkanutar.icimdekiler.ui.theme.IcimdekilerTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Locale

class adminTumUrunlerFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private var urunListesiState = mutableStateOf<List<Urunler>>(emptyList())
    private var kategori = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {
            kategori = adminTumUrunlerFragmentArgs.fromBundle(it).kategori
        }

        urunleriAl(kategori)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                IcimdekilerTheme {
                    AdminTumUrunlerScreen(
                        urunListesi = urunListesiState.value,
                        onSearchClick = { query -> urunAra(query, kategori) },
                        onUrunClick = { urun ->
                            // ADMIN İÇİN: Ürün düzenleme ekranına yönlendir
                            // Not: Argüman isimlerini kendi NavGraph'ına göre kontrol et
                            val action = adminTumUrunlerFragmentDirections
                                .actionAdminTumUrunlerFragmentToUrunEkleFragment(
                                    durum = "eski",
                                    documentId = urun.documentId ?: "",
                                    barkodNo = urun.barkodNo ?: "",
                                    urunAdi = urun.urunAdi ?: "",
                                    icindekiler = urun.icindekiler ?: "",
                                    gorselUrl = urun.gorselUrl ?: ""
                                )
                            findNavController().navigate(action)
                        }
                    )
                }
            }
        }
    }

    private fun urunleriAl(kategori: String) {
        val query = if (kategori == "tumUrunler" || kategori.isEmpty()) {
            db.collection("urunler").orderBy("urunAdiLowerCase", Query.Direction.ASCENDING)
        } else {
            db.collection("urunler").whereEqualTo("kategori", kategori).orderBy("urunAdiLowerCase", Query.Direction.ASCENDING)
        }

        query.addSnapshotListener { value, error ->
            if (error != null) return@addSnapshotListener
            value?.let { snapshot ->
                val liste = snapshot.documents.mapNotNull { doc ->
                    Urunler(
                        doc.getString("barkodNo") ?: "",
                        doc.getString("urunAdi") ?: "",
                        doc.getString("icindekiler") ?: "",
                        doc.getString("gorselUrl") ?: "",
                        doc.id
                    )
                }
                urunListesiState.value = liste
            }
        }
    }

    private fun urunAra(arananMetin: String, kategori: String) {
        val normalizedQuery = arananMetin.lowercase(Locale("tr", "TR"))
            .replace("ç", "c").replace("ğ", "g").replace("ı", "i")
            .replace("ö", "o").replace("ş", "s").replace("ü", "u")

        val baseQuery = if (kategori == "tumUrunler" || kategori.isEmpty()) {
            db.collection("urunler")
        } else {
            db.collection("urunler").whereEqualTo("kategori", kategori)
        }

        baseQuery.get().addOnSuccessListener { documents ->
            val filtrelenmisListe = documents.mapNotNull { doc ->
                val urunAdi = doc.getString("urunAdi") ?: ""
                val normalizedUrunAdi = urunAdi.lowercase(Locale("tr", "TR"))
                    .replace("ç", "c").replace("ğ", "g").replace("ı", "i")
                    .replace("ö", "o").replace("ş", "s").replace("ü", "u")

                if (normalizedQuery.isEmpty() || normalizedUrunAdi.contains(normalizedQuery)) {
                    Urunler(
                        doc.getString("barkodNo") ?: "",
                        urunAdi,
                        doc.getString("icindekiler") ?: "",
                        doc.getString("gorselUrl") ?: "",
                        doc.id
                    )
                } else null
            }
            urunListesiState.value = filtrelenmisListe
        }
    }
}