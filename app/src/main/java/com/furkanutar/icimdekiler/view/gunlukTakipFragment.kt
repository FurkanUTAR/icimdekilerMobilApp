package com.furkanutar.icimdekiler.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.ui.GunlukHedef
import com.furkanutar.icimdekiler.ui.GunlukTakipScreen
import com.furkanutar.icimdekiler.ui.TuketimKaydi
import com.furkanutar.icimdekiler.ui.theme.IcimdekilerTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import java.time.LocalDate

class gunlukTakipFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private val kayitlarListesi = mutableStateListOf<TuketimKaydi>()
    private var gunlukHedef = mutableStateOf(GunlukHedef())
    private var seciliTarih = mutableStateOf(LocalDate.now())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                IcimdekilerTheme {
                    GunlukTakipScreen(
                        tarih = seciliTarih.value,
                        hedef = gunlukHedef.value,
                        kayitlar = kayitlarListesi,
                        onTarihSecildi = { yeniTarih ->
                            seciliTarih.value = yeniTarih
                            verileriYukle(yeniTarih)
                        },
                        onHedefGuncelle = { yeniHedef ->
                            gunlukHedef.value = yeniHedef
                            hedefiFirestoreaKaydet(yeniHedef)
                        },
                        onBackClick = {
                            findNavController().popBackStack()
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verileriYukle(seciliTarih.value)
    }

    private fun verileriYukle(tarih: LocalDate = seciliTarih.value) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), getString(R.string.buEkranIcinGirisYapmalisiniz), Toast.LENGTH_SHORT).show()
            return
        }

        val uid = user.uid
        val seciliGun = tarih.toString()

        // Önce seçili günün özel hedefini kontrol et
        db.collection("users").document(uid).collection("gunlukKayitlar").document(seciliGun)
            .get()
            .addOnSuccessListener { dayDoc ->
                if (dayDoc.exists() && dayDoc.contains("hedefKalori")) {
                    val k = dayDoc.getLong("hedefKalori")?.toInt() ?: 2000
                    val p = dayDoc.getLong("hedefProtein")?.toInt() ?: 150
                    val karb = dayDoc.getLong("hedefKarbonhidrat")?.toInt() ?: 200
                    val y = dayDoc.getLong("hedefYag")?.toInt() ?: 60
                    gunlukHedef.value = GunlukHedef(k, p, karb, y)
                } else {
                    // Özel hedef yoksa genel (global) hedefi çek
                    db.collection("users").document(uid).collection("hedefler").document("gunluk")
                        .get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                val k = doc.getLong("kalori")?.toInt() ?: 2000
                                val p = doc.getLong("protein")?.toInt() ?: 150
                                val karb = doc.getLong("karbonhidrat")?.toInt() ?: 200
                                val y = doc.getLong("yag")?.toInt() ?: 60
                                gunlukHedef.value = GunlukHedef(k, p, karb, y)
                            }
                        }
                }
            }
            .addOnFailureListener {
                Log.e("GunlukTakip", "Günlük hedef kontrol edilirken hata", it)
            }

        // Günlük Kayıtları Çek
        db.collection("users").document(uid).collection("gunlukKayitlar").document(seciliGun).collection("urunler")
            .get()
            .addOnSuccessListener { snapshot ->
                kayitlarListesi.clear()
                for (doc in snapshot.documents) {
                    val ad = doc.getString("urunAdi") ?: "Bilinmeyen"
                    val k = doc.getLong("kalori")?.toInt() ?: 0
                    val p = doc.getDouble("protein")?.toFloat() ?: 0f
                    val karb = doc.getDouble("karbonhidrat")?.toFloat() ?: 0f
                    val y = doc.getDouble("yag")?.toFloat() ?: 0f
                    val m = doc.getLong("miktarGr")?.toInt() ?: 100

                    kayitlarListesi.add(TuketimKaydi(ad, k, p, karb, y, m))
                }
            }
            .addOnFailureListener {
                Log.e("GunlukTakip", "Kayıtlar çekilirken hata", it)
            }
    }

    private fun hedefiFirestoreaKaydet(hedef: GunlukHedef) {
        val user = auth.currentUser ?: return
        val seciliGun = seciliTarih.value.toString()

        // 1. O güne özel hedefi kaydet (Geçmiş günlerin bozulmaması için)
        val gunlukHedefMap = hashMapOf(
            "hedefKalori" to hedef.kalori,
            "hedefProtein" to hedef.protein,
            "hedefKarbonhidrat" to hedef.karbonhidrat,
            "hedefYag" to hedef.yag
        )
        
        db.collection("users").document(user.uid).collection("gunlukKayitlar").document(seciliGun)
            .set(gunlukHedefMap, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), getString(R.string.hedeflerKaydedildi), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), getString(R.string.hedeflerKaydedilemedi), Toast.LENGTH_SHORT).show()
            }

        // 2. Eğer bugün veya gelecekteki bir günse, bunu GLOBAL hedef olarak da güncelle
        if (!seciliTarih.value.isBefore(LocalDate.now())) {
            val globalHedefMap = hashMapOf(
                "kalori" to hedef.kalori,
                "protein" to hedef.protein,
                "karbonhidrat" to hedef.karbonhidrat,
                "yag" to hedef.yag
            )
            db.collection("users").document(user.uid).collection("hedefler").document("gunluk")
                .set(globalHedefMap, SetOptions.merge())
        }
    }
}
