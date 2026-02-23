package com.furkanutar.icimdekiler.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.furkanutar.icimdekiler.model.Urunler
import com.furkanutar.icimdekiler.view.adminTumUrunlerFragmentDirections
import com.furkanutar.icimdekiler.view.kullaniciTumUrunlerFragmentDirections
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.databinding.RecyclerRowBinding
import com.squareup.picasso.Picasso

class UrunlerAdapter(val urunListesi: ArrayList<Urunler>, val kullaniciTipi: String) : RecyclerView.Adapter<UrunlerAdapter.UrunHolder>() {

    class UrunHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrunHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UrunHolder(binding)
    }

    override fun getItemCount(): Int {
        return urunListesi.size
    }

    override fun onBindViewHolder(holder: UrunHolder, position: Int) {
        val urun = urunListesi[position]

        // Ürün bilgilerini RecyclerView'da göster
        holder.binding.recyclerViewUrunAdiText.text = urun.urunAdi
        val imageUrl = urun.gorselUrl
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).fit().centerCrop().into(holder.binding.recyclerViewUrunGorselImage)
        } else {
            holder.binding.recyclerViewUrunGorselImage.setImageResource(R.drawable.ic_launcher_background)
        }

        // Ürün tıklandığında yapılacak işlemler
        holder.itemView.setOnClickListener {
            if (kullaniciTipi == "admin") {
                // Admin ise ürünü güncelleme ekranına yönlendir
                val action = adminTumUrunlerFragmentDirections.actionAdminTumUrunlerFragmentToUrunEkleFragment(
                    durum = "eski",
                    documentId = urun.documentId.toString(), // Ürünün benzersiz kimliği
                    barkodNo = urun.barkodNo.toString(),
                    urunAdi = urun.urunAdi.toString(),
                    icindekiler = urun.icindekiler.toString(),
                    gorselUrl = imageUrl.toString()
                )
                it.findNavController().navigate(action)
            } else {
                // Kullanıcı ise ürün detay ekranına yönlendir
                val action = kullaniciTumUrunlerFragmentDirections.actionKullaniciTumUrunlerFragmentToUrunFragment(
                    urun.barkodNo.toString(),
                    urun.urunAdi.toString(),
                    urun.icindekiler.toString(),
                    urun.gorselUrl.toString()
                )
                it.findNavController().navigate(action)
            }
        }
    }
}