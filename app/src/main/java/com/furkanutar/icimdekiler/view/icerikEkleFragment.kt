package com.furkanutar.icimdekiler.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.model.Icerikler
import com.furkanutar.icimdekiler.ui.IcerikEkleScreen
import com.furkanutar.icimdekiler.ui.OzelAlertDialog
import com.furkanutar.icimdekiler.ui.theme.IcimdekilerTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class icerikEkleFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    // Compose'un listeyi takip edebilmesi için State nesnesi
    private var icerikListesiState = mutableStateOf<List<Icerikler>>(emptyList())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Ekran açıldığında verileri çekmeye başla
        icerikleriAl()

        return ComposeView(requireContext()).apply {
            setContent {
                IcimdekilerTheme {
                    var showDialog by remember { mutableStateOf(false) }
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    var geciciUrunAdi by remember { mutableStateOf("") }
                    var geciciAciklama by remember { mutableStateOf("") }
                    var silinecekIcerikId by remember { mutableStateOf("") }

                    // Hazırladığımız Alternatif 2 UI'ını çağırıyoruz
                    IcerikEkleScreen(
                        mevcutIcerikler = icerikListesiState.value,
                        onKaydetClick = { urunAdi, aciklama ->
                            geciciUrunAdi = urunAdi
                            geciciAciklama = aciklama
                            showDialog = true
                        },
                        onSilClick = { id ->
                            silinecekIcerikId = id
                            showDeleteDialog = true
                        }
                    )

                    // Kayıt Onay Dialogu
                    if (showDialog) {
                        OzelAlertDialog(
                            baslik = stringResource(R.string.kayitEtmekIstediginizdenEminMisiniz),
                            onDismiss = { showDialog = false },
                            onConfirm = {
                                showDialog = false
                                icerikEkle(geciciUrunAdi, geciciAciklama)
                            },
                            onayButonMetni = stringResource(R.string.evet),
                            iptalButonMetni = stringResource(R.string.hayir),
                            onayButonRengi = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Silme Onay Dialogu
                    if (showDeleteDialog) {
                        OzelAlertDialog(
                            baslik = "Bu içeriği silmek istediğinizden emin misiniz?",
                            onDismiss = { showDeleteDialog = false },
                            onConfirm = {
                                showDeleteDialog = false
                                icerikSil(silinecekIcerikId)
                            },
                            onayButonMetni = stringResource(R.string.evet),
                            iptalButonMetni = stringResource(R.string.hayir),
                            onayButonRengi = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    private fun icerikleriAl() {
        db.collection("icerik")
            .orderBy("urun", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("FirestoreError", error.localizedMessage ?: "Hata")
                    return@addSnapshotListener
                }

                value?.let { snapshot ->
                    val liste = snapshot.documents.mapNotNull { doc ->
                        val icerikAdi = doc.getString("urun") ?: ""
                        val aciklama = doc.getString("aciklama") ?: ""
                        Icerikler(doc.id, icerikAdi, aciklama)
                    }
                    // State'i güncelle (Bu sayede liste otomatik yenilenir)
                    icerikListesiState.value = liste
                }
            }
    }

    fun icerikEkle(urunAdi: String, aciklama: String) {
        val urun = urunAdi.trim()
        val aciklamaTrim = aciklama.trim()

        if (urun.isEmpty() || aciklamaTrim.isEmpty()) {
            Toast.makeText(requireContext(), R.string.lutfenBosAlanBirakmayiniz, Toast.LENGTH_SHORT).show()
            return
        }

        val icerikMap = hashMapOf<String, Any>(
            "urun" to urun,
            "aciklama" to aciklamaTrim
        )

        db.collection("icerik")
            .whereEqualTo("urun", urun)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    db.collection("icerik").add(icerikMap)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), R.string.urunBasariylaKayitEdildi, Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), R.string.urunDahaOnceKayitEdilmis, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun icerikSil(id: String) {
        db.collection("icerik").document(id).delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "İçerik silindi.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Silme hatası: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}