package com.furkanutar.icimdekiler.view

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.ui.UrunEkleScreen
import com.furkanutar.icimdekiler.ui.UrunEkleUiState
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.text.Collator
import java.util.Locale

class urunEkleFragment : Fragment() {

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val args: urunEkleFragmentArgs by navArgs()

    private val kategoriler = mutableListOf<String>()
    private val icerikListesi = mutableListOf<String>()
    private val icindekilerListesi = mutableStateListOf<String>()

    private lateinit var activityResultLauncherGallery: ActivityResultLauncher<Intent>

    private var durum by mutableStateOf("yeni")
    private var documentId by mutableStateOf("")
    private var barkodNo by mutableStateOf("")
    private var urunAdi by mutableStateOf("")
    private var seciliKategori by mutableStateOf("")
    private var seciliIcerik by mutableStateOf("")
    private var secilenGorselUri by mutableStateOf<Uri?>(null)
    private var secilenGorselUrl by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncherGallery()
        icerikAl()
        kategorileriHazirla()
        argumanlariYukle()
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        return ComposeView(requireContext()).apply {
            setContent {
                UrunEkleScreen(
                    state = UrunEkleUiState(
                        barkodNo = barkodNo,
                        urunAdi = urunAdi,
                        seciliKategori = seciliKategori,
                        seciliIcerik = seciliIcerik,
                        seciliGorselUrl = secilenGorselUri?.toString() ?: secilenGorselUrl,
                        icindekilerListesi = icindekilerListesi,
                        kategoriler = kategoriler,
                        icerikler = icerikListesi,
                        yeniMi = durum == "yeni"
                    ),
                    onBarkodChange = { barkodNo = it },
                    onBarkodOkuClick = { barkodOkuGaleri() },
                    onUrunAdiChange = { urunAdi = it },
                    onGorselSecClick = { gorselSecGaleri() },
                    onKategoriSec = { seciliKategori = it },
                    onIcerikSec = { seciliIcerik = it },
                    onIcerikEkle = {
                        if (seciliIcerik.isNotBlank()) {
                            icindekilerListesi.add(seciliIcerik)
                        }
                    },
                    onIcerikSil = { index ->
                        if (index in icindekilerListesi.indices) {
                            icindekilerListesi.removeAt(index)
                        }
                    },
                    onKaydetClick = {
                        if (durum == "yeni") {
                            AlertDialog.Builder(requireContext())
                                .setTitle(R.string.kayitEtmekIstediginizdenEminMisiniz)
                                .setPositiveButton(R.string.evet) { _, _ -> urunKaydet() }
                                .setNegativeButton(R.string.hayir, null)
                                .show()
                        } else {
                            AlertDialog.Builder(requireContext())
                                .setTitle(R.string.guncellemekIstediginizdenEminMisiniz)
                                .setPositiveButton(R.string.evet) { _, _ -> urunGuncelle() }
                                .setNegativeButton(R.string.hayir, null)
                                .show()
                        }
                    },
                    onSilClick = {
                        AlertDialog.Builder(requireContext())
                            .setTitle(R.string.silmekIstediginizdenEminMisiniz)
                            .setPositiveButton(R.string.evet) { _, _ -> urunSil() }
                            .setNegativeButton(R.string.hayir, null)
                            .show()
                    }
                )
            }
        }
    }

    private fun argumanlariYukle() {
        durum = args.durum
        barkodNo = args.barkodNo
        urunAdi = args.urunAdi
        secilenGorselUrl = args.gorselUrl
        documentId = args.documentId

        if (args.icindekiler.isNotBlank()) {
            icindekilerListesi.clear()
            icindekilerListesi.addAll(
                args.icindekiler.split(",").map { it.trim() }.filter { it.isNotBlank() }
            )
        }
    }

    private fun kategorileriHazirla() {
        kategoriler.clear()
        kategoriler.addAll(listOf("İçecek", "Süt ve Süt Ürünü", "Temel Gıda", "Atıştırmalık"))
        val collator = Collator.getInstance(Locale("tr", "TR"))
        kategoriler.sortWith { a, b -> collator.compare(a, b) }
        if (seciliKategori.isBlank() && kategoriler.isNotEmpty()) seciliKategori = kategoriler.first()
    }

    private fun icerikAl() {
        db.collection("icerik").get()
            .addOnSuccessListener { snapshot ->
                icerikListesi.clear()
                for (doc in snapshot.documents) {
                    val icerik = doc.getString("urun")
                    if (!icerik.isNullOrBlank()) icerikListesi.add(icerik)
                }
                val collator = Collator.getInstance(Locale("tr", "TR"))
                icerikListesi.sortWith { a, b -> collator.compare(a, b) }
                if (seciliIcerik.isBlank() && icerikListesi.isNotEmpty()) seciliIcerik = icerikListesi.first()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun barkodOkuGaleri() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        activityResultLauncherGallery.launch(intent)
    }

    private fun gorselSecGaleri() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        activityResultLauncherGallery.launch(intent)
    }

    private fun registerLauncherGallery() {
        activityResultLauncherGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.data ?: return@registerForActivityResult

                // önce görseli seçili hale getir
                secilenGorselUri = imageUri

                // barkod okumayı da deneyelim (başarılı olursa barkod alanını doldurur)
                try {
                    val image = InputImage.fromFilePath(requireContext(), imageUri)
                    BarcodeScanning.getClient().process(image)
                        .addOnSuccessListener { barcodes ->
                            val bulunan = barcodes.firstOrNull()?.displayValue
                            if (!bulunan.isNullOrBlank()) barkodNo = bulunan
                        }
                        .addOnFailureListener { /* sessiz geç */ }
                } catch (_: Exception) {
                    // görsel seçiminde barkod okunamazsa sadece görsel seçim olarak kalır
                }
            }
        }
    }

    private fun urunKaydet() {
        val barkod = barkodNo.trim()
        val ad = urunAdi.trim()
        val adLower = ad.lowercase().trim()
            .replace("ç", "c")
            .replace("ğ", "g")
            .replace("ı", "i")
            .replace("ö", "o")
            .replace("ş", "s")
            .replace("ü", "u")
        val kategori = seciliKategori.trim()
        val birlesikIcindekiler = icindekilerListesi.joinToString(", ").trim()

        if (barkod.isEmpty() || ad.isEmpty() || kategori.isEmpty() || birlesikIcindekiler.isEmpty()) {
            Toast.makeText(requireContext(), R.string.lutfenBosAlanBirakmayiniz, Toast.LENGTH_SHORT).show()
            return
        }

        val seciliUri = secilenGorselUri
        if (seciliUri == null) {
            Toast.makeText(requireContext(), R.string.gorselBulunamadi, Toast.LENGTH_SHORT).show()
            return
        }

        val gorselReferansi = storage.reference.child("images/${barkod}.jpg")
        gorselReferansi.putFile(seciliUri)
            .addOnSuccessListener {
                gorselReferansi.downloadUrl.addOnSuccessListener { uri ->
                    val urunMap = hashMapOf(
                        "urunAdi" to ad,
                        "urunAdiLowerCase" to adLower,
                        "barkodNo" to barkod,
                        "kategori" to kategori,
                        "icindekiler" to birlesikIcindekiler,
                        "gorselUrl" to uri.toString()
                    )

                    db.collection("urunler")
                        .whereEqualTo("barkodNo", barkod)
                        .get()
                        .addOnSuccessListener { q ->
                            if (q.isEmpty) {
                                db.collection("urunler").add(urunMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(requireContext(), R.string.urunBasariylaKayitEdildi, Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(requireContext(), R.string.urunDahaOnceKayitEdilmis, Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun urunGuncelle() {
        if (documentId.isBlank()) {
            Toast.makeText(requireContext(), R.string.sonucBulunamadi, Toast.LENGTH_SHORT).show()
            return
        }

        val barkod = barkodNo.trim()
        val ad = urunAdi.trim()
        val adLower = ad.lowercase().trim()
            .replace("ç", "c")
            .replace("ğ", "g")
            .replace("ı", "i")
            .replace("ö", "o")
            .replace("ş", "s")
            .replace("ü", "u")

        val updateMap = hashMapOf<String, Any>(
            "urunAdi" to ad,
            "urunAdiLowerCase" to adLower,
            "barkodNo" to barkod,
            "kategori" to seciliKategori,
            "icindekiler" to icindekilerListesi.joinToString(", ")
        )

        val seciliUri = secilenGorselUri
        if (seciliUri != null) {
            val gorselReferansi = storage.reference.child("images/${barkod}.jpg")
            gorselReferansi.putFile(seciliUri)
                .addOnSuccessListener {
                    gorselReferansi.downloadUrl.addOnSuccessListener { uri ->
                        updateMap["gorselUrl"] = uri.toString()
                        urunBelgesiGuncelle(updateMap)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        } else {
            secilenGorselUrl?.let { updateMap["gorselUrl"] = it }
            urunBelgesiGuncelle(updateMap)
        }
    }

    private fun urunBelgesiGuncelle(updateMap: Map<String, Any>) {
        db.collection("urunler").document(documentId)
            .update(updateMap)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), R.string.urunBasariylaGuncellendi, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun urunSil() {
        if (documentId.isBlank()) {
            Toast.makeText(requireContext(), R.string.sonucBulunamadi, Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("urunler").document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), R.string.urunBasariylaSilindi, Toast.LENGTH_SHORT).show()
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.urunEkleFragment, true)
                    .build()
                findNavController().navigate(R.id.adminAnaSayfaFragment, null, navOptions)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }
}
