package com.furkanutar.icimdekiler.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.navArgs
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.ui.UrunScreen
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import java.util.Locale

class urunFragment : Fragment() {

    private val db = Firebase.firestore
    private val args: urunFragmentArgs by navArgs()

    private val icindekilerListesi = mutableStateListOf<String>()
    private var urunAdi by mutableStateOf("")
    private var barkodNo by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        argumanlariYukle()
    }

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
                    urunAdi = urunAdi,
                    gorselUrl = args.gorselUrl,
                    icindekilerListesi = icindekilerListesi,
                    onIngredientClick = { secilenMadde ->
                        aciklamaGetir(secilenMadde, args.urunAdi)
                    }
                )
            }
        }
    }

    private fun argumanlariYukle() {
        Log.d("Fragment_Veri", "TÜM ARGÜMANLAR: $args")

        val gelenAd = args.urunAdi
        val gelenBarkod = args.barkodNo
        val gelenIcerik = args.icindekiler

        val duzenlenmisUrunAdi = gelenAd.split(" ").joinToString(" ") { kelime ->
            kelime.lowercase().replaceFirstChar { it.uppercase() }
        }

        Log.d("Fragment_Veri", "Argümandan gelen isim: $gelenAd")
        Log.d("Fragment_Veri", "Argümandan gelen içerik: $gelenIcerik")

        if (gelenAd.isNotBlank()) urunAdi = duzenlenmisUrunAdi
        if (gelenBarkod.isNotBlank()) barkodNo = gelenBarkod

        if (gelenIcerik.isNotBlank()) {
            icindekilerListesi.clear()

            val parcalanmisListe = gelenIcerik
                // 1. ADIM: Tam parantez içlerini siler: (Soya), [Gluten] vb.
                .replace(Regex("\\(.*?\\)|\\[.*?]|\\{.*?\\}"), "")
                // 2. ADIM: Virgül, nokta, noktalı virgül ve "ve" bağlacına göre böl
                .split(Regex("[,.;]|\\bve\\b"))
                .map { madde ->
                    madde.trim()
                        // 3. ADIM: Kenarda kalmış tek parantezleri, yıldızları veya gereksiz işaretleri temizle
                        .replace(Regex("^[\\s()*\\[\\]{}]+|[\\s()*\\[\\]{}]+$"), "")
                        .lowercase(Locale("tr", "TR"))
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .joinToString(" ") { kelime ->
                            kelime.replaceFirstChar { it.uppercase(Locale("tr", "TR")) }
                        }
                }
                // 4. ADIM: Temizlik sonrası hala kenarda işaret kaldıysa (Örn: "Çilek)") son bir kez temizle
                .map { it.trim().removeSuffix(")").removePrefix("(").trim() }
                // 5. ADIM: Filtreleme
                .filter { madde ->
                    madde.length > 1 &&
                            !madde.all { it.isDigit() } &&
                            !madde.contains(Regex("[%:0-9]"))
                }
                .distinct()

            icindekilerListesi.addAll(parcalanmisListe)
        }

        urunKaydet(gelenBarkod,duzenlenmisUrunAdi,icindekilerListesi,args.gorselUrl,"tumUrunler")
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
                    Snackbar.make(requireView(), R.string.sonucBulunamadi, Snackbar.LENGTH_SHORT)
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
            "barkodNo" to args.barkodNo,
            "aramaTerimi" to aramaTerimi,
            "mesaj" to mesaj,
            "tarih" to FieldValue.serverTimestamp()
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

    private fun urunKaydet(barkodNo: String, urunAdi: String, icindekilerListesi: List<String>, gorselUrl: String, kategori: String){
        val birlesikIcindekiler = icindekilerListesi.joinToString(", ").trim()

        val urunMap = hashMapOf(
            "urunAdi" to urunAdi.trim(),
            "urunAdiLowerCase" to urunAdi.lowercase().trim(),
            "barkodNo" to barkodNo.trim(),
            "kategori" to kategori.trim(),
            "icindekiler" to birlesikIcindekiler,
            "gorselUrl" to gorselUrl.trim()
        )

        if (barkodNo.isEmpty() || urunAdi.isEmpty() || kategori.isEmpty()) return

        db.collection("urunler")
            .whereEqualTo("barkodNo", barkodNo)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) db.collection("urunler").add(urunMap)
            }

        val bildiriMap = hashMapOf(
            "durum" to "OFF'tan Ürün Kayıt Edildi.",
            "urunAdi" to urunAdi,
            "barkodNo" to barkodNo,
            "mesaj" to "OFF'tan kayıt edilen ürün.",
            "zaman" to FieldValue.serverTimestamp()
        )

        db.collection("bildiriler")
            .add(bildiriMap)
    }
}