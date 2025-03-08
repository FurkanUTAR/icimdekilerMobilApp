package com.example.icimdekiler.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.icimdekiler.R
import com.example.icimdekiler.databinding.FragmentAdminAnaSayfaBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class adminAnaSayfaFragment : Fragment() {

    // Binding
    private var _binding: FragmentAdminAnaSayfaBinding? = null
    private val binding get() = _binding!!

    // Firebase
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraExecutor: ExecutorService

    private var barkodNo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        registerLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminAnaSayfaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val popupMenu = PopupMenu(requireContext(), binding.popupMenu)
        popupMenu.menuInflater.inflate(R.menu.menu_fab, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.cikisYap -> {
                    auth.signOut()

                    val navOptions = NavOptions.Builder().setPopUpTo(R.id.adminAnaSayfaFragment, true).setLaunchSingleTop(true).build()
                    findNavController().navigate(R.id.action_adminAnaSayfaFragment_to_girisYapFragment, null, navOptions)
                    true
                }
                else -> false
            }
        }

        binding.popupMenu.setOnClickListener { popupMenu.show() }

        binding.barkodOkuImageView.setOnClickListener { barkodOku() }

        binding.araImage.setOnClickListener { urunEkleGecisUrunAdi() }

        binding.ekleImage.setOnClickListener {
            val action = adminAnaSayfaFragmentDirections.actionAdminAnaSayfaFragmentToUrunEkleFragment(durum = "yeni", barkodNo = "", urunAdi = "", icindekiler = "")
            findNavController().navigate(action)
        }

        binding.tumUrunlerButton.setOnClickListener {
            val action = adminAnaSayfaFragmentDirections.actionAdminAnaSayfaFragmentToAdminTumUrunlerFragment()
            findNavController().navigate(action)
        }
    }

    private fun urunEkleGecisUrunAdi() {
        val urunAdiLowerCase=binding.urunAdiText.text.toString().lowercase().trim()

        db.collection("urunler")
            .whereEqualTo("urunAdiLowerCase", urunAdiLowerCase)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.firstOrNull()
                    barkodNo = document?.getString("barkodNo") ?: ""
                    val urunAdi = document?.getString("urunAdi") ?: ""
                    val icindekiler = document?.getString("icindekiler") ?: ""

                    val action = adminAnaSayfaFragmentDirections.actionAdminAnaSayfaFragmentToUrunEkleFragment("eski", barkodNo, urunAdi, icindekiler)
                    findNavController().navigate(action)
                }else Toast.makeText(requireContext(), "Ürün bulunamadı", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exepion -> Toast.makeText(requireContext(),exepion.localizedMessage, Toast.LENGTH_LONG).show() }
    }

    private fun urunEkleGecisBarkodNo() {
        db.collection("urunler")
            .whereEqualTo("barkodNo", barkodNo)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.firstOrNull()
                    val urunAdi = document?.getString("urunAdi") ?: ""
                    val icindekiler = document?.getString("icindekiler") ?: ""

                    val action = adminAnaSayfaFragmentDirections.actionAdminAnaSayfaFragmentToUrunEkleFragment("eski", barkodNo, urunAdi, icindekiler)
                    findNavController().navigate(action)
                } else Toast.makeText(requireContext(), "Ürün bulunamadı", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exeption -> Toast.makeText(requireContext(), exeption.localizedMessage, Toast.LENGTH_SHORT).show() }
    }

    private fun barkodOku() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                Snackbar.make(requireView(), "Barkod okumak için kameraya erişim izni gerekli!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("İzin Ver") {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }.show()
            } else permissionLauncher.launch(Manifest.permission.CAMERA)
        } else pickImageCamera()
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                if (imageBitmap == null) {
                    Toast.makeText(requireContext(), "Görüntü alınamadı!", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                val image = InputImage.fromBitmap(imageBitmap, 0)
                val scanner = BarcodeScanning.getClient()

                cameraExecutor.execute {
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            requireActivity().runOnUiThread {
                                if (barcodes.isNotEmpty()) {
                                    for (barcode in barcodes) {
                                        val barkod = barcode.displayValue
                                        if (!barkod.isNullOrBlank()) {
                                            barkodNo = barkod
                                            urunEkleGecisBarkodNo()
                                            break
                                        }
                                    }
                                } else Toast.makeText(requireContext(), "Barkod Okunamadı!", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener { e ->
                            requireActivity().runOnUiThread { Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show() }
                        }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) pickImageCamera()
            else Toast.makeText(requireContext(), "Kamera izni verilmedi!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImageCamera() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        activityResultLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}