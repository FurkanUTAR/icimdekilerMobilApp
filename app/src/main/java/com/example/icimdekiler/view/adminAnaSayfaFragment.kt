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
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.icimdekiler.R
import com.example.icimdekiler.databinding.FragmentAdminAnaSayfaBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class adminAnaSayfaFragment : Fragment() {

    private var _binding: FragmentAdminAnaSayfaBinding? = null
    private val binding get() = _binding!!

    private var urunlerListener: ListenerRegistration? = null  // Dinleyiciyi saklamak için

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

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
            Log.e("AdminAnaSayfa", "Initialization error", e)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            _binding = FragmentAdminAnaSayfaBinding.inflate(inflater, container, false)
            return binding.root
        } catch (e: Exception) {
            Log.e("AdminAnaSayfa", "View creation error", e)
            return null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            urunleriAl()

            binding.popupMenu.setOnClickListener { v ->
                try {
                    val popupMenu = PopupMenu(v.context, v)
                    popupMenu.menuInflater.inflate(R.menu.menu_fab, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener { item ->
                        try {
                            when (item.itemId) {
                                R.id.cikisYap -> {
                                    AlertDialog.Builder(v.context)
                                        .setTitle(R.string.cikisYap)
                                        .setMessage(R.string.cikisYapmakIstediginizdenEminMisiniz)
                                        .setPositiveButton(R.string.evet) { dialog, value ->
                                            try {
                                                auth.signOut()
                                                findNavController().navigate(
                                                    R.id.action_adminAnaSayfaFragment_to_girisYapFragment,
                                                    null,
                                                    NavOptions.Builder()
                                                        .setPopUpTo(R.id.adminAnaSayfaFragment, true)
                                                        .setLaunchSingleTop(true)
                                                        .build()
                                                )
                                            } catch (e: Exception) {
                                                Log.e("AdminAnaSayfa", "Logout error", e)
                                            }
                                        }
                                        .setNegativeButton(R.string.iptal, null)
                                        .show()
                                    true
                                }
                                else -> false
                            }
                        } catch (e: Exception) {
                            Log.e("AdminAnaSayfa", "Popup menu error", e)
                            false
                        }
                    }
                    popupMenu.show()
                } catch (e: Exception) {
                    Log.e("AdminAnaSayfa", "Popup menu error", e)
                }
            }

            binding.barkodOkuImageView.setOnClickListener {
                try {
                    val secim = arrayOf(
                        getString(R.string.kamera),
                        getString(R.string.galeri)
                    )
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.secimYap)
                        .setItems(secim) { dialog, which ->
                            try {
                                if (which == 0) showBarcodeScannerDialog()
                                else barkodOkuGaleri()
                            } catch (e: Exception) {
                                Log.e("AdminAnaSayfa", "Barcode option error", e)
                            }
                        }.show()
                } catch (e: Exception) {
                    Log.e("AdminAnaSayfa", "Barcode dialog error", e)
                }
            }

            binding.araImage.setOnClickListener {
                try {
                    urunAdiAra()
                } catch (e: Exception) {
                    Log.e("AdminAnaSayfa", "Search error", e)
                }
            }

            binding.ekleImage.setOnClickListener {
                try {
                    val secim = arrayOf(
                        getString(R.string.urunEkle),
                        getString(R.string.icerikEkle)
                    )
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.secimYap)
                        .setItems(secim) { dialog, which ->
                            try {
                                if (which == 0) {
                                    val action = adminAnaSayfaFragmentDirections
                                        .actionAdminAnaSayfaFragmentToUrunEkleFragment(
                                            "yeni", "", "", "", "", "")
                                    findNavController().navigate(action)
                                } else {
                                    val action = adminAnaSayfaFragmentDirections
                                        .actionAdminAnaSayfaFragmentToIcerikEkleFragment()
                                    findNavController().navigate(action)
                                }
                            } catch (e: Exception) {
                                Log.e("AdminAnaSayfa", "Add option error", e)
                            }
                        }.show()
                } catch (e: Exception) {
                    Log.e("AdminAnaSayfa", "Add dialog error", e)
                }
            }

            binding.tumUrunlerButton.setOnClickListener {
                try {
                    val action = adminAnaSayfaFragmentDirections
                        .actionAdminAnaSayfaFragmentToAdminTumUrunlerFragment()
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    Log.e("AdminAnaSayfa", "Navigation error", e)
                }
            }
        } catch (e: Exception) {
            Log.e("AdminAnaSayfa", "View setup error", e)
        }
    }

    private fun showBarcodeScannerDialog() {
        try {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.CAMERA
                    )
                ) {
                    Snackbar.make(requireView(), R.string.barkodOkumakIcinKamerayaErisimIzniGerekli, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.izinVer) {
                            try {
                                requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
                            } catch (e: Exception) {
                                Log.e("AdminAnaSayfa", "Permission request error", e)
                            }
                        }.show()
                } else {
                    try {
                        requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
                    } catch (e: Exception) {
                        Log.e("AdminAnaSayfa", "Permission request error", e)
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
                            Log.e("AdminAnaSayfa", "Dialog close error", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AdminAnaSayfa", "Camera dialog error", e)
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
                    Log.e("AdminAnaSayfa", "Camera setup error", e)
                }
            }, ContextCompat.getMainExecutor(requireContext()))
        } catch (e: Exception) {
            Log.e("AdminAnaSayfa", "Camera start error", e)
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
                                                barkodNoAra()
                                                dialog.dismiss()
                                            } catch (e: Exception) {
                                                Log.e("AdminAnaSayfa", "Barcode success error", e)
                                            }
                                        }
                                        break
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AdminAnaSayfa", "Barcode processing error", e)
                        }
                    }
                    .addOnFailureListener { e ->
                        requireActivity().runOnUiThread {
                            try {
                                Toast.makeText(
                                    requireContext(),
                                    e.localizedMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: Exception) {
                                Log.e("AdminAnaSayfa", "Barcode failure error", e)
                            }
                        }
                    }
                    .addOnCompleteListener {
                        try {
                            imageProxy.close()
                        } catch (e: Exception) {
                            Log.e("AdminAnaSayfa", "Image proxy close error", e)
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e("AdminAnaSayfa", "Image analysis error", e)
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
                        Log.e("AdminAnaSayfa", "Camera restart error", e)
                    }
                }
            } else {
                try {
                    Toast.makeText(
                        requireContext(),
                        "Kamera izni gerekiyor",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Log.e("AdminAnaSayfa", "Permission toast error", e)
                }
            }
        } catch (e: Exception) {
            Log.e("AdminAnaSayfa", "Permission result error", e)
        }
    }

    private fun urunleriAl() {
        urunlerListener = db.collection("urunler")
            .orderBy("urunAdiLowerCase", Query.Direction.ASCENDING)
            .limit(30)
            .addSnapshotListener { value, error ->
                val bind = binding // Null kontrolü

                if (error != null) {
                    Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (value != null && !value.isEmpty) {
                    val urunAdlari = ArrayList<String>()
                    for (document in value.documents) {
                        val urunAdi = document.getString("urunAdi") ?: ""
                        urunAdlari.add(urunAdi)
                    }

                    // AutoComplete Adapter
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, urunAdlari)
                    bind.urunAdiText.setAdapter(adapter)
                }
            }
    }

    private fun urunAdiAra() {
        try {
            val urunAdiLowerCase = binding.urunAdiText.text.toString()
                .lowercase(Locale("tr","TR"))
                .replace("ç", "c")
                .replace("ğ", "g")
                .replace("ı", "i")
                .replace("ö", "o")
                .replace("ş", "s")
                .replace("ü", "u")
                .trim()

            try {
                db.collection("urunler")
                    .whereEqualTo("urunAdiLowerCase", urunAdiLowerCase)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        try {
                            if (!querySnapshot.isEmpty) {
                                try {
                                    val document = querySnapshot.documents.firstOrNull()
                                    val documentId = document?.id ?: ""
                                    barkodNo = document?.getString("barkodNo") ?: ""
                                    val urunAdi = document?.getString("urunAdi") ?: ""
                                    val icindekiler = document?.getString("icindekiler") ?: ""
                                    val gorselUrl = document?.getString("gorselUrl") ?: ""

                                    try {
                                        val currentFragment = findNavController().currentDestination?.id
                                        val targetFragment = R.id.urunEkleFragment

                                        if (currentFragment != targetFragment) {
                                            val action = adminAnaSayfaFragmentDirections
                                                .actionAdminAnaSayfaFragmentToUrunEkleFragment(
                                                    "eski", barkodNo, urunAdi, icindekiler, gorselUrl, documentId
                                                )
                                            findNavController().navigate(action)
                                        } else {
                                            Log.d("NavigationDebug", "Already in urunEkleFragment")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("AdminAnaSayfa", "Navigation error", e)
                                    }
                                } catch (e: Exception) {
                                    Log.e("AdminAnaSayfa", "Document processing error", e)
                                }
                            } else {
                                try {
                                    Toast.makeText(requireContext(), R.string.urunBulunamadi, Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("AdminAnaSayfa", "Toast error", e)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AdminAnaSayfa", "Query success processing error", e)
                        }
                    }
                    .addOnFailureListener { exception ->
                        try {
                            Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Log.e("AdminAnaSayfa", "Toast error", e)
                        }
                    }
            } catch (e: Exception) {
                Log.e("AdminAnaSayfa", "Firestore query error", e)
            }
        } catch (e: Exception) {
            Log.e("AdminAnaSayfa", "Search processing error", e)
        }
    }

    private fun barkodNoAra() {
        try {
            try {
                db.collection("urunler")
                    .whereEqualTo("barkodNo", barkodNo)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        try {
                            if (!querySnapshot.isEmpty) {
                                try {
                                    val document = querySnapshot.documents.firstOrNull()
                                    val documentId = document?.id ?: ""
                                    val urunAdi = document?.getString("urunAdi") ?: ""
                                    val icindekiler = document?.getString("icindekiler") ?: ""
                                    val gorselUrl = document?.getString("gorselUrl") ?: ""

                                    try {
                                        val action = adminAnaSayfaFragmentDirections
                                            .actionAdminAnaSayfaFragmentToUrunEkleFragment(
                                                "eski", barkodNo, urunAdi, icindekiler, gorselUrl, documentId
                                            )

                                        if (findNavController().currentDestination?.id != R.id.urunEkleFragment) {
                                            findNavController().navigate(action)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("AdminAnaSayfa", "Navigation error", e)
                                    }
                                } catch (e: Exception) {
                                    Log.e("AdminAnaSayfa", "Document processing error", e)
                                }
                            } else {
                                try {
                                    val action = adminAnaSayfaFragmentDirections
                                        .actionAdminAnaSayfaFragmentToUrunEkleFragment(
                                            "yeni", barkodNo, "", "", "", ""
                                        )
                                    if (findNavController().currentDestination?.id != R.id.urunEkleFragment) {
                                        findNavController().navigate(action)
                                    }
                                } catch (e: Exception) {
                                    Log.e("AdminAnaSayfa", "Navigation error", e)
                                }

                                try {
                                    Toast.makeText(
                                        requireContext(),
                                        R.string.urunBulunamadi,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: Exception) {
                                    Log.e("AdminAnaSayfa", "Toast error", e)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AdminAnaSayfa", "Query success processing error", e)
                        }
                    }
                    .addOnFailureListener { exception ->
                        try {
                            Toast.makeText(
                                requireContext(),
                                exception.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Log.e("AdminAnaSayfa", "Toast error", e)
                        }
                    }
            } catch (e: Exception) {
                Log.e("AdminAnaSayfa", "Firestore query error", e)
            }
        } catch (e: Exception) {
            Log.e("AdminAnaSayfa", "Barcode search processing error", e)
        }
    }

    private fun barkodOkuGaleri() {
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                try {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.READ_MEDIA_IMAGES
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        try {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(
                                    requireActivity(),
                                    Manifest.permission.READ_MEDIA_IMAGES
                                )
                            ) {
                                try {
                                    Snackbar.make(
                                        requireView(),
                                        R.string.barkodOkumakIcinGaleriyeErisimIzniGerekli,
                                        Snackbar.LENGTH_INDEFINITE
                                    )
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
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        try {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(
                                    requireActivity(),
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                )
                            ) {
                                try {
                                    Snackbar.make(
                                        requireView(),
                                        R.string.barkodOkumakIcinGaleriyeErisimIzniGerekli,
                                        Snackbar.LENGTH_INDEFINITE
                                    )
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
                    if (result.resultCode == AppCompatActivity.RESULT_OK) {
                        try {
                            val imageUri = result.data?.data
                            if (imageUri != null) {
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
                                                            try {
                                                                barkodNoAra()
                                                            } catch (e: Exception) {
                                                                Log.e("AdminAnaSayfa", "Barcode search error", e)
                                                            }
                                                            break
                                                        }
                                                    }
                                                } else {
                                                    try {
                                                        Toast.makeText(
                                                            requireContext(),
                                                            R.string.barkodOkunamadi,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } catch (e: Exception) {
                                                        Log.e("AdminAnaSayfa", "Toast error", e)
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Log.e("AdminAnaSayfa", "Barcode success processing error", e)
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            try {
                                                Toast.makeText(
                                                    requireContext(),
                                                    e.localizedMessage,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } catch (e: Exception) {
                                                Log.e("AdminAnaSayfa", "Toast error", e)
                                            }
                                        }
                                } catch (e: Exception) {
                                    Log.e("AdminAnaSayfa", "Image processing error", e)
                                }
                            } else {
                                try {
                                    Toast.makeText(
                                        requireContext(),
                                        R.string.gorselBulunamadi,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: Exception) {
                                    Log.e("AdminAnaSayfa", "Toast error", e)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AdminAnaSayfa", "Activity result processing error", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AdminAnaSayfa", "Activity result handling error", e)
                }
            }

            permissionLauncherGallery = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                try {
                    if (result) {
                        try {
                            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            activityResultLauncherGallery.launch(intent)
                        } catch (e: Exception) {
                            Log.e("AdminAnaSayfa", "Gallery intent error", e)
                        }
                    } else {
                        try {
                            Toast.makeText(
                                requireContext(),
                                R.string.galeriIzniVerilmedi,
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Log.e("AdminAnaSayfa", "Toast error", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AdminAnaSayfa", "Permission result handling error", e)
                }
            }
        } catch (e: Exception) {
            Log.e("AdminAnaSayfa", "Launcher registration error", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        urunlerListener?.remove()  // Dinleyiciyi kaldır
        _binding = null  // ViewBinding'i temizle
        cameraExecutor.shutdown()  // Kamera executor'ı kapat
    }
}
