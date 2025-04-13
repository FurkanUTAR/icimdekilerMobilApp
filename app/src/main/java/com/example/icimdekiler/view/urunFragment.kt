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

    private var _binding: FragmentUrunBinding? = null
    private val binding get() = _binding!!

    val db = Firebase.firestore

    private val icindekilerListesi = mutableListOf<String>()
    private lateinit var icindekilerAdapter: ArrayAdapter<String>

    var barkodNo: String=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUrunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            icindekilerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, icindekilerListesi)
            binding.icindekilerListView.adapter = icindekilerAdapter

            arguments?.let {
                barkodNo = urunFragmentArgs.fromBundle(it).barkodNo
                val urunAdi = urunFragmentArgs.fromBundle(it).urunAdi
                val gelenIcindekiler = urunFragmentArgs.fromBundle(it).icindekiler
                val gorselUrl = urunFragmentArgs.fromBundle(it).gorselUrl

                binding.urunAdiText.text = urunAdi

                val icindekiler = gelenIcindekiler.split(", ").map { it.trim() }
                icindekilerListesi.clear()
                icindekilerListesi.addAll(icindekiler)
                icindekilerAdapter.notifyDataSetChanged()

                if (gorselUrl.isNotEmpty()) {
                    Picasso.get().load(gorselUrl).fit().centerCrop().into(binding.gorselSecImageView)
                } else {
                    binding.gorselSecImageView.setImageResource(R.drawable.ic_launcher_background)
                }
            }

            binding.icindekilerListView.setOnItemClickListener { parent, _, position, _ ->
                val urun = parent.getItemAtPosition(position) as String
                aciklamaGetir(urun, position)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun aciklamaGetir(urun: String, position: Int) {
        try {
            db.collection("icerik")
                .whereEqualTo("urun", urun)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents.firstOrNull()

                        if (document != null) {
                            val aciklama = document.getString("aciklama") ?: "Açıklama bulunamadı"
                            val alert = AlertDialog.Builder(requireContext())
                            alert.setMessage(aciklama)
                            alert.setPositiveButton(R.string.tamam) { dialog, _ -> dialog.dismiss() }
                            alert.show()
                        } else {
                            Toast.makeText(requireContext(), R.string.belgeBulunamadi, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), R.string.sonucBulunamadi, Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Veritabanı hatası: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}