package com.example.icimdekiler.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.icimdekiler.R
import com.example.icimdekiler.databinding.FragmentIcerikEkleBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlin.collections.set

class icerikEkleFragment : Fragment() {

    //Binding
    private var _binding: FragmentIcerikEkleBinding? = null
    private val binding get() = _binding!!

    //Firebase
    private val db = FirebaseFirestore.getInstance()

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
            alert.setTitle(R.string.kayitEtmekIstediginizdenEminMisiniz)
            alert.setPositiveButton(R.string.evet) { dialog, _ -> icerikEkle() }
            alert.setNegativeButton(R.string.hayir, null).show()
        }
    }

    private fun icerikEkle() {
        val urun = binding.urunAdiText.text.toString().trim()
        val aciklama = binding.aciklamaText.text.toString().trim()

        if (urun.isEmpty() || aciklama.isEmpty()) {
            Toast.makeText(requireContext(), R.string.lutfenBosAlanBirakmayiniz, Toast.LENGTH_SHORT).show()
            return
        }

        val icerikMap = hashMapOf<String, Any>()
        icerikMap["urun"] = urun
        icerikMap["aciklama"] = aciklama

        db.collection("icerik")
            .whereEqualTo("urun", urun)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null) {
                    val document = querySnapshot.documents.firstOrNull()
                    if (document == null) {
                        db.collection("icerik")
                            .add(icerikMap)
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
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}