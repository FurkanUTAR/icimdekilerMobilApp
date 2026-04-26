package com.furkanutar.icimdekiler.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
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
import com.furkanutar.icimdekiler.ui.theme.IcimdekilerTheme
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import java.util.Locale

class urunFragment : Fragment() {

    private val db = Firebase.firestore
    private val args: urunFragmentArgs by navArgs()

    private val icindekilerListesi = mutableStateListOf<String>()
    private var urunAdi       by mutableStateOf("")
    private var barkodNo      by mutableStateOf("")
    private var kalori        by mutableStateOf(0)
    private var protein       by mutableStateOf(0f)
    private var karbonhidrat  by mutableStateOf(0f)
    private var yag           by mutableStateOf(0f)

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
                IcimdekilerTheme {
                    val gorunenIcindekiler = args.icindekiler
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    UrunScreen(
                        urunAdi       = urunAdi,
                        gorselUrl     = args.gorselUrl,
                        kalori        = kalori,
                        protein       = protein,
                        karbonhidrat  = karbonhidrat,
                        yag           = yag,
                        icindekilerListesi = gorunenIcindekiler,
                        onIngredientClick = { secilenMadde ->
                            aciklamaGetir(secilenMadde, args.urunAdi)
                        },
                        onEkleClick = { miktar ->
                            Log.d("KaloriTakip", "Seçilen miktar: $miktar gram.")
                            Toast.makeText(
                                requireContext(),
                                "$miktar gr günlüğünüze eklendi",
                                Toast.LENGTH_SHORT
                            ).show()
                            // İleride buraya RoomDB kaydı gelecek
                        }
                    )
                }
            }
        }
    }

    private fun argumanlariYukle() {
        Log.d("Fragment_Veri", "TÜM ARGÜMANLAR: $args")

        val gelenAd      = args.urunAdi
        val gelenBarkod  = args.barkodNo
        val gelenIcerik  = args.icindekiler

        val duzenlenmisUrunAdi = gelenAd.split(" ").joinToString(" ") { kelime ->
            kelime.lowercase().replaceFirstChar { it.uppercase() }
        }

        if (gelenAd.isNotBlank())     urunAdi    = duzenlenmisUrunAdi
        if (gelenBarkod.isNotBlank()) barkodNo   = gelenBarkod

        // Besin değerleri
        kalori       = args.kalori
        protein      = args.protein
        karbonhidrat = args.karbonhidrat
        yag          = args.yag

        if (gelenIcerik.isNotBlank()) {
            icindekilerListesi.clear()
            val parcalanmis = gelenIcerik
                .replace(Regex("\\(.*?\\)|\\[.*?]|\\{.*?\\}"), "")
                .split(Regex("[,.;]|\\bve\\b"))
                .map { madde ->
                    madde.trim()
                        .replace(Regex("^[\\s()*\\[\\]{}]+|[\\s()*\\[\\]{}]+$"), "")
                        .lowercase(Locale("tr", "TR"))
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .joinToString(" ") { kelime ->
                            kelime.replaceFirstChar { it.uppercase(Locale("tr", "TR")) }
                        }
                }
                .map { it.trim().removeSuffix(")").removePrefix("(").trim() }
                .filter { madde ->
                    madde.length > 1 &&
                            !madde.all { it.isDigit() } &&
                            !madde.contains(Regex("[%:0-9]"))
                }
                .distinct()
            icindekilerListesi.addAll(parcalanmis)
        }

        urunKaydet(
            barkodNo         = gelenBarkod,
            urunAdi          = duzenlenmisUrunAdi,
            icindekilerListesi = icindekilerListesi,
            gorselUrl        = args.gorselUrl,
            kategori         = "tumUrunler",
            kalori           = args.kalori,
            protein          = args.protein,
            karbonhidrat     = args.karbonhidrat,
            yag              = args.yag
        )
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
                            bildir(
                                "Açıklama Bulunamadı",
                                urun,
                                "Kullanıcı bu ürünün açıklamasını bulamadı ve bildirdi.",
                                urunAdi
                            )
                        }.show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Veritabanı hatası", Toast.LENGTH_SHORT).show()
            }
    }

    private fun bildir(durum: String, aramaTerimi: String, mesaj: String, urunAdi: String) {
        val bildiriMap = hashMapOf(
            "durum"       to durum,
            "urunAdi"     to urunAdi,
            "barkodNo"    to args.barkodNo,
            "aramaTerimi" to aramaTerimi,
            "mesaj"       to mesaj,
            "tarih"       to FieldValue.serverTimestamp()
        )
        db.collection("bildiriler")
            .whereEqualTo("aramaTerimi", aramaTerimi)
            .whereEqualTo("mesaj", mesaj)
            .get()
            .addOnSuccessListener { q ->
                if (q.isEmpty) {
                    db.collection("bildiriler").add(bildiriMap)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), R.string.bildirinizIletildiTesekkurler, Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), R.string.bildiriGonderilemedi, Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), R.string.bildiriDahaOnceYapilmis, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun urunKaydet(
        barkodNo: String,
        urunAdi: String,
        icindekilerListesi: List<String>,
        gorselUrl: String,
        kategori: String,
        kalori: Int,
        protein: Float,
        karbonhidrat: Float,
        yag: Float
    ) {
        if (barkodNo.isEmpty() || urunAdi.isEmpty() || kategori.isEmpty()) return

        val birlesikIcindekiler = icindekilerListesi.joinToString(", ").trim()

        val urunMap = hashMapOf(
            "urunAdi"          to urunAdi.trim(),
            "urunAdiLowerCase" to urunAdi.lowercase().trim(),
            "barkodNo"         to barkodNo.trim(),
            "kategori"         to kategori.trim(),
            "icindekiler"      to birlesikIcindekiler,
            "gorselUrl"        to gorselUrl.trim(),
            "kalori"           to kalori,
            "protein"          to protein,
            "karbonhidrat"     to karbonhidrat,
            "yag"              to yag
        )

        db.collection("urunler")
            .whereEqualTo("barkodNo", barkodNo)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // Yeni ürün → ekle
                    db.collection("urunler").add(urunMap)
                    Log.d("urunKaydet", "Yeni ürün kaydedildi: $urunAdi")
                } else {
                    // Mevcut ürün → sadece eksik besin değerleri varsa güncelle
                    val docRef = querySnapshot.documents.first().reference
                    val mevcutKalori = querySnapshot.documents.first().getLong("kalori")?.toInt() ?: 0
                    if (mevcutKalori == 0 && kalori > 0) {
                        docRef.update(
                            mapOf(
                                "kalori"      to kalori,
                                "protein"     to protein,
                                "karbonhidrat" to karbonhidrat,
                                "yag"         to yag
                            )
                        )
                        Log.d("urunKaydet", "Mevcut ürün besin değerleri güncellendi: $urunAdi")
                    }
                }
            }

        // Bildiri kaydı (OFF'tan gelen ürünler için)
        db.collection("bildiriler").add(
            hashMapOf(
                "durum"    to "OFF'tan Ürün Kayıt Edildi.",
                "urunAdi"  to urunAdi,
                "barkodNo" to barkodNo,
                "mesaj"    to "OFF'tan kayıt edilen ürün.",
                "zaman"    to FieldValue.serverTimestamp()
            )
        )
    }
}