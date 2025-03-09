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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class adminTumUrunlerFragment : Fragment() {

    //Binding
    private var _binding: FragmentAdminTumUrunlerBinding? = null
    private val binding get() = _binding!!

    //Firebase
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private var urunListesi=ArrayList<Urunler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth=Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminTumUrunlerBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        urunleriAl()

        binding.araImage.setOnClickListener { urunAra() }
    }

    private fun urunleriAl(){
        db.collection("urunler")
            .orderBy("urunAdi", Query.Direction.ASCENDING)
            .limit(30)
            .addSnapshotListener { value, error ->
                if (error != null){
                    Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
                } else {
                    if (value != null && !value.isEmpty) {

                        urunListesi.clear()
                        for (document in value.documents) {
                            var barkodNo = document.getString("barkodNo") ?: ""
                            var urunAdi = document.getString("urunAdi") ?: ""
                            var icindekiler = document.getString("icindekiler") ?: ""

                            val indirilenUrun = Urunler(barkodNo, urunAdi, icindekiler)
                            urunListesi.add(indirilenUrun)
                        }

                        val adapter = UrunlerAdapter(urunListesi, "admin")
                        binding.urunlerRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                        binding.urunlerRecyclerView.adapter = adapter
                    }
                }
            }
    }

    private fun urunAra() {
        val urun = binding.urunAdiText.text.toString().trim()

        if (urun.isNotEmpty()) {
            db.collection("urunler")
                .orderBy("urunAdiLowerCase")
                .startAt(urun)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }
                    else {
                        if (value != null && !value.isEmpty) {
                            urunListesi.clear()
                            for (document in value.documents) {
                                var barkodNo = document.getString("barkodNo") ?: ""
                                var urunAdi = document.getString("urunAdi") ?: ""
                                var icindekiler = document.getString("icindekiler") ?: ""

                                val indirilenUrun = Urunler(barkodNo, urunAdi, icindekiler)
                                urunListesi.add(indirilenUrun)
                            }

                            val adapter = UrunlerAdapter(urunListesi,"admin")
                            binding.urunlerRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                            binding.urunlerRecyclerView.adapter = adapter
                        } else {
                            urunListesi.clear()
                            binding.urunlerRecyclerView.adapter?.notifyDataSetChanged()
                            return@addSnapshotListener
                        }
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // _binding = null
    }
}