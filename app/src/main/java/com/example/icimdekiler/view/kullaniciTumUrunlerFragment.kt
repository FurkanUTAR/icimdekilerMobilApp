
package com.example.icimdekiler.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.findNavController
import com.example.icimdekiler.R
import com.example.icimdekiler.model.Urunler
import com.example.icimdekiler.ui.KullaniciTumUrunlerScreen
import com.example.icimdekiler.ui.theme.IcimdekilerTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Locale

class kullaniciTumUrunlerFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    // Compose'un dinleyeceği stateful liste
    private var urunListesiState = mutableStateOf<List<Urunler>>(emptyList())
    private var kategori = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // SafeArgs ile kategori bilgisini al
        arguments?.let {
            kategori = kullaniciTumUrunlerFragmentArgs.fromBundle(it).kategori
        }

        // İlk veri yüklemesi
        urunleriAl(kategori)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                IcimdekilerTheme {
                    KullaniciTumUrunlerScreen(
                        urunListesi = urunListesiState.value,
                        onSearchClick = { arananKelime ->
                            urunAra(arananKelime, kategori)
                        },
                        onUrunClick = { urun ->
                            val action = kullaniciTumUrunlerFragmentDirections
                                .actionKullaniciTumUrunlerFragmentToUrunFragment(
                                    urun.barkodNo ?: "",      // Eğer null ise boş string gönder
                                    urun.urunAdi ?: "",       // Eğer null ise boş string gönder
                                    urun.icindekiler ?: "",
                                    urun.gorselUrl ?: ""
                                )
                            findNavController().navigate(action)
                        }
                    )
                }
            }
        }
    }

    private fun urunleriAl(kategori: String) {
        val baseQuery = if (kategori == "tumUrunler" || kategori.isEmpty()) {
            db.collection("urunler").orderBy("urunAdiLowerCase", Query.Direction.ASCENDING)
        } else {
            db.collection("urunler").whereEqualTo("kategori", kategori)
                .orderBy("urunAdiLowerCase", Query.Direction.ASCENDING)
        }

        baseQuery.addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(context, "Hata: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            value?.let { snapshot ->
                val liste = snapshot.documents.mapNotNull { doc ->
                    val bNo = doc.getString("barkodNo") ?: ""
                    val uAd = doc.getString("urunAdi") ?: ""
                    if (bNo.isNotEmpty() && uAd.isNotEmpty()) {
                        Urunler(
                            bNo, uAd,
                            doc.getString("icindekiler") ?: "",
                            doc.getString("gorselUrl") ?: "",
                            doc.id
                        )
                    } else null
                }
                urunListesiState.value = liste
            }
        }
    }

    private fun urunAra(arananMetin: String, kategori: String) {
        val queryText = arananMetin.lowercase(Locale("tr", "TR"))
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
                val urunAdiNormalized = urunAdi.lowercase(Locale("tr", "TR"))
                    .replace("ç", "c").replace("ğ", "g").replace("ı", "i")
                    .replace("ö", "o").replace("ş", "s").replace("ü", "u")

                if (queryText.isEmpty() || urunAdiNormalized.contains(queryText)) {
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
        }.addOnFailureListener {
            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }
}
