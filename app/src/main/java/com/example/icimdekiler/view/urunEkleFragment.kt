package com.example.icimdekiler.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

class urunEkleFragment : Fragment() {

    //Binding
    private var _binding: FragmentUrunEkleBinding? = null
    private val binding get() = _binding!!

    //Firebase
    val db = Firebase.firestore

    private lateinit var permissionLauncherCamera: ActivityResultLauncher<String>
    private lateinit var activityResultLauncherCamera: ActivityResultLauncher<Intent>

    private lateinit var permissionLauncherGallery: ActivityResultLauncher<String>
    private lateinit var activityResultLauncherGallery: ActivityResultLauncher<Intent>

    private lateinit var cameraExecutor: ExecutorService

    private val icerikListesi = ArrayList<String>()
    private lateinit var icerikAdapter:ArrayAdapter<String>

    private val icindekilerListesi = mutableListOf<String>()
    private lateinit var icindekilerAdapter: ArrayAdapter<String>

    var barkodNo: String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        registerLauncherCamera()
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
            val durum= urunEkleFragmentArgs.fromBundle(it).durum
            barkodNo = urunEkleFragmentArgs.fromBundle(it).barkodNo
            val urunAdi= urunEkleFragmentArgs.fromBundle(it).urunAdi
            val gelenIcindekiler= urunEkleFragmentArgs.fromBundle(it).icindekiler

            if (durum=="yeni"){
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
        }

        binding.barkodOkuImage.setOnClickListener {
            val secim = arrayOf("${R.string.kamera}","${R.string.galeri}")
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle(R.string.secimYap)
            alert.setItems(secim){ dialog, which ->
                if(which==0) barkodOkuKamera()
                else barkodOkuGaleri()
            }.show()
        }

        binding.ekleImage.setOnClickListener {
            icindekilerListesi.add(icerikListesi[binding.icerikSpinner.selectedItemPosition])
            icindekilerAdapter.notifyDataSetChanged()
        }

        binding.kaydetButton.setOnClickListener {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle(R.string.kayitEtmekIstediginizdenEminMisiniz)
            alert.setPositiveButton(R.string.evet){ dialog, value -> urunKaydet()}
            alert.setNegativeButton(R.string.hayir,null).show()
        }

        binding.guncelleButton.setOnClickListener {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle(R.string.guncellemekIstediginizdenEminMisiniz)
            alert.setPositiveButton(R.string.evet){ dialog, value -> urunGuncelle()}
            alert.setNegativeButton(R.string.hayir,null).show()
        }

        binding.silButton.setOnClickListener {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle(R.string.silmekIstediginizdenEminMisiniz)
            alert.setPositiveButton(R.string.evet){ dialog, value -> urunSil()}
            alert.setNegativeButton(R.string.hayir,null).show()
        }

