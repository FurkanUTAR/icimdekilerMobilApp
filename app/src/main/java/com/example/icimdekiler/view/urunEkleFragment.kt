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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
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
                binding.gorselSecImageView.setImageResource(R.drawable.ic_launcher_background)
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
        // Kamera izni kontrol edilir.
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // İzin verilmediyse kullanıcıya izin isteği gösterilir.
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                Snackbar.make(requireView(), R.string.barkodOkumakIcinKamerayaErisimIzniGerekli, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.izinVer) {
                        requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
                    }.show()
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
            }
        } else { // İzin verildiyse kamerayı başlat.
            // BottomSheet dialog oluşturulur.
            val dialog = BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.dialog_barkod_okuma, null)
            dialog.setContentView(view)
            dialog.show() // Dialog gösterilir.

            val previewView = view.findViewById<PreviewView>(R.id.previewView)
            val btnClose = view.findViewById<Button>(R.id.btnClose)

            startCamera(previewView, dialog)

            btnClose.setOnClickListener { dialog.dismiss() } // Dialog'u kapat butonu.
        }
    }

    private fun startCamera(previewView: PreviewView, dialog: BottomSheetDialog) {
        // Kamera sağlayıcısını alır.
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        // Kamera başlatma işlemi tamamlandığında çağrılır.
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Kamera önizlemesi oluşturulur.
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            // Görüntü analizi için yapılandırma yapılır.
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
                        analyzeImage(imageProxy, dialog) // Görüntüyü analiz et.
                    }
                }

            // Arka kamerayı seçer.
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // Kamera bağlantısını yapılandırır.
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.e("CameraX", "Kamera bağlantısı başarısız", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeImage(imageProxy: ImageProxy, dialog: BottomSheetDialog) {
        // Görüntüyü alır.
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            // Görüntüyü ML Kit için uygun formata dönüştürür.
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // Barkod tarayıcıyı kullanarak görüntüyü analiz eder.
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
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
                }
                .addOnFailureListener { e ->
                    // Hata durumunda kullanıcıya bilgi ver.
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                }.addOnCompleteListener { imageProxy.close() } // Görüntüyü kapat.
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val dialog = BottomSheetDialog(requireContext())
            val previewView = dialog.findViewById<PreviewView>(R.id.previewView)
            if (previewView != null) {
                startCamera(previewView, dialog)
            }
        } else {
            Toast.makeText(requireContext(), "Kamera izni gerekiyor", Toast.LENGTH_SHORT).show()
        }
    }

    private fun barkodOkuGaleri() {
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
        }else{
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
    }

    private fun registerLauncherGallery() {
        activityResultLauncherGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null){
                    secilenGorsel = imageUri
                    val image = InputImage.fromFilePath(requireContext(), imageUri)
                    val scanner = BarcodeScanning.getClient()

                    if (islem=="barkodOku"){
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                if (barcodes.isNotEmpty()) {
                                    for (barcode in barcodes) {
                                        val barkod = barcode.displayValue
                                        if (barkod != null) {
                                            binding.barkodNoText.setText(barkod)
                                            break // İlk barkodu alınca döngüden çık
                                        }
                                    }
                                } else Toast.makeText(requireContext(), R.string.barkodOkunamadi, Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener { e -> Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show() }
                    } else if(islem == "gorselSec"){
                        try {
                            if (Build.VERSION.SDK_INT >= 28){
                                val source = ImageDecoder.createSource(requireActivity().contentResolver,secilenGorsel!!)
                                secilenBitmap = ImageDecoder.decodeBitmap(source)
                                binding.gorselSecImageView.setImageBitmap(secilenBitmap)
                            } else {
                                secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,secilenGorsel)
                                binding.gorselSecImageView.setImageBitmap(secilenBitmap)
                            }
                            if (secilenBitmap != null){
                                val targetWidth = binding.gorselSecImageView.width
                                val targetHeight = binding.gorselSecImageView.height
                                secilenBitmap = resizeBitmapWithAspectRatio(secilenBitmap!!, targetWidth, targetHeight)
                            }
                        } catch (e: Exception){
                            e.printStackTrace()
                        }
                    }
                } else Toast.makeText(requireContext(), R.string.gorselBulunamadi, Toast.LENGTH_SHORT).show()
            }
        }

        permissionLauncherGallery = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncherGallery.launch(intent)
            } else Toast.makeText(requireContext(), R.string.galeriIzniVerilmedi, Toast.LENGTH_SHORT).show()
        }
    }

    private fun resizeBitmapWithAspectRatio(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
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
    }

    private fun urunKaydet() {
        // Giriş alanlarından verileri al
        val barkodNo = binding.barkodNoText.text.toString().trim()
        val urunAdi = binding.urunAdiText.text.toString().trim()
        val urunAdiLowerCase = urunAdi.lowercase().trim()
        val birlesikIcindekiler = icindekilerListesi.joinToString(", ").trim()

        // Boş alan kontrolü
        if (barkodNo.isEmpty() || urunAdi.isEmpty() || birlesikIcindekiler.isEmpty()) {
            Toast.makeText(requireContext(), R.string.lutfenBosAlanBirakmayiniz, Toast.LENGTH_SHORT).show()
            return
        }

        // Görsel adını barkodNo ile ilişkilendir
        val gorselAdi = "${barkodNo}.jpg"
        val gorselReferansi = storage.reference.child("images/$gorselAdi")

        if (secilenGorsel != null) {
            // Görseli Storage'a yükle
            gorselReferansi.putFile(secilenGorsel!!)
                .addOnSuccessListener {
                    // Görsel yüklendikten sonra URL'sini al
                    gorselReferansi.downloadUrl.addOnSuccessListener { uri ->
                        val gorselUrl = uri.toString()

                        // Firestore'a kaydedilecek verileri hazırla
                        val urunMap = hashMapOf(
                            "urunAdi" to urunAdi,
                            "urunAdiLowerCase" to urunAdiLowerCase,
                            "barkodNo" to barkodNo,
                            "icindekiler" to birlesikIcindekiler,
                            "gorselUrl" to gorselUrl
                        )

                        // Aynı isimde ürün olup olmadığını kontrol et
                        db.collection("urunler")
                            .whereEqualTo("urunAdiLowerCase", urunAdiLowerCase)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                if (querySnapshot.isEmpty) {
                                    // Yeni ürünü Firestore'a ekle
                                    db.collection("urunler")
                                        .add(urunMap)
                                        .addOnSuccessListener {
                                            Toast.makeText(requireContext(), R.string.urunBasariylaKayitEdildi, Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    // Aynı isimde ürün varsa hata göster
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
        } else Toast.makeText(requireContext(), R.string.gorselBulunamadi, Toast.LENGTH_SHORT).show()
    }

    private fun urunGuncelle() {
        // Giriş alanlarından verileri al
        val barkodNo = binding.barkodNoText.text.toString().trim()
        val urunAdi = binding.urunAdiText.text.toString().trim()
        val urunAdiLowerCase = urunAdi.lowercase().trim()
        val birlesikIcindekiler = icindekilerListesi.joinToString(", ").trim()

        // Barkod numarası boşsa hata göster ve işlemi sonlandır
        if (barkodNo.isEmpty()) {
            Toast.makeText(requireContext(), R.string.gecersizBarkodNo, Toast.LENGTH_SHORT).show()
            return
        }

        // Güncellenecek verileri hazırla
        val guncellenenUrunMap: MutableMap<String, Any> = mutableMapOf(
            "urunAdi" to urunAdi,
            "barkodNo" to barkodNo,
            "icindekiler" to birlesikIcindekiler,
            "urunAdiLowerCase" to urunAdiLowerCase
        )

        // Firestore'daki belge referansını al
        val documentRef = db.collection("urunler").document(documentId)

        if (secilenGorsel != null) {
            // Yeni görsel yüklenecekse
            val gorselAdi = "${barkodNo}.jpg" // Görsel adını barkodNo ile ilişkilendir
            val gorselReferansi = storage.reference.child("images/$gorselAdi")

            // Görseli Storage'a yükle
            gorselReferansi.putFile(secilenGorsel!!)
                .addOnSuccessListener {
                    // Görsel yüklendikten sonra URL'sini al
                    gorselReferansi.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Görsel URL'sini güncellenecek verilere ekle
                        guncellenenUrunMap["gorselUrl"] = downloadUri.toString()

                        // Firestore'da belgeyi güncelle
                        documentRef.update(guncellenenUrunMap)
                            .addOnSuccessListener { Toast.makeText(requireContext(), R.string.urunBasariylaGuncellendi, Toast.LENGTH_SHORT).show() }
                            .addOnFailureListener { exception -> Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show() }
                    }
                }
                .addOnFailureListener { exception -> Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show() }
        } else {
            // Sadece metin bilgilerini güncelle
            documentRef.update(guncellenenUrunMap)
                .addOnSuccessListener { Toast.makeText(requireContext(), R.string.urunBasariylaGuncellendi, Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { exception -> Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun urunSil() {
        if(barkodNo.isNotEmpty()){
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
                                } catch (e: Exception) { Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show() }
                                Toast.makeText(requireContext(), R.string.urunBasariylaSilindi, Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener { exception -> Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show() }
                    } else Toast.makeText(requireContext(), R.string.urunBulunamadi, Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { exception -> Toast.makeText(requireContext(),exception.localizedMessage, Toast.LENGTH_SHORT).show() }
        } else Toast.makeText(requireContext(), R.string.gecersizBarkodNo, Toast.LENGTH_SHORT).show()
    }

    private fun icerikAl(){
        db.collection("icerik")
            .addSnapshotListener { value, error ->
                if (error != null){
                    Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
                } else {
                    if (value != null){
                        if (!value.isEmpty){
                            val documents = value.documents

                            icerikListesi.clear()
                            for (document in documents){
                                val urun=document.get("urun") as String

                                icerikListesi.add(urun)
                                icerikAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,android.R.id.text1,icerikListesi)
                                binding.icerikSpinner.adapter = icerikAdapter
                            }
                        }
                    }
                }
            }
    }

    private fun aciklamaGetir(urun:String, position:Int){
        db.collection("icerik")
            .whereEqualTo("urun", urun)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // İlk belgeyi al
                    val document = querySnapshot.documents.firstOrNull()

                    if (document != null) {
                        // Belgeden "aciklama" alanını al
                        val aciklama = document.getString("aciklama") ?: "Açıklama bulunamadı"
                        val alert= AlertDialog.Builder(requireContext())
                        alert.setMessage(aciklama.toString())
                        alert.setPositiveButton(R.string.tamam) { dialog, which -> }
                        alert.setNegativeButton(R.string.sil) { dialog,which ->
                            if (position >= 0 && position < icindekilerListesi.size ){
                                icindekilerListesi.removeAt(position)
                                icindekilerAdapter.notifyDataSetChanged()
                            }
                        }
                        alert.show()
                    } else Toast.makeText(requireContext(), R.string.belgeBulunamadi, Toast.LENGTH_SHORT).show()
                } else Toast.makeText(requireContext(), R.string.sonucBulunamadi, Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exception -> Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}
