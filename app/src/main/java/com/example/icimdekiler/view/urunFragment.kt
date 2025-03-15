package com.example.icimdekiler.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.icimdekiler.R
import com.example.icimdekiler.databinding.FragmentUrunBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso

class urunFragment : Fragment() {

    //Binding
    private var _binding: FragmentUrunBinding? = null
    private val binding get() = _binding!!

    //Firebase
    val db = Firebase.firestore

    private val icindekilerListesi = mutableListOf<String>()
    private lateinit var icindekilerAdapter: ArrayAdapter<String>

    var barkodNo: String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUrunBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        icindekilerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, icindekilerListesi)
        binding.icindekilerListView.adapter = icindekilerAdapter

        arguments?.let {
            barkodNo = urunFragmentArgs.fromBundle(it).barkodNo
            val urunAdi = urunFragmentArgs.fromBundle(it).urunAdi
            val gelenIcindekiler = urunFragmentArgs.fromBundle(it).icindekiler
            var gorselUrl = urunFragmentArgs.fromBundle(it).gorselUrl

            binding.barkodNoText.text = barkodNo
            binding.urunAdiText.text = urunAdi

            val icindekiler = gelenIcindekiler.split(", ").map { it.trim() }
            icindekilerListesi.clear()
            icindekilerListesi.addAll(icindekiler)
            icindekilerAdapter.notifyDataSetChanged()

            if (gorselUrl.isNotEmpty()) {
                Picasso.get().load(gorselUrl).fit().centerCrop().into(binding.gorselSecImageView) // 📌 ImageView'inin id'sini buraya yaz
            } else {
                binding.gorselSecImageView.setImageResource(R.drawable.ic_launcher_background) // Varsayılan resim
            }
        }

        binding.icindekilerListView.setOnItemClickListener { parent, view, position, id ->
            val urun = parent.getItemAtPosition(position) as String
            aciklamaGetir(urun,position)
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
                        alert.show()
                    } else Toast.makeText(requireContext(), R.string.belgeBulunamadi, Toast.LENGTH_SHORT).show()
                } else Toast.makeText(requireContext(), R.string.sonucBulunamadi, Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exception -> Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}