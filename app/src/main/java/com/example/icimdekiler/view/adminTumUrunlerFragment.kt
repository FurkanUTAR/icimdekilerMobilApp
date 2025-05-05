package com.example.icimdekiler.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.icimdekiler.R
import com.example.icimdekiler.adapter.UrunlerAdapter
import com.example.icimdekiler.databinding.FragmentAdminTumUrunlerBinding
import com.example.icimdekiler.model.Urunler
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import java.util.Locale

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

        var kategori = ""
        arguments?.let {
            kategori = adminTumUrunlerFragmentArgs.fromBundle(it).kategori
        }

        urunleriAl(kategori)

        binding.araImage.setOnClickListener { urunAra(kategori) }
    }

    private fun urunleriAl(kategori : String) {
        if (kategori == "tumUrunler" || kategori.isEmpty()){
            db.collection("urunler")
                .orderBy("urunAdiLowerCase", Query.Direction.ASCENDING) // Küçük harf bazlı alfabetik sıralama
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

                        binding.urunSayisiText.text = "${getString(R.string.urunSayisi)} : ${urunListesi.count()}"

                        if (isAdded && !isDetached) {
                            val adapter = UrunlerAdapter(urunListesi, "admin")
                            binding.urunlerRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                            binding.urunlerRecyclerView.adapter = adapter
                        }
                    } else {
                        Toast.makeText(requireContext(), R.string.urunBulunamadi, Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            db.collection("urunler")
                .whereEqualTo("kategori",kategori)
                .orderBy("urunAdiLowerCase", Query.Direction.ASCENDING) // Küçük harf bazlı alfabetik sıralama
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

                        binding.urunSayisiText.text = "${getString(R.string.urunSayisi)} : ${urunListesi.count()}"

                        if (isAdded && !isDetached) {
                            val adapter = UrunlerAdapter(urunListesi, "admin")
                            binding.urunlerRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                            binding.urunlerRecyclerView.adapter = adapter
                        }
                    } else {
                        Toast.makeText(requireContext(), R.string.urunBulunamadi, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun urunAra(kategori: String) {
        val urun = binding.urunAdiText.text.toString().trim()
            .lowercase(Locale("tr","TR"))
            .replace("ç", "c")
            .replace("ğ", "g")
            .replace("ı", "i")
            .replace("ö", "o")
            .replace("ş", "s")
            .replace("ü", "u")

        if (kategori == "tumUrunler" || kategori.isEmpty()){
            db.collection("urunler")
                .orderBy("urunAdiLowerCase", Query.Direction.ASCENDING) // Küçük harf bazlı sıralama ile arama
                .get()
                .addOnSuccessListener { documents ->
                    urunListesi.clear()
                    for (document in documents) {
                        val documentId = document.id
                        val barkodNo = document.getString("barkodNo") ?: ""
                        val urunAdi = document.getString("urunAdi") ?: ""
                        val icindekiler = document.getString("icindekiler") ?: ""
                        val gorselUrl = document.getString("gorselUrl") ?: ""

                        val urunAdiNormalized = urunAdi
                            .lowercase(Locale("tr","TR"))
                            .replace("ç", "c")
                            .replace("ğ", "g")
                            .replace("ı", "i")
                            .replace("ö", "o")
                            .replace("ş", "s")
                            .replace("ü", "u")

                        if (urun.isEmpty() || urunAdiNormalized.lowercase().contains(urun)) {
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
        } else {
            db.collection("urunler")
                .whereEqualTo("kategori",kategori)
                .orderBy("urunAdiLowerCase", Query.Direction.ASCENDING) // Küçük harf bazlı sıralama ile arama
                .get()
                .addOnSuccessListener { documents ->
                    urunListesi.clear()
                    for (document in documents) {
                        val documentId = document.id
                        val barkodNo = document.getString("barkodNo") ?: ""
                        val urunAdi = document.getString("urunAdi") ?: ""
                        val icindekiler = document.getString("icindekiler") ?: ""
                        val gorselUrl = document.getString("gorselUrl") ?: ""

                        val urunAdiNormalized = urunAdi
                            .lowercase(Locale("tr","TR"))
                            .replace("ç", "c")
                            .replace("ğ", "g")
                            .replace("ı", "i")
                            .replace("ö", "o")
                            .replace("ş", "s")
                            .replace("ü", "u")

                        if (urun.isEmpty() || urunAdiNormalized.lowercase().contains(urun)) {
                            val indirilenUrun = Urunler(barkodNo, urunAdi, icindekiler, gorselUrl, documentId)
                            urunListesi.add(indirilenUrun)
                        }
                    }

                    val adapter = UrunlerAdapter(urunListesi, "admin")
                    binding.urunlerRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                    binding.urunlerRecyclerView.adapter = adapter
                }.addOnFailureListener { error ->
                    print(error.localizedMessage)
                    Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //_binding = null
    }
}