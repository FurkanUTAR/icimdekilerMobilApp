
package com.furkanutar.icimdekiler.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.model.Bildiri
import com.furkanutar.icimdekiler.ui.BildiriKarti
import com.furkanutar.icimdekiler.ui.BildiriListesi
import com.furkanutar.icimdekiler.ui.theme.IcimdekilerTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class bildiriFragment : Fragment() {


    private val db = FirebaseFirestore.getInstance()
    private val bildiriListesi = mutableStateOf<List<Bildiri>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bildirileriAl()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                IcimdekilerTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        BildiriListesi(bildiriListesi.value)
                    }
                }
            }
        }
    }

    fun bildirileriAl(){
        db.collection("bildiriler")
            .orderBy("zaman", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null){
                    Log.e("Bildiri_Hata", "Veri çekilemedi: ${error.message}")
                    return@addSnapshotListener
                }
                if (value != null) {
                    val yeniListe = value.toObjects(Bildiri::class.java)
                    bildiriListesi.value = yeniListe
                }
            }
    }
}