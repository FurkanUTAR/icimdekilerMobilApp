package com.furkanutar.icimdekiler.view

import android.app.AlertDialog
import android.os.Bundle
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
import com.furkanutar.icimdekiler.databinding.FragmentIcerikEkleBinding
import com.furkanutar.icimdekiler.ui.IcerikEkleScreen
import com.furkanutar.icimdekiler.ui.OzelAlertDialog
import com.furkanutar.icimdekiler.ui.theme.IcimdekilerTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.set

class icerikEkleFragment : Fragment() {

    //Firebase
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply{
            setContent {
                IcimdekilerTheme {

                    var showDialog by remember { mutableStateOf(false) }
                    var geciciUrunAdi by remember { mutableStateOf("") }
                    var geciciAciklama by remember { mutableStateOf("") }


                    IcerikEkleScreen { urunAdi, aciklama ->
                        geciciUrunAdi = urunAdi
                        geciciAciklama = aciklama
                        showDialog = true
                    }

                    if (showDialog){
                        OzelAlertDialog(
                            baslik = stringResource(R.string.kayitEtmekIstediginizdenEminMisiniz),
                            onDismiss = { showDialog = false },
                            onConfirm = {
                                showDialog = false
                                icerikEkle(geciciUrunAdi,geciciAciklama)
                            },
                            onayButonMetni = stringResource(R.string.evet),
                            iptalButonMetni = stringResource(R.string.hayir),
                            onayButonRengi = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    private fun icerikEkle(urunAdi: String, aciklama: String) {
        val urun = urunAdi.trim()
        val aciklama = aciklama.trim()

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
}