        binding.icindekilerListView.setOnItemClickListener { parent, view, position, id ->
            val urun = parent.getItemAtPosition(position) as String
            aciklamaGetir(urun,position)
        }
    }

    private fun urunKaydet() {
        val barkodNo = binding.barkodNoText.text.toString().trim()
        val urunAdi = binding.urunAdiText.text.toString().trim()
        val urunAdiLowerCase = binding.urunAdiText.text.toString().lowercase().trim()
        val birlesikIcindekiler = icindekilerListesi.joinToString(", ").trim()

        val urunMap = hashMapOf<String, Any>()
        urunMap["urunAdi"] = urunAdi
        urunMap["urunAdiLowerCase"] = urunAdiLowerCase
        urunMap["barkodNo"] = barkodNo
        urunMap["icindekiler"] = birlesikIcindekiler

        if (barkodNo.isNotEmpty() && urunAdi.isNotEmpty() && birlesikIcindekiler.isNotEmpty() && urunAdiLowerCase.isNotEmpty()) {
            db.collection("urunler")
                .whereEqualTo("urunAdiLowerCase",urunAdiLowerCase)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val document = querySnapshot.documents.firstOrNull()
                    if (document == null){
                        db.collection("urunler")
                            .add(urunMap)
                            .addOnSuccessListener { Toast.makeText(requireContext(), R.string.urunBasariylaKayitEdildi, Toast.LENGTH_SHORT).show() }
                            .addOnFailureListener { exeption -> Toast.makeText(requireContext(), exeption.localizedMessage, Toast.LENGTH_SHORT).show() }
                    } else Toast.makeText(requireContext(), R.string.urunDahaOnceKayitEdilmis, Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { exeption -> Toast.makeText(requireContext(), exeption.localizedMessage, Toast.LENGTH_SHORT).show() }
        }else Toast.makeText(requireContext(), R.string.lutfenBosAlanBirakmayiniz, Toast.LENGTH_SHORT).show()
    }

    private fun urunGuncelle(){
        val barkodNo= binding.barkodNoText.text.toString().trim()
        val urunAdi= binding.urunAdiText.text.toString().trim()
        val birlesikIcindekiler = icindekilerListesi.joinToString(", ").trim()

        if(barkodNo.isNotEmpty()){
            db.collection("urunler")
                .whereEqualTo("barkodNo", barkodNo)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val document = querySnapshot.documents.firstOrNull()
                    if (document != null) {
                        val documentId = document.id
                        if (barkodNo.isNotEmpty() && urunAdi.isNotEmpty() && birlesikIcindekiler.isNotEmpty()) {
                            db.collection("urunler")
                                .document(documentId)
                                .update(
                                    mapOf(
                                        "urunAdi" to urunAdi,
                                        "barkodNo" to barkodNo,
                                        "icindekiler" to birlesikIcindekiler
                                    )
                                )
                                .addOnSuccessListener { Toast.makeText(requireContext(), R.string.urunBasariylaGuncellendi, Toast.LENGTH_SHORT).show() }
                                .addOnFailureListener { exception -> Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show() }
                        }
                    } else Toast.makeText(requireContext(), R.string.belgeBulunamadi, Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { exception -> Toast.makeText(requireContext(),exception.localizedMessage, Toast.LENGTH_SHORT).show() }
        } else Toast.makeText(requireContext(), R.string.gecersizBarkodNo, Toast.LENGTH_SHORT).show()
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

    private fun aciklamaGetir(urun:String, position:Int ){
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

    private fun barkodOkuKamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                Snackbar.make(requireView(), R.string.barkodOkumakIcinKamerayaErisimIzniGerekli, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.izinVer) {
                        permissionLauncherCamera.launch(Manifest.permission.CAMERA)
                    }.show()
            } else {
                permissionLauncherCamera.launch(Manifest.permission.CAMERA)
            }
        } else {
            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            activityResultLauncherCamera.launch(intent)
        }
    }

    private fun registerLauncherGallery() {
        activityResultLauncherGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri!=null){
                    val image = InputImage.fromFilePath(requireContext(), imageUri)
                    val scanner = BarcodeScanning.getClient()

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

    private fun registerLauncherCamera() {
        activityResultLauncherCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                val image = InputImage.fromBitmap(imageBitmap, 0)
                val scanner = BarcodeScanning.getClient()

                cameraExecutor.execute {
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            if (barcodes.isNotEmpty()) {
                                for (barcode in barcodes) {
                                    val barkod = barcode.displayValue
                                    if (barkod != null) {
                                        binding.barkodNoText.setText(barkod)
                                        break // Tek barkod okutulduğunda döngüden çık
                                    }
                                }
                            } else Toast.makeText(requireContext(), R.string.barkodOkunamadi, Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { e -> Toast.makeText(requireContext(),e.localizedMessage, Toast.LENGTH_SHORT).show() }
                }
            }
        }

        permissionLauncherCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                activityResultLauncherCamera.launch(intent)
            } else Toast.makeText(requireContext(), R.string.kameraIzniVerilmedi, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}
