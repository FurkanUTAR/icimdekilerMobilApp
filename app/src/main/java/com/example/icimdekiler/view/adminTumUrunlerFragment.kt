package com.example.icimdekiler.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.icimdekiler.adapter.UrunlerAdapter
import com.example.icimdekiler.databinding.FragmentAdminTumUrunlerBinding
import com.example.icimdekiler.model.Urunler
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class adminTumUrunlerFragment : Fragment() {

    //Binding
    private var _binding: FragmentAdminTumUrunlerBinding? = null
    private val binding get() = _binding!!

    //Firebase
    private val db = FirebaseFirestore.getInstance()

    private var urunListesi = ArrayList<Urunler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminTumUrunlerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        urunleriAl()

        binding.araImage.setOnClickListener { urunAra() }
    }

    private fun urunleriAl() {
        db.collection("urunler")
            .orderBy("urunAdiLowerCase", Query.Direction.ASCENDING) // Küçük harf bazlı alfabetik sıralama
            .limit(30)
            .addSnapshotListener { value, error ->
                if (!isAdded || isDetached) return@addSnapshotListener

                if (error != null) {
                    Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (value != null && !value.isEmpty) {
                    urunListesi.clear()
                    for (document in value.documents) {
                        val documentId = document.id
                        val barkodNo = document.getString("barkodNo") ?: ""
                        val urunAdi = document.getString("urunAdi") ?: ""
                        val icindekiler = document.getString("icindekiler") ?: ""
                        val gorselUrl = document.getString("gorselUrl") ?: ""

                        if (barkodNo.isNotEmpty() && urunAdi.isNotEmpty()) {
                            val indirilenUrun = Urunler(barkodNo, urunAdi, icindekiler, gorselUrl, documentId)
                            urunListesi.add(indirilenUrun)
                        }
                    }

                    if (isAdded && !isDetached) {
                        val adapter = UrunlerAdapter(urunListesi, "admin")
                        binding.urunlerRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                        binding.urunlerRecyclerView.adapter = adapter
                    }
                } else {
                    Toast.makeText(requireContext(), "Ürün bulunamadı", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun urunAra() {
        val urun = binding.urunAdiText.text.toString().trim().lowercase()

        val sorgu =
            if (urun.isEmpty()) db.collection("urunler").orderBy("urunAdiLowerCase", Query.Direction.ASCENDING) // Eğer arama kutusu boşsa alfabetik sırayla tüm ürünleri getir
            else db.collection("urunler").orderBy("urunAdiLowerCase", Query.Direction.ASCENDING) // Küçük harf bazlı sıralama ile arama

        sorgu.get()
            .addOnSuccessListener { documents ->
                urunListesi.clear()
                for (document in documents) {
                    val documentId = document.id
                    val barkodNo = document.getString("barkodNo") ?: ""
                    val urunAdi = document.getString("urunAdi") ?: ""
                    val icindekiler = document.getString("icindekiler") ?: ""
                    val gorselUrl = document.getString("gorselUrl") ?: ""

                    if (urun.isEmpty() || urunAdi.lowercase().contains(urun)) {
                        val indirilenUrun = Urunler(barkodNo, urunAdi, icindekiler, gorselUrl, documentId)
                        urunListesi.add(indirilenUrun)
                    }
                }

                val adapter = UrunlerAdapter(urunListesi, "admin")
                binding.urunlerRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                binding.urunlerRecyclerView.adapter = adapter
            }.addOnFailureListener { error ->
                Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //_binding = null
    }
}