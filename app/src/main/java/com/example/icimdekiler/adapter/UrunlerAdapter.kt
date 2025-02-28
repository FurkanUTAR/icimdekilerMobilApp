package com.example.icimdekiler.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.ListMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.example.icimdekiler.R
import com.example.icimdekiler.databinding.RecyclerRowBinding
import com.example.icimdekiler.model.Urunler
import com.example.icimdekiler.view.urunFragment

class UrunlerAdapter(val urunListesi : ArrayList<Urunler>) : RecyclerView.Adapter<UrunlerAdapter.UrunHolder>() {

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
        holder.binding.recyclerViewBarkodNoText.text = urunListesi[position].barkodNo
        holder.binding.recyclerViewUrunAdiText.text = urunListesi[position].urunAdi
        holder.binding.recyclerViewIcindekilerText.text = urunListesi[position].icindekiler
    }
}