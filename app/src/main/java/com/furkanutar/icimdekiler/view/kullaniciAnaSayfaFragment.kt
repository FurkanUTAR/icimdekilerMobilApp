package com.furkanutar.icimdekiler.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.api.FatSecretClient
import com.furkanutar.icimdekiler.api.RetrofitClient
import com.furkanutar.icimdekiler.ui.KaynakSecimDialog
import com.furkanutar.icimdekiler.ui.KullaniciAnaSayfaScreen
import com.furkanutar.icimdekiler.ui.OzelAlertDialog
import com.furkanutar.icimdekiler.ui.theme.IcimdekilerTheme
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class kullaniciAnaSayfaFragment : Fragment() {

    private var urunAdi by mutableStateOf("")

    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    private lateinit var permissionLauncherGallery: ActivityResultLauncher<String>
    private lateinit var activityResultLauncherGallery: ActivityResultLauncher<Intent>

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

    private var camera: Camera? = null
    private var isFlashOn = false  // Flash durumu

    private var barkodNo: String = ""

    private var showLogoutDialog by mutableStateOf(false)
    private var showSourceDialog by mutableStateOf(false)
    private var isBarcodeAction by mutableStateOf(false)
    private var kullaniciAdSoyad by mutableStateOf("Kullanıcı")
    private var kullaniciEmail by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            auth = Firebase.auth
            cameraExecutor = Executors.newSingleThreadExecutor()
            barcodeScanner = BarcodeScanning.getClient()
            registerLauncherGallery()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                IcimdekilerTheme {
                    KullaniciAnaSayfaScreen(
                        urunAdi = urunAdi,
                        // Firebase'den mevcut kullanıcıyı kontrol ediyoruz
                        isLoggedIn = auth.currentUser != null,
                        kullaniciAdSoyad = kullaniciAdSoyad,
                        kullaniciEmail = kullaniciEmail,

                        // Kalori değerlerini şimdilik sabit (veya ViewModel'den) veriyoruz
                        gunlukKaloriHedefi = 2000,
                        harcananKalori = 450,

                        onUrunAdiChange = { urunAdi = it },
                        onSearchClick = { urunAdiAra() },
                        onBarcodeClick = {
                            isBarcodeAction = true
                            showSourceDialog = true
                        },
                        onTumUrunlerClick = { kontrol("tumUrunler") },
                        onAtistirmalikClick = { kontrol("Atıştırmalık") },
                        onTemelGidaClick = { kontrol("Temel Gıda") },
                        onSutUrunleriClick = { kontrol("Süt ve Süt Ürünü") },
                        onIceceklerClick = { kontrol("İçecek") },

                        // Yan menüden tetiklenen aksiyonlar
                        onSignOutConfirm = { showLogoutDialog = true },
                        onAyarlarClick = {
                            findNavController().navigate(R.id.action_kullaniciAnaSayfaFragment_to_ayarlarFragment)
                        },
                        onLoginClick = {
                            findNavController().navigate(R.id.action_kullaniciAnaSayfaFragment_to_girisYapFragment)
                        },
                        onRegisterClick = {
                            findNavController().navigate(R.id.action_kullaniciAnaSayfaFragment_to_kayitOlFragment)
                        },
                        onGunlukTakipClick = {
                            findNavController().navigate(R.id.action_kullaniciAnaSayfaFragment_to_gunlukTakipFragment)
                        }
                    )
                }

                if (showSourceDialog){
                    KaynakSecimDialog(
                        isBarcodeAction = isBarcodeAction,
                        onDismiss = { showSourceDialog = false },
                        onOption1 = {
                            if (isBarcodeAction) showBarcodeScannerDialog()
                        },
                        onOption2 = { if (isBarcodeAction)  barkodOkuGaleri() }
                    )
                }

                if (showLogoutDialog){
                    OzelAlertDialog(
                        baslik = getString(R.string.cikisYapmakIstediginizdenEminMisiniz),
                        onayButonMetni = getString(R.string.evet),
                        iptalButonMetni = getString(R.string.hayir),
                        onayButonRengi = Color.Red,
                        onDismiss = { showLogoutDialog = false },
                        onConfirm = {
                            showLogoutDialog = false
                            auth.signOut()
                            kullaniciBilgileriniYukle()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        kullaniciBilgileriniYukle()
    }

    private fun kullaniciBilgileriniYukle() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            kullaniciAdSoyad = getString(R.string.misafirKullanici)
            kullaniciEmail = ""
            return
        }

        kullaniciEmail = currentUser.email.orEmpty()

        db.collection("kullaniciBilgileri")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener

                val isimSoyisim = document.getString("isimSoyisim")?.takeIf { it.isNotBlank() }

                kullaniciAdSoyad = isimSoyisim ?: "Kullanıcı"
                kullaniciEmail = document.getString("ePosta")?.takeIf { it.isNotBlank() }
                    ?: currentUser.email.orEmpty()
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                kullaniciAdSoyad = currentUser.displayName ?: "Kullanıcı"
                kullaniciEmail = currentUser.email.orEmpty()
            }
    }

    fun kontrol(kategori : String){
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val action = kullaniciAnaSayfaFragmentDirections
                .actionKullaniciAnaSayfaFragmentToKullaniciTumUrunlerFragment(kategori)
            findNavController().navigate(action)
            return
        }

        // Kullanıcı bilgileri Firestore'dan çek
        db.collection("kullaniciBilgileri")
            .whereEqualTo("kullaniciUID", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val kullanici = documents.documents.first()
                    val isAdmin = kullanici.getBoolean("isAdmin") ?: false

                    // Yönlendirme işlemi
                    if (isAdmin){
                        val action = adminAnaSayfaFragmentDirections.actionAdminAnaSayfaFragmentToAdminTumUrunlerFragment(kategori)
                        requireView().findNavController().navigate(action)
                    } else {
                        val action = kullaniciAnaSayfaFragmentDirections.actionKullaniciAnaSayfaFragmentToKullaniciTumUrunlerFragment(kategori)
                        requireView().findNavController().navigate(action)
                    }
                } else {
                    val action = kullaniciAnaSayfaFragmentDirections
                        .actionKullaniciAnaSayfaFragmentToKullaniciTumUrunlerFragment(kategori)
                    findNavController().navigate(action)
                }
            }.addOnFailureListener {
                val action = kullaniciAnaSayfaFragmentDirections
                    .actionKullaniciAnaSayfaFragmentToKullaniciTumUrunlerFragment(kategori)
                findNavController().navigate(action)
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
                                Log.e("KullanıcıAnaSayfa", "Permission request error", e)
                            }
                        }.show()
                } else {
                    try {
                        requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
                    } catch (e: Exception) {
                        Log.e("KullanıcıAnaSayfa", "Permission request error", e)
                    }
                }
            } else {
                try {
                    val dialog = BottomSheetDialog(requireContext(), R.style.Theme_Icimdekiler_BottomSheetDialog)
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
                            Log.e("KullanıcıAnaSayfa", "Dialog close error", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("KullanıcıAnaSayfa", "Camera dialog error", e)
                }
            }
        } catch (e: Exception) {
            Log.e("KullanıcıAnaSayfa", "Barcode scanner error", e)
        }
    }

    private fun startCamera(previewView: PreviewView, dialog: BottomSheetDialog, btnFlashToggle: Button?) {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = androidx.camera.core.Preview.Builder()
                        .setTargetRotation(previewView.display.rotation)
                        .build()
                        .also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
                                try {
                                    analyzeImage(imageProxy, dialog)
                                } catch (e: Exception) {
                                    Log.e("KullanıcıAnaSayfa", "Image analysis error", e)
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
                    Log.e("KullanıcıAnaSayfa", "Camera setup error", e)
                }
            }, ContextCompat.getMainExecutor(requireContext()))
        } catch (e: Exception) {
            Log.e("KullanıcıAnaSayfa", "Camera start error", e)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeImage(imageProxy: ImageProxy, dialog: BottomSheetDialog) {
        try {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

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
                                                barkodNoAra()
                                                dialog.dismiss()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                        break
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    .addOnFailureListener { e ->
                        requireActivity().runOnUiThread {
                            try {
                                Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    .addOnCompleteListener {
                        try {
                            imageProxy.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
                        e.printStackTrace()
                    }
                }
            } else {
                try {
                    Toast.makeText(requireContext(),R.string.barkodOkumakIcinKamerayaErisimIzniGerekli, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun urunAdiAra() {
        try {
            val urunAdiLowerCase = urunAdi
                .lowercase(Locale("tr","TR"))
                .replace("ç", "c").replace("ğ", "g").replace("ı", "i")
                .replace("ö", "o").replace("ş", "s").replace("ü", "u")
                .trim()

            db.collection("urunler")
                .whereEqualTo("urunAdiLowerCase", urunAdiLowerCase)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    try {
                        if (!querySnapshot.isEmpty) {
                            val document = querySnapshot.documents.firstOrNull()
                            barkodNo      = document?.getString("barkodNo")  ?: ""
                            val urunAdi   = document?.getString("urunAdi")   ?: ""
                            val icindekiler = document?.getString("icindekiler") ?: ""
                            val gorselUrl = document?.getString("gorselUrl")  ?: ""
                            val kalori        = document?.getLong("kalori")?.toInt() ?: 0
                            val protein       = (document?.getDouble("protein")       ?: 0.0).toFloat()
                            val karbonhidrat  = (document?.getDouble("karbonhidrat")  ?: 0.0).toFloat()
                            val yag           = (document?.getDouble("yag")           ?: 0.0).toFloat()

                            if (findNavController().currentDestination?.id != R.id.urunEkleFragment) {
                                val action = kullaniciAnaSayfaFragmentDirections
                                    .actionKullaniciAnaSayfaFragmentToUrunFragment(
                                        barkodNo, urunAdi, icindekiler, gorselUrl,
                                        kalori, protein, karbonhidrat, yag
                                    )
                                findNavController().navigate(action)
                            }
                        } else {
                            Toast.makeText(requireContext(), R.string.urunBulunamadi, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun barkodNoAra() {
        try {
            db.collection("urunler")
                .whereEqualTo("barkodNo", barkodNo)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    try {
                        if (!querySnapshot.isEmpty) {
                            val document      = querySnapshot.documents.firstOrNull()
                            val urunAdi       = document?.getString("urunAdi")    ?: ""
                            val icindekiler   = document?.getString("icindekiler") ?: ""
                            val gorselUrl     = document?.getString("gorselUrl")   ?: ""
                            val kalori        = document?.getLong("kalori")?.toInt() ?: 0
                            val protein       = (document?.getDouble("protein")      ?: 0.0).toFloat()
                            val karbonhidrat  = (document?.getDouble("karbonhidrat") ?: 0.0).toFloat()
                            val yag           = (document?.getDouble("yag")          ?: 0.0).toFloat()

                            val action = kullaniciAnaSayfaFragmentDirections
                                .actionKullaniciAnaSayfaFragmentToUrunFragment(
                                    barkodNo, urunAdi, icindekiler, gorselUrl,
                                    kalori, protein, karbonhidrat, yag
                                )
                            if (findNavController().currentDestination?.id != R.id.urunFragment) {
                                findNavController().navigate(action)
                            }
                        } else {
                            offApiSorgu(barkodNo)
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun offApiSorgu(barkodNo: String) {
        lifecycleScope.launch {
            try {
                // 1. OpenFoodFacts → görsel + içindekiler
                val yanit = RetrofitClient.api.getUrun(barkodNo)

                if (yanit.durum == 1 && yanit.urun != null) {
                    val apiUrun    = yanit.urun
                    val gelenAd    = apiUrun.urunAdi ?: ""
                    val gelenMarka = apiUrun.marka   ?: ""
                    val gelenIcerik = apiUrun.icindekilerTr ?: apiUrun.icindekilerGenel ?: ""
                    val gelenGorsel = apiUrun.gorselUrl ?: ""
                    val tamUrunAdi  = "$gelenMarka $gelenAd".trim()

                    Log.d("OFF_Sorgu", "OFF bulundu: $tamUrunAdi")

                    // 2. FatSecret → besin değerleri (hata veya eşleşmeme olursa null döner)
                    var fsKalori       = 0
                    var fsProtein      = 0f
                    var fsKarb         = 0f
                    var fsYag          = 0f
                    
                    try {
                        val besin = FatSecretClient.besinDegerleriniAl(urunAdi = tamUrunAdi, barkodNo = barkodNo)
                        if (besin != null) {
                            fsKalori  = besin.calories?.toDoubleOrNull()?.toInt() ?: 0
                            fsProtein = besin.protein?.toFloatOrNull()            ?: 0f
                            fsKarb    = besin.carbohydrate?.toFloatOrNull()       ?: 0f
                            fsYag     = besin.fat?.toFloatOrNull()                ?: 0f
                            Log.d("FatSecret", "$tamUrunAdi (FS) → Kal=$fsKalori P=$fsProtein K=$fsKarb Y=$fsYag")
                        } else {
                            Log.w("FatSecret", "FatSecret eşleşmesi bulunamadı, OpenFoodFacts verileri kullanılıyor.")
                            val nut = apiUrun.nutriments
                            if (nut != null) {
                                fsKalori  = nut.enerjiKcal100g?.toInt() ?: 0
                                fsProtein = nut.protein100g?.toFloat() ?: 0f
                                fsKarb    = nut.karbonhidrat100g?.toFloat() ?: 0f
                                fsYag     = nut.yag100g?.toFloat() ?: 0f
                                Log.d("OFF_Nutriments", "$tamUrunAdi (OFF) → Kal=$fsKalori P=$fsProtein K=$fsKarb Y=$fsYag")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FatSecret", "Besin değerleri alınamadı: ${e.message}")
                    }

                    // 3. Yönlendir
                    val action = kullaniciAnaSayfaFragmentDirections
                        .actionKullaniciAnaSayfaFragmentToUrunFragment(
                            barkodNo    = barkodNo,
                            urunAdi     = tamUrunAdi,
                            icindekiler = gelenIcerik,
                            gorselUrl   = gelenGorsel,
                            kalori      = fsKalori,
                            protein     = fsProtein,
                            karbonhidrat = fsKarb,
                            yag         = fsYag
                        )

                    if (findNavController().currentDestination?.id == R.id.kullaniciAnaSayfaFragment) {
                        findNavController().navigate(action)
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.urunBulunamadi, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("OFF_Sorgu", "Hata: ${e.localizedMessage}")
            }
        }
    }
    
    private fun barkodOkuGaleri() {
        try {
            if(Build.VERSION.SDK_INT >= 33){
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)) {
                        Snackbar.make(requireView(), R.string.barkodOkumakIcinGaleriyeErisimIzniGerekli, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.izinVer) {
                                try {
                                    permissionLauncherGallery.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }.show()
                    } else {
                        try {
                            permissionLauncherGallery.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    try {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        activityResultLauncherGallery.launch(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }else{
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Snackbar.make(requireView(), R.string.barkodOkumakIcinGaleriyeErisimIzniGerekli, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.izinVer) {
                                try {
                                    permissionLauncherGallery.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }.show()
                    } else {
                        try {
                            permissionLauncherGallery.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    try {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        activityResultLauncherGallery.launch(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun registerLauncherGallery() {
        try {
            activityResultLauncherGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                try {
                    if (result.resultCode == AppCompatActivity.RESULT_OK) {
                        val imageUri = result.data?.data
                        if (imageUri!=null){
                            try {
                                val image = InputImage.fromFilePath(requireContext(), imageUri)
                                val scanner = BarcodeScanning.getClient()

                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        try {
                                            if (barcodes.isNotEmpty()) {
                                                for (barcode in barcodes) {
                                                    val barkod = barcode.displayValue
                                                    if (barkod != null) {
                                                        barkodNo = barkod
                                                        barkodNoAra()
                                                        break
                                                    }
                                                }
                                            } else {
                                                try {
                                                    Toast.makeText(requireContext(), R.string.barkodOkunamadi, Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }.addOnFailureListener { e ->
                                        try {
                                            Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            try {
                                Toast.makeText(requireContext(), R.string.gorselBulunamadi, Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            permissionLauncherGallery = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                try {
                    if (result) {
                        try {
                            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            activityResultLauncherGallery.launch(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        try {
                            Toast.makeText(requireContext(), R.string.galeriIzniVerilmedi, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            cameraExecutor.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}