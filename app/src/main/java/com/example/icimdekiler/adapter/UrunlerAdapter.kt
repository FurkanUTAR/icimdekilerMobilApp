package com.example.icimdekiler.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.icimdekiler.databinding.RecyclerRowBinding
import com.example.icimdekiler.model.Urunler
import com.example.icimdekiler.view.adminTumUrunlerFragmentDirections
import com.example.icimdekiler.view.kullaniciTumUrunlerFragmentDirections
import androidx.navigation.findNavController

class UrunlerAdapter(val urunListesi: ArrayList<Urunler>, val kullaniciTipi: String) : RecyclerView.Adapter<UrunlerAdapter.UrunHolder>() {

    class UrunHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrunHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UrunHolder(binding)
    }

    override fun getItemCount(): Int {
        return urunListesi.size
    }

    override fun onBindViewHolder(holder: UrunHolder, position: Int) {
        val urun = urunListesi[position]

        holder.binding.recyclerViewUrunAdiText.text = urunListesi[position].urunAdi
        holder.itemView.setOnClickListener {
            if (kullaniciTipi=="admin"){
                val action = adminTumUrunlerFragmentDirections.actionAdminTumUrunlerFragmentToUrunEkleFragment(durum = "eski", barkodNo = urun.barkodNo.toString(), urunAdi= urun.urunAdi.toString(), icindekiler= urun.icindekiler.toString())
                it.findNavController().navigate(action)
            } else {
                val action= kullaniciTumUrunlerFragmentDirections.actionKullaniciTumUrunlerFragmentToUrunFragment()
                it.findNavController().navigate(action)
            }
        }
    }
}