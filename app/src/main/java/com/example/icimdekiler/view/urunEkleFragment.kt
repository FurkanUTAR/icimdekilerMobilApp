package com.example.icimdekiler.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import com.example.icimdekiler.databinding.FragmentUrunEkleBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.navigation.fragment.findNavController
import com.example.icimdekiler.R
import androidx.core.graphics.scale
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.squareup.picasso.Picasso

class urunEkleFragment : Fragment() {

    // Binding
    private var _binding: FragmentUrunEkleBinding? = null
    private val binding get() = _binding!!

    // Firebase
    val db = Firebase.firestore
    val storage = Firebase.storage

    private lateinit var permissionLauncherGallery: ActivityResultLauncher<String>
    private lateinit var activityResultLauncherGallery: ActivityResultLauncher<Intent>

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

    private var camera: Camera? = null
    private var isFlashOn = false  // Flash durumu

    private val icerikListesi = ArrayList<String>()
    private lateinit var icerikAdapter: ArrayAdapter<String>

    private val icindekilerListesi = mutableListOf<String>()
    private lateinit var icindekilerAdapter: ArrayAdapter<String>

    var secilenGorsel: Uri? = null
    var secilenBitmap: Bitmap? = null

    var barkodNo: String = ""
    var islem: String = ""
    var documentId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        barcodeScanner = BarcodeScanning.getClient() // Barkod tarayıcıyı başlat
        registerLauncherGallery()
        icerikAl()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUrunEkleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        icindekilerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, icindekilerListesi)
        binding.icindekilerListView.adapter = icindekilerAdapter

        arguments?.let {
            val durum = urunEkleFragmentArgs.fromBundle(it).durum
            barkodNo = urunEkleFragmentArgs.fromBundle(it).barkodNo
            val urunAdi = urunEkleFragmentArgs.fromBundle(it).urunAdi
            val gelenIcindekiler = urunEkleFragmentArgs.fromBundle(it).icindekiler
            val gorselUrl = urunEkleFragmentArgs.fromBundle(it).gorselUrl
            documentId = urunEkleFragmentArgs.fromBundle(it).documentId

            if (durum == "yeni") {
                binding.kaydetButton.isEnabled = true
                binding.guncelleButton.isEnabled = false
                binding.silButton.isEnabled = false
                binding.barkodNoText.setText(barkodNo)
            } else {
                binding.kaydetButton.isEnabled = false
                binding.guncelleButton.isEnabled = true
                binding.silButton.isEnabled = true

                binding.barkodNoText.setText(barkodNo)
                binding.urunAdiText.setText(urunAdi)

                val icindekiler = gelenIcindekiler.split(", ").map { it.trim() }
                icindekilerListesi.clear()
                icindekilerListesi.addAll(icindekiler)
                icindekilerAdapter.notifyDataSetChanged()
            }

            if (gorselUrl.isNotEmpty()) {
                Picasso.get().load(gorselUrl).fit().centerCrop().into(binding.gorselSecImageView)
            } else {
                binding.gorselSecImageView.setImageResource(R.drawable.insert_photo)
            }
        }

        binding.barkodOkuImageView.setOnClickListener {
            val secim = arrayOf(
                getString(R.string.kamera),
                getString(R.string.galeri)
            )
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle(R.string.secimYap)
            alert.setItems(secim) { dialog, which ->
                if (which == 0) showBarcodeScannerDialog()
                else {
                    islem = "barkodOku"
                    barkodOkuGaleri()
                }
            }.show()
        }

        binding.gorselSecImageView.setOnClickListener {
            islem = "gorselSec"
            barkodOkuGaleri()
        }

        binding.ekleImage.setOnClickListener {
            icindekilerListesi.add(icerikListesi[binding.icerikSpinner.selectedItemPosition])
            icindekilerAdapter.notifyDataSetChanged()
        }

        binding.kaydetButton.setOnClickListener {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle(R.string.kayitEtmekIstediginizdenEminMisiniz)
            alert.setPositiveButton(R.string.evet) { dialog, value -> urunKaydet() }
            alert.setNegativeButton(R.string.hayir, null).show()
        }

        binding.guncelleButton.setOnClickListener {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle(R.string.guncellemekIstediginizdenEminMisiniz)
            alert.setPositiveButton(R.string.evet) { dialog, value -> urunGuncelle() }
            alert.setNegativeButton(R.string.hayir, null).show()
        }

        binding.silButton.setOnClickListener {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle(R.string.silmekIstediginizdenEminMisiniz)
            alert.setPositiveButton(R.string.evet) { dialog, value -> urunSil() }
            alert.setNegativeButton(R.string.hayir, null).show()
        }

        binding.icindekilerListView.setOnItemClickListener { parent, view, position, id ->
            val urun = parent.getItemAtPosition(position) as String
            aciklamaGetir(urun, position)
        }
    }

    private fun showBarcodeScannerDialog() {
        try {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                    Snackbar.make(requireView(), R.string.barkodOkumakIcinKamerayaErisimIzniGerekli, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.izinVer) {
                            try {
                                requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
                            } catch (e: Exception) {
                                Log.e("ÜrünEkle", "Permission request error", e)
                            }
                        }.show()
                } else {
                    try {
                        requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
                    } catch (e: Exception) {
                        Log.e("ÜrünEkle", "Permission request error", e)
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
                            Log.e("ÜrünEkle", "Dialog close error", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ÜrünEkle", "Camera dialog error", e)
                }
            }
        } catch (e: Exception) {
            Log.e("ÜrünEkle", "Barcode scanner error", e)
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
                                    Log.e("ÜrünEkle", "Image analysis error", e)
                                }
                            }
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

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
                            btnFlashToggle.text = if (isFlashOn) "Flash Kapat" else "Flash Aç"
                        }

                    } catch (e: Exception) {
                        Log.e("CameraX", "Camera bind failed", e)
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Flash açılamadı: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ÜrünEkle", "Camera setup error", e)
                }
            }, ContextCompat.getMainExecutor(requireContext()))
        } catch (e: Exception) {
            Log.e("ÜrünEkle", "Camera start error", e)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeImage(imageProxy: ImageProxy, dialog: BottomSheetDialog) {
        try {
            // Görüntüyü alır.
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                // Görüntüyü ML Kit için uygun formata dönüştürür.
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                // Barkod tarayıcıyı kullanarak görüntüyü analiz eder.
                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        try {
                            if (barcodes.isNotEmpty()) {
                                for (barcode in barcodes) {
                                    val barkod = barcode.displayValue
                                    if (barkod != null) {
                                        requireActivity().runOnUiThread {
                                            binding.barkodNoText.setText(barkod)
                                            dialog.dismiss()
                                        }
                                        break // İlk barkodu bulunca döngüden çık.
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("CameraX", "Barkod analizi hatası", e)
                            requireActivity().runOnUiThread {
                                Toast.makeText(requireContext(), "Barkod analizi hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Hata durumunda kullanıcıya bilgi ver.
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnCompleteListener { imageProxy.close() } // Görüntüyü kapat.
            }
        } catch (e: Exception) {
            Log.e("CameraX", "Görüntü analizi hatası", e)
            Toast.makeText(requireContext(), "Görüntü analizi hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            imageProxy.close()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val dialog = BottomSheetDialog(requireContext())
                val previewView = dialog.findViewById<PreviewView>(R.id.previewView)
                if (previewView != null) {
                    val btnFlashToggle = view?.findViewById<Button>(R.id.btnFlashToggle)
                    startCamera(previewView, dialog, btnFlashToggle)
                }
            } else {
                Toast.makeText(requireContext(), "Kamera izni gerekiyor", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("CameraX", "İzin işleme hatası", e)
            Toast.makeText(requireContext(), "İzin kontrolü hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun barkodOkuGaleri() {
        try {
            if(Build.VERSION.SDK_INT >= 33){
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)) {
                        Snackbar.make(requireView(), R.string.barkodOkumakIcinGaleriyeErisimIzniGerekli, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.izinVer) {
                                permissionLauncherGallery.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            }.show()
                    } else {
                        permissionLauncherGallery.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                } else {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncherGallery.launch(intent)
                }
            } else {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Snackbar.make(requireView(), R.string.barkodOkumakIcinGaleriyeErisimIzniGerekli, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.izinVer) {
                                permissionLauncherGallery.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }.show()
                    } else {
                        permissionLauncherGallery.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                } else {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncherGallery.launch(intent)
                }
            }
        } catch (e: Exception) {
            Log.e("Gallery", "Galeriye erişim hatası", e)
            Toast.makeText(requireContext(), "Galeriye erişim hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerLauncherGallery() {
        try {
            activityResultLauncherGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                try {
                    if (result.resultCode == RESULT_OK) {
                        val imageUri = result.data?.data
                        if (imageUri != null) {
                            secilenGorsel = imageUri
                            val image = InputImage.fromFilePath(requireContext(), imageUri)
                            val scanner = BarcodeScanning.getClient()

                            if (islem == "barkodOku") {
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        try {
                                            if (barcodes.isNotEmpty()) {
                                                for (barcode in barcodes) {
                                                    val barkod = barcode.displayValue
                                                    if (barkod != null) {
                                                        binding.barkodNoText.setText(barkod)
                                                        break // İlk barkodu alınca döngüden çık
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(requireContext(), R.string.barkodOkunamadi, Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("Gallery", "Barkod işleme hatası", e)
                                            Toast.makeText(requireContext(), "Barkod işleme hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                                    }
                            } else if (islem == "gorselSec") {
                                try {
                                    if (Build.VERSION.SDK_INT >= 28) {
                                        val source = ImageDecoder.createSource(requireActivity().contentResolver, secilenGorsel!!)
                                        secilenBitmap = ImageDecoder.decodeBitmap(source)
                                        binding.gorselSecImageView.setImageBitmap(secilenBitmap)
                                    } else {
                                        secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, secilenGorsel)
                                        binding.gorselSecImageView.setImageBitmap(secilenBitmap)
                                    }
                                    if (secilenBitmap != null) {
                                        val targetWidth = binding.gorselSecImageView.width
                                        val targetHeight = binding.gorselSecImageView.height
                                        secilenBitmap = resizeBitmapWithAspectRatio(secilenBitmap!!, targetWidth, targetHeight)
                                    }
                                } catch (e: Exception) {
                                    Log.e("Gallery", "Görsel işleme hatası", e)
                                    Toast.makeText(requireContext(), "Görsel işleme hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(requireContext(), R.string.gorselBulunamadi, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Gallery", "Galeriye erişim hatası", e)
                    Toast.makeText(requireContext(), "Galeriye erişim hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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
                    Log.e("Gallery", "İzin işleme hatası", e)
                    Toast.makeText(requireContext(), "İzin kontrolü hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("Gallery", "Galeri launcher hatası", e)
            Toast.makeText(requireContext(), "Galeri başlatma hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resizeBitmapWithAspectRatio(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        try {
            val width = bitmap.width
            val height = bitmap.height

            // Oranları hesapla
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioTarget = targetWidth.toFloat() / targetHeight.toFloat()

            var finalWidth = targetWidth
            var finalHeight = targetHeight

            // Hangi boyuta göre ölçeklendirme yapılacağını belirle
            if (ratioTarget > ratioBitmap) {
                finalWidth = (targetHeight * ratioBitmap).toInt()
            } else {
                finalHeight = (targetWidth / ratioBitmap).toInt()
            }

            // Bitmap'i yeniden boyutlandır
            return bitmap.scale(finalWidth, finalHeight)
        } catch (e: Exception) {
            Log.e("Gallery", "Resim boyutlandırma hatası", e)
            Toast.makeText(requireContext(), "Resim boyutlandırma hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            return bitmap // Hata durumunda orijinal resmi döndür
        }
    }

    private fun urunKaydet() {
        try {
            val barkodNo = binding.barkodNoText.text.toString().trim()
            val urunAdi = binding.urunAdiText.text.toString().trim()
            val urunAdiLowerCase = urunAdi.lowercase().trim()
            val birlesikIcindekiler = icindekilerListesi.joinToString(", ").trim()

            if (barkodNo.isEmpty() || urunAdi.isEmpty() || birlesikIcindekiler.isEmpty()) {
                Toast.makeText(requireContext(), R.string.lutfenBosAlanBirakmayiniz, Toast.LENGTH_SHORT).show()
                return
            }

            val gorselAdi = "${barkodNo}.jpg"
            val gorselReferansi = storage.reference.child("images/$gorselAdi")

            if (secilenGorsel != null) {
                gorselReferansi.putFile(secilenGorsel!!)
                    .addOnSuccessListener {
                        gorselReferansi.downloadUrl.addOnSuccessListener { uri ->
                            val gorselUrl = uri.toString()
                            val urunMap = hashMapOf(
                                "urunAdi" to urunAdi,
                                "urunAdiLowerCase" to urunAdiLowerCase,
                                "barkodNo" to barkodNo,
                                "icindekiler" to birlesikIcindekiler,
                                "gorselUrl" to gorselUrl
                            )

                            db.collection("urunler")
                                .whereEqualTo("urunAdiLowerCase", urunAdiLowerCase)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (querySnapshot.isEmpty) {
                                        db.collection("urunler")
                                            .add(urunMap)
                                            .addOnSuccessListener {
                                                Toast.makeText(requireContext(), R.string.urunBasariylaKayitEdildi, Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { exception ->
                                                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(requireContext(), R.string.urunDahaOnceKayitEdilmis, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(requireContext(), R.string.gorselBulunamadi, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun urunGuncelle() {
        try {
            val barkodNo = binding.barkodNoText.text.toString().trim()
            val urunAdi = binding.urunAdiText.text.toString().trim()
            val urunAdiLowerCase = urunAdi.lowercase().trim()
            val birlesikIcindekiler = icindekilerListesi.joinToString(", ").trim()

            if (barkodNo.isEmpty()) {
                Toast.makeText(requireContext(), R.string.gecersizBarkodNo, Toast.LENGTH_SHORT).show()
                return
            }

            val guncellenenUrunMap: MutableMap<String, Any> = mutableMapOf(
                "urunAdi" to urunAdi,
                "barkodNo" to barkodNo,
                "icindekiler" to birlesikIcindekiler,
                "urunAdiLowerCase" to urunAdiLowerCase
            )

            val documentRef = db.collection("urunler").document(documentId)
            if (barkodNo.isNotEmpty() && urunAdi.isNotEmpty() && birlesikIcindekiler.isNotEmpty()){
                if (secilenGorsel != null) {
                    val gorselAdi = "${barkodNo}.jpg"
                    val gorselReferansi = storage.reference.child("images/$gorselAdi")

                    gorselReferansi.putFile(secilenGorsel!!)
                        .addOnSuccessListener {
                            gorselReferansi.downloadUrl.addOnSuccessListener { downloadUri ->
                                guncellenenUrunMap["gorselUrl"] = downloadUri.toString()

                                documentRef.update(guncellenenUrunMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(requireContext(), R.string.urunBasariylaGuncellendi, Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                } else {
                    documentRef.update(guncellenenUrunMap)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), R.string.urunBasariylaGuncellendi, Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(requireContext(), R.string.lutfenBosAlanBirakmayiniz, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun urunSil() {
        try {
            if (barkodNo.isNotEmpty()) {
                db.collection("urunler")
                    .whereEqualTo("barkodNo", barkodNo)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val document = querySnapshot.documents.firstOrNull()
                        if (document != null) {
                            val documentId = document.id
                            db.collection("urunler")
                                .document(documentId)
                                .delete()
                                .addOnSuccessListener {
                                    try {
                                        val navOptions = NavOptions.Builder().setPopUpTo(R.id.urunEkleFragment, true).setLaunchSingleTop(true).build()
                                        findNavController().navigate(R.id.action_urunEkleFragment_to_adminTumUrunlerFragment, null, navOptions)
                                    } catch (e: Exception) {
                                        Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                                    }
                                    Toast.makeText(requireContext(), R.string.urunBasariylaSilindi, Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(requireContext(), R.string.urunBulunamadi, Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), R.string.gecersizBarkodNo, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun icerikAl() {
        try {
            db.collection("icerik")
                .addSnapshotListener { value, error ->
                    try {
                        if (error != null) {
                            // Fragment bağlanmış mı, kontrol etmeden hata mesajı gösterme
                            if (isAdded) {
                                Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
                            }
                            return@addSnapshotListener
                        }
                        if (value != null && !value.isEmpty) {
                            val documents = value.documents
                            icerikListesi.clear()
                            for (document in documents) {
                                val urun = document.get("urun") as? String ?: continue
                                icerikListesi.add(urun)
                            }
                            icerikListesi.sort()

                            // Fragment bağlanmış mı, kontrol etmeden adapter'ı set etme
                            if (isAdded) {
                                icerikAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, android.R.id.text1, icerikListesi)
                                binding.icerikSpinner.adapter = icerikAdapter
                            }
                        }
                    } catch (e: Exception) {
                        if (isAdded) {
                            Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }
        } catch (e: Exception) {
            if (isAdded) {
                Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun aciklamaGetir(urun: String, position: Int) {
        try {
            db.collection("icerik")
                .whereEqualTo("urun", urun)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    try {
                        if (!querySnapshot.isEmpty) {
                            val document = querySnapshot.documents.firstOrNull()
                            if (document != null) {
                                val aciklama = document.getString("aciklama") ?: "Açıklama bulunamadı"
                                val alert = AlertDialog.Builder(requireContext())
                                alert.setMessage(aciklama)
                                alert.setPositiveButton(R.string.tamam) { _, _ -> }
                                alert.setNegativeButton(R.string.sil) { _, _ ->
                                    if (position in 0 until icindekilerListesi.size) {
                                        icindekilerListesi.removeAt(position)
                                        icindekilerAdapter.notifyDataSetChanged()
                                    }
                                }
                                alert.show()
                            } else {
                                Toast.makeText(requireContext(), R.string.belgeBulunamadi, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), R.string.sonucBulunamadi, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        try {
            super.onDestroyView()
            _binding = null
            cameraExecutor.shutdown()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }
}
