package com.example.icimdekiler.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.icimdekiler.databinding.FragmentIcerikEkleBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import org.checkerframework.checker.units.qual.radians
import kotlin.collections.set

class icerikEkleFragment : Fragment() {

    //Binding
    private var _binding: FragmentIcerikEkleBinding? = null
    private val binding get() = _binding!!

    //Firebase
    private val db=Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIcerikEkleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.kaydetButton.setOnClickListener {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle("Kayıt etmek istediğinizden emin misiniz?")
            alert.setPositiveButton("Evet") { dialog, value -> icerikEkle()}
            alert.setNegativeButton("Hayır",null).show()
        }
    }

    fun icerikEkle(){
        val urunAdi=binding.urunAdiText.text.toString().trim()
        val aciklama=binding.aciklamaText.text.toString().trim()

        val icerikMap=hashMapOf<String,Any>()
        icerikMap["urunAdi"] = urunAdi
        icerikMap["aciklama"] = aciklama

        db.collection("icerik")
            .whereEqualTo("urunAdi",urunAdi)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null){
                    val document = querySnapshot.documents.firstOrNull()
                    if (document == null){
                        db.collection("icerik")
                            .add(icerikMap)
                            .addOnSuccessListener { task -> Toast.makeText(requireContext(), "Ürün başarıyla kayıt edildi", Toast.LENGTH_SHORT).show() }
                            .addOnFailureListener { exeption -> Toast.makeText(requireContext(), exeption.localizedMessage, Toast.LENGTH_SHORT).show() }
                    } else Toast.makeText(requireContext(), "Bu ürün daha önce kayıt edilmiş", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exeption -> Toast.makeText(requireContext(), exeption.localizedMessage, Toast.LENGTH_SHORT).show() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}