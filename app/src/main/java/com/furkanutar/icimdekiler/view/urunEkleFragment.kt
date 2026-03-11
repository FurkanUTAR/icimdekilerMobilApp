package com.furkanutar.icimdekiler.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.ui.KaynakSecimDialog
import com.furkanutar.icimdekiler.ui.OzelAlertDialog
import com.furkanutar.icimdekiler.ui.UrunEkleScreen
import com.furkanutar.icimdekiler.ui.UrunEkleUiState
import com.furkanutar.icimdekiler.ui.theme.EmeraldGreen
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.text.Collator
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class urunEkleFragment : Fragment() {

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val args: urunEkleFragmentArgs by navArgs()

    private lateinit var permissionLauncherGallery: ActivityResultLauncher<String>
    private lateinit var activityResultLauncherGallery: ActivityResultLauncher<Intent>

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

    private var camera: Camera? = null
    private var isFlashOn = false  // Flash durumu

    private val kategoriler = mutableListOf<String>()
    private val icerikListesi = mutableListOf<String>()
    private val icindekilerListesi = mutableStateListOf<String>()

    private var islem by mutableStateOf("") // "barkodOku" veya "gorselSec"
    private var durum by mutableStateOf("yeni")
    private var documentId by mutableStateOf("")
    private var barkodNo by mutableStateOf("")
    private var urunAdi by mutableStateOf("")
    private var seciliKategori by mutableStateOf("")
    private var seciliIcerik by mutableStateOf("")
    private var secilenGorselUri by mutableStateOf<Uri?>(null)
    private var secilenGorselUrl by mutableStateOf<String?>(null)

    private var showOzelDialog by mutableStateOf(false)
    private var dialogBaslik by mutableStateOf("")
    private var pendingAction by mutableStateOf<(() -> Unit)?>(null)
    private var onayButonRengi by mutableStateOf(EmeraldGreen)

    private var showSourceDialog by mutableStateOf(false)
    private var isBarcodeAction by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        barcodeScanner = BarcodeScanning.getClient()
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
                        icindekilerListesi = icindekilerListesi.toList(), // Compose'un değişikliği fark etmesi için toList()
                        kategoriler = kategoriler,
                        icerikler = icerikListesi,
                        yeniMi = durum == "yeni"
                    ),
                    onBarkodChange = { barkodNo = it },
                    onBarkodOkuClick = {
                        isBarcodeAction = true
                        showSourceDialog = true
                    },
                    onUrunAdiChange = { urunAdi = it },
                    onGorselSecClick = {
                        islem = "gorselSec"
                        barkodOkuGaleri()
                    },
                    onKategoriSec = { seciliKategori = it },
                    onIcerikSec = { seciliIcerik = it },
                    // YENİ: ComboBox içine bir şey yazıldığında tetiklenir
                    onIcerikMetinDegistir = { seciliIcerik = it },
                    onIcerikEkle = {
                        if (seciliIcerik.isNotBlank()) {
                            icindekilerListesi.add(seciliIcerik)
                            seciliIcerik = "" // Eklendikten sonra kutuyu temizle
                        }
                    },
                    onIcerikSil = { index ->
                        if (index in icindekilerListesi.indices) {
                            icindekilerListesi.removeAt(index)
                        }
                    },
                    // YENİ: Sıralama değiştirme bağlantısı
                    onIcerikYerDegistir = { eski, yeni ->
                        icerikYerDegistir(eski, yeni)
                    },
                    onKaydetClick = {
                        dialogBaslik = if (durum == "yeni")
                            getString(R.string.kayitEtmekIstediginizdenEminMisiniz)
                        else
                            getString(R.string.guncellemekIstediginizdenEminMisiniz)

                        onayButonRengi = EmeraldGreen
                        pendingAction = { if (durum == "yeni") urunKaydet() else urunGuncelle() }
                        showOzelDialog = true
                    },
                    onSilClick = {
                        dialogBaslik = getString(R.string.silmekIstediginizdenEminMisiniz)
                        onayButonRengi = Color.Red
                        pendingAction = { urunSil() }
                        showOzelDialog = true
                    }
                )


                if (showSourceDialog) {
                    KaynakSecimDialog(
                        isBarcodeAction = isBarcodeAction,
                        onDismiss = { showSourceDialog = false },
                        onOption1 = { if (isBarcodeAction) showBarcodeScannerDialog() }, // Kamera (Barkod)
                        onOption2 = { if (isBarcodeAction) {
                            islem = "barkodOku"
                            barkodOkuGaleri()
                        } } // Galeri (Barkod)
                    )
                }

                // Kendi yazdığın özel dialogu burada çağırıyoruz
                if (showOzelDialog) {
                    OzelAlertDialog(
                        baslik = dialogBaslik,
                        onayButonRengi = onayButonRengi,
                        onayButonMetni = getString(R.string.evet),
                        iptalButonMetni = getString(R.string.hayir),
                        onDismiss = { showOzelDialog = false },
                        onConfirm = {
                            pendingAction?.invoke()
                            showOzelDialog = false
                        }
                    )
                }
            }
        }
    }

    private fun argumanlariYukle() {
        Log.d("Fragment_Veri", "TÜM ARGÜMANLAR: $args")

        val gelenDurum = args.durum
        if (gelenDurum.isNotBlank()) durum = gelenDurum

        documentId = args.documentId ?: ""
        val gelenAd = args.urunAdi
        val gelenBarkod = args.barkodNo
        val gelenGorsel = args.gorselUrl
        val gelenIcerik = args.icindekiler

        val duzenlenmisUrunAdi = gelenAd.split(" ").joinToString(" ") { kelime ->
            kelime.lowercase().replaceFirstChar { it.uppercase() }
        }

        Log.d("Fragment_Veri", "Argümandan gelen isim: $gelenAd")
        Log.d("Fragment_Veri", "Argümandan gelen içerik: $gelenIcerik")

        if (gelenAd.isNotBlank()) urunAdi = duzenlenmisUrunAdi
        if (gelenBarkod.isNotBlank()) barkodNo = gelenBarkod
        if (gelenGorsel.isNotBlank()) secilenGorselUrl = gelenGorsel

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

    fun icerikYerDegistir(eskiIndeks: Int, yeniIndeks: Int) {
        if (eskiIndeks in icindekilerListesi.indices && yeniIndeks in icindekilerListesi.indices) {
            // Elemanların yerini değiştir
            val tasinanOge = icindekilerListesi.removeAt(eskiIndeks)
            icindekilerListesi.add(yeniIndeks, tasinanOge)
            seciliIcerik = seciliIcerik // Basit bir state değişikliği recomposition tetikler
        }
    }

    private fun showBarcodeScannerDialog() {
        try {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                    Snackbar.make(requireView(), R.string.barkodOkumakIcinKamerayaErisimIzniGerekli, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.izinVer) {
                            try {
                                requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
                            } catch (e: Exception) {
                                Log.e("UrunEkle", "Permission request error", e)
                            }
                        }.show()
                } else {
                    try {
                        requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
                    } catch (e: Exception) {
                        Log.e("UrunEkle", "Permission request error", e)
                    }
                }
            } else {
                try {
                    val dialog = BottomSheetDialog(requireContext())
                    val view = layoutInflater.inflate(R.layout.dialog_barkod_okuma, null)
                    dialog.setContentView(view)
                    dialog.show()

                    val previewView = view.findViewById<PreviewView>(R.id.previewView)
                    val btnClose = view.findViewById<Button>(R.id.btnClose)

                    val btnFlashToggle = view.findViewById<Button>(R.id.btnFlashToggle)
                    startCamera(previewView, dialog, btnFlashToggle)

                    btnClose.setOnClickListener {
                        try {
                            // Flash'ı kapat
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                cameraProvider.unbindAll() // Kamerayı durdur
                            }, ContextCompat.getMainExecutor(requireContext()))


                            dialog.dismiss()
                        } catch (e: Exception) {
                            Log.e("UrunEkle", "Dialog close error", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("UrunEkle", "Camera dialog error", e)
                }
            }
        } catch (e: Exception) {
            Log.e("AdminAnaSayfa", "Barcode scanner error", e)
        }
    }

    private fun startCamera(previewView: PreviewView, dialog: BottomSheetDialog, btnFlashToggle: Button?) {
        try {
            // Kamera sağlayıcısını alır.
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

            // Kamera başlatma işlemi tamamlandığında çağrılır.
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    // Kamera önizlemesi oluşturulur.
                    val preview = androidx.camera.core.Preview.Builder()
                        .setTargetRotation(previewView.display.rotation)
                        .build()
                        .also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                    // Görüntü analizi için yapılandırma yapılır.
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
                                try {
                                    analyzeImage(imageProxy, dialog)
                                } catch (e: Exception) {
                                    Log.e("AdminAnaSayfa", "Image analysis error", e)
                                }
                            }
                        }

                    val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()

                        // 📸 Kamera nesnesini değişkene atıyoruz
                        camera = cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageAnalyzer)

                        // Flash başlangıçta kapalı olacak
                        camera?.cameraControl?.enableTorch(false)

                        // 🎯 Buton ile flash kontrolü
                        btnFlashToggle?.setOnClickListener {
                            isFlashOn = !isFlashOn  // Flash durumunu tersine çevir
                            camera?.cameraControl?.enableTorch(isFlashOn)

                            // Buton metnini güncelle
                            btnFlashToggle.text = if (isFlashOn) getString(R.string.flasKapat) else getString(R.string.flasAc)
                        }

                    } catch (e: Exception) {
                        Log.e("CameraX", "Camera bind failed", e)
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Flash açılamadı: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("UrunEkle", "Camera setup error", e)
                }
            }, ContextCompat.getMainExecutor(requireContext()))
        } catch (e: Exception) {
            Log.e("UrunEkle", "Camera start error", e)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeImage(imageProxy: ImageProxy, dialog: BottomSheetDialog) {
        try {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        try {
                            if (barcodes.isNotEmpty()) {
                                for (barcode in barcodes) {
                                    val barkod = barcode.displayValue
                                    if (barkod != null) {
                                        requireActivity().runOnUiThread {
                                            try {
                                                barkodNo = barkod
                                                dialog.dismiss()
                                            } catch (e: Exception) {
                                                Log.e("UrunEkle", "Barcode success error", e)
                                            }
                                        }
                                        break
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("UrunEkle", "Barcode processing error", e)
                        }
                    }
                    .addOnFailureListener { e ->
                        requireActivity().runOnUiThread {
                            try {
                                Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Log.e("UrunEkle", "Barcode failure error", e)
                            }
                        }
                    }
                    .addOnCompleteListener {
                        try {
                            imageProxy.close()
                        } catch (e: Exception) {
                            Log.e("UrunEkle", "Image proxy close error", e)
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e("UrunEkle", "Image analysis error", e)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val dialog = BottomSheetDialog(requireContext())
                val previewView = dialog.findViewById<PreviewView>(R.id.previewView)
                if (previewView != null) {
                    try {
                        val btnFlashToggle = view?.findViewById<Button>(R.id.btnFlashToggle)
                        startCamera(previewView, dialog, btnFlashToggle)
                    } catch (e: Exception) {
                        Log.e("UrunEkle", "Camera restart error", e)
                    }
                }
            } else {
                try {
                    Toast.makeText(requireContext(), R.string.barkodOkumakIcinKamerayaErisimIzniGerekli, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("UrunEkle", "Permission toast error", e)
                }
            }
        } catch (e: Exception) {
            Log.e("UrunEkle", "Permission result error", e)
        }
    }

    private fun barkodOkuGaleri() {
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                try {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                        try {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)) {
                                try {
                                    Snackbar.make(requireView(), R.string.barkodOkumakIcinGaleriyeErisimIzniGerekli, Snackbar.LENGTH_INDEFINITE)
                                        .setAction(R.string.izinVer) {
                                            try {
                                                permissionLauncherGallery.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                            } catch (e: Exception) {
                                                Log.e("AdminAnaSayfa", "Permission launch error", e)
                                            }
                                        }.show()
                                } catch (e: Exception) {
                                    Log.e("AdminAnaSayfa", "Snackbar error", e)
                                }
                            } else {
                                try {
                                    permissionLauncherGallery.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                } catch (e: Exception) {
                                    Log.e("AdminAnaSayfa", "Permission launch error", e)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AdminAnaSayfa", "Permission check error", e)
                        }
                    } else {
                        try {
                            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            activityResultLauncherGallery.launch(intent)
                        } catch (e: Exception) {
                            Log.e("AdminAnaSayfa", "Gallery intent error", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AdminAnaSayfa", "Android 13+ permission check error", e)
                }
            } else {
                try {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        try {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                try {
                                    Snackbar.make(requireView(), R.string.barkodOkumakIcinGaleriyeErisimIzniGerekli, Snackbar.LENGTH_INDEFINITE)
                                        .setAction(R.string.izinVer) {
                                            try {
                                                permissionLauncherGallery.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                            } catch (e: Exception) {
                                                Log.e("AdminAnaSayfa", "Permission launch error", e)
                                            }
                                        }.show()
                                } catch (e: Exception) {
                                    Log.e("AdminAnaSayfa", "Snackbar error", e)
                                }
                            } else {
                                try {
                                    permissionLauncherGallery.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                } catch (e: Exception) {
                                    Log.e("AdminAnaSayfa", "Permission launch error", e)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AdminAnaSayfa", "Permission check error", e)
                        }
                    } else {
                        try {
                            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            activityResultLauncherGallery.launch(intent)
                        } catch (e: Exception) {
                            Log.e("AdminAnaSayfa", "Gallery intent error", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AdminAnaSayfa", "Pre-Android 13 permission check error", e)
                }
            }
        } catch (e: Exception) {
            Log.e("AdminAnaSayfa", "Gallery barcode processing error", e)
        }
    }

    private fun registerLauncherGallery() {
        try {
            activityResultLauncherGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                try {
                    if (result.resultCode == RESULT_OK) {
                        val imageUri = result.data?.data
                        if (imageUri != null) {
                            when (islem) {
                                "barkodOku" -> {
                                    val image = InputImage.fromFilePath(requireContext(), imageUri)
                                    // Scanner'ı burada doğrudan kullanabiliriz
                                    barcodeScanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            if (barcodes.isNotEmpty()) {
                                                // İlk bulunan barkodu al
                                                val barkod = barcodes[0].displayValue
                                                if (barkod != null) {
                                                    barkodNo = barkod // State güncellenir, TextField dolar
                                                }
                                            } else {
                                                Toast.makeText(requireContext(), R.string.barkodOkunamadi, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("BarkodHata", "ML Kit hatası", e)
                                            Toast.makeText(requireContext(), "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                "gorselSec" -> {
                                    secilenGorselUri = imageUri
                                }
                            }
                        } else {
                            Toast.makeText(requireContext(), R.string.gorselBulunamadi, Toast.LENGTH_SHORT).show()
                        }
                    }
                    islem = "" // İşlem tamamlandıktan sonra sıfırla
                } catch (e: Exception) {
                    Log.e("ÜrünEkle", "Galeriye erişim hatası", e)
                    Toast.makeText(requireContext(), "Galeriye erişim hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    islem = "" // Hata durumunda da sıfırla
                }
            }

            permissionLauncherGallery = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                try {
                    if (result) {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        activityResultLauncherGallery.launch(intent)
                    } else {
                        Toast.makeText(requireContext(), R.string.galeriIzniVerilmedi, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("ÜrünEkle", "İzin işleme hatası", e)
                    Toast.makeText(requireContext(), "İzin işleme hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("ÜrünEkle", "Launcher kayıt hatası", e)
            Toast.makeText(requireContext(), "Launcher kayıt hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun urunKaydet() {
        val barkod = barkodNo.trim()
        val ad = urunAdi.trim()
        val adLower = ad.lowercase().trim()
        val kategori = seciliKategori.trim()
        val birlesikIcindekiler = icindekilerListesi.joinToString(", ").trim()

        if (barkod.isEmpty() || ad.isEmpty() || kategori.isEmpty() || birlesikIcindekiler.isEmpty()) {
            Toast.makeText(requireContext(), R.string.lutfenBosAlanBirakmayiniz, Toast.LENGTH_SHORT).show()
            return
        }

        // --- KRİTİK DEĞİŞİKLİK BURADA BAŞLIYOR ---

        // Eğer galeriden yeni bir görsel seçilmişse (secilenGorselUri doluysa)
        if (secilenGorselUri != null) {
            val gorselReferansi = storage.reference.child("images/${barkod}")
            gorselReferansi.putFile(secilenGorselUri!!)
                .addOnSuccessListener {
                    gorselReferansi.downloadUrl.addOnSuccessListener { uri ->
                        // Storage'a yüklenen yeni URL ile kaydet
                        veritabaninaKaydet(ad, adLower, barkod, kategori, birlesikIcindekiler, uri.toString())
                    }
                }
                .addOnFailureListener { Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show() }
        }
        // Eğer galeriden seçilmemiş ama API'den bir link gelmişse (secilenGorselUrl doluysa)
        else if (!secilenGorselUrl.isNullOrBlank()) {
            // Direkt API'den gelen URL ile kaydet
            veritabaninaKaydet(ad, adLower, barkod, kategori, birlesikIcindekiler, secilenGorselUrl!!)
        }
        // İkisi de yoksa hata ver
        else {
            Toast.makeText(requireContext(), R.string.gorselBulunamadi, Toast.LENGTH_SHORT).show()
        }
    }

    private fun veritabaninaKaydet(ad: String, adLower: String, barkod: String, kategori: String, icerik: String, url: String) {
        val urunMap = hashMapOf(
            "urunAdi" to ad,
            "urunAdiLowerCase" to adLower,
            "barkodNo" to barkod,
            "kategori" to kategori,
            "icindekiler" to icerik,
            "gorselUrl" to url
        )

        db.collection("urunler")
            .whereEqualTo("barkodNo", barkod)
            .get()
            .addOnSuccessListener { q ->
                if (q.isEmpty) {
                    db.collection("urunler").add(urunMap)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), R.string.urunBasariylaKayitEdildi, Toast.LENGTH_SHORT).show()
                            // İstersen kayıt sonrası ana sayfaya dönebilirsin
                            findNavController().popBackStack()
                        }
                        .addOnFailureListener { e -> Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show() }
                } else {
                    Toast.makeText(requireContext(), R.string.urunDahaOnceKayitEdilmis, Toast.LENGTH_SHORT).show()
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()  // Kamera executor'ı kapat
    }
}
