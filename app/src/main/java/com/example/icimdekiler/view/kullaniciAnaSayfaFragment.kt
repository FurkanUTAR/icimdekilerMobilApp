package com.example.icimdekiler.view

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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.icimdekiler.R
import com.example.icimdekiler.databinding.FragmentKullaniciAnaSayfaBinding
import com.example.icimdekiler.ui.AdminAnaSayfaScreen
import com.example.icimdekiler.ui.KullaniciAnaSayfaScreen
import com.example.icimdekiler.ui.theme.IcimdekilerTheme
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
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
                        onUrunAdiChange = { urunAdi = it },
                        onSearchClick = { urunAdiAra() },
                        onAddClick = { showAddOptionsDialog() },
                        onBarcodeClick = { showBarcodeOptionsDialog() },
                        onTumUrunlerClick = { kontrol("tumUrunler") },
                        onAtistirmalikClick = { kontrol("Atıştırmalık") },
                        onTemelGidaClick = { kontrol("Temel Gıda") },
                        onSutUrunleriClick = { kontrol("Süt ve Süt Ürünü") },
                        onIceceklerClick = { kontrol("İçecek") },
                        onSignOutConfirm = {
                            AlertDialog.Builder(requireContext())
                                .setTitle(R.string.cikisYap)
                                .setMessage(R.string.cikisYapmakIstediginizdenEminMisiniz)
                                .setPositiveButton(R.string.evet) { _, _ ->
                                    auth.signOut()
                                    findNavController().navigate(R.id.action_kullaniciAnaSayfaFragment_to_girisYapFragment)
                                }
                                .setNegativeButton(R.string.iptal, null)
                                .show()
                        },
                        onAyarlarClick = {findNavController().navigate(R.id.action_kullaniciAnaSayfaFragment_to_ayarlarFragment)}
                    )
                }
            }
        }
    }

    private fun showBarcodeOptionsDialog() {
        val secim = arrayOf(getString(R.string.kamera), getString(R.string.galeri))
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.secimYap)
            .setItems(secim) { _, which ->
                if (which == 0) showBarcodeScannerDialog() else barkodOkuGaleri()
            }
            .show()
    }

    private fun showAddOptionsDialog() {
        val secim = arrayOf(getString(R.string.urunEkle), getString(R.string.icerikEkle))
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.secimYap)
            .setItems(secim) { _, which ->
                if (which == 0) {
                    val action = adminAnaSayfaFragmentDirections
                        .actionAdminAnaSayfaFragmentToUrunEkleFragment("yeni", "", "", "", "", "")
                    findNavController().navigate(action)
                } else {
                    val action = adminAnaSayfaFragmentDirections.actionAdminAnaSayfaFragmentToIcerikEkleFragment()
                    findNavController().navigate(action)
                }
            }
            .show()
    }

    fun kontrol(kategori : String){
        val currentUser = auth.currentUser
        if (currentUser != null) {
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
                        Toast.makeText(requireContext(), R.string.beklenmedikBirHataOlustu, Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), R.string.beklenmedikBirHataOlustu, Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), R.string.beklenmedikBirHataOlustu, Toast.LENGTH_SHORT).show()
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
                .replace("ç", "c")
                .replace("ğ", "g")
                .replace("ı", "i")
                .replace("ö", "o")
                .replace("ş", "s")
                .replace("ü", "u")
                .trim()

            db.collection("urunler")
                .whereEqualTo("urunAdiLowerCase", urunAdiLowerCase)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    try {
                        if (!querySnapshot.isEmpty) {
                            val document = querySnapshot.documents.firstOrNull()
                            barkodNo = document?.getString("barkodNo") ?: ""
                            val urunAdi = document?.getString("urunAdi") ?: ""
                            val icindekiler = document?.getString("icindekiler") ?: ""
                            var gorselUrl = document?.getString("gorselUrl") ?: ""

                            val currentFragment = findNavController().currentDestination?.id
                            val targetFragment = R.id.urunEkleFragment

                            if (currentFragment != targetFragment) {
                                val action = kullaniciAnaSayfaFragmentDirections.actionKullaniciAnaSayfaFragmentToUrunFragment(barkodNo, urunAdi, icindekiler, gorselUrl)
                                findNavController().navigate(action)
                            } else Log.d("NavigationDebug", "Zaten urunEkleFragment içindesin, tekrar yönlendirme yapılmadı.")
                        } else {
                            try {
                                Toast.makeText(requireContext(), R.string.urunBulunamadi, Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.addOnFailureListener { exepion ->
                    try {
                        Toast.makeText(requireContext(), exepion.localizedMessage, Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun barkodNoAra() {
        try {
            db.collection("urunler")
                .whereEqualTo("barkodNo", barkodNo)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    try {
                        if (!querySnapshot.isEmpty) {
                            val document = querySnapshot.documents.firstOrNull()
                            val urunAdi = document?.getString("urunAdi") ?: ""
                            val icindekiler = document?.getString("icindekiler") ?: ""
                            var gorselUrl = document?.getString("gorselUrl") ?: ""

                            val action = kullaniciAnaSayfaFragmentDirections.actionKullaniciAnaSayfaFragmentToUrunFragment(barkodNo, urunAdi, icindekiler, gorselUrl)
                            if (findNavController().currentDestination?.id != R.id.urunFragment) {
                                findNavController().navigate(action)
                            }
                        } else {
                            try {
                                Toast.makeText(requireContext(), R.string.urunBulunamadi, Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.addOnFailureListener { exeption ->
                    try {
                        Toast.makeText(requireContext(), exeption.localizedMessage, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
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