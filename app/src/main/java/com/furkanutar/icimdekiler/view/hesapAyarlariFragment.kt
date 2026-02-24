package com.furkanutar.icimdekiler.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.furkanutar.icimdekiler.R
import com.furkanutar.icimdekiler.ui.HesapAyarlariScreen
import com.furkanutar.icimdekiler.ui.theme.IcimdekilerTheme
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class hesapAyarlariFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    // State'ler (Veriler burada tutuluyor)
    private var kullaniciAdi by mutableStateOf("")
    private var isimSoyisim by mutableStateOf("")
    private var ePosta by mutableStateOf("")
    private var telNo by mutableStateOf("")
    private var belgeId by mutableStateOf("") // Firestore belge ID'sini saklamak için

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        mevcutBilgileriGetir() // Sayfa açılınca verileri çek
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                IcimdekilerTheme {
                    HesapAyarlariScreen(
                        kullaniciAdi = kullaniciAdi,
                        isimSoyisim = isimSoyisim,
                        ePosta = ePosta,
                        telNo = telNo,
                        onKullaniciAdiChange = { kullaniciAdi = it },
                        onIsimSoyisimChange = { isimSoyisim = it },
                        onEPostaChange = { ePosta = it },
                        onTelNoChange = { telNo = it },
                        onParolaDegistirClick = {
                            showConfirmationDialog(getString(R.string.parolayiDegistir)) { parolaGuncelle() }
                        },
                        onGuncelleClick = { verileriGuncelle() },
                        onHesabiSilClick = {
                            showConfirmationDialog(getString(R.string.silmekIstediginizdenEminMisiniz)) { hesabiSil() }
                        }
                    )
                }
            }
        }
    }

    private fun mevcutBilgileriGetir() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("kullaniciBilgileri")
            .whereEqualTo("kullaniciUID", uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents.first()
                    belgeId = doc.id
                    kullaniciAdi = doc.getString("kullaniciAdi") ?: ""
                    isimSoyisim = doc.getString("isimSoyisim") ?: ""
                    ePosta = doc.getString("ePosta") ?: ""
                    telNo = doc.getString("telNo") ?: ""
                }
            }
    }

    private fun showConfirmationDialog(title: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setPositiveButton(R.string.evet) { _, _ -> onConfirm() }
            .setNegativeButton(R.string.hayir, null)
            .show()
    }

    private fun verileriGuncelle() {
        if (kullaniciAdi.isBlank() || isimSoyisim.isBlank() || ePosta.isBlank() || telNo.isBlank()) {
            Toast.makeText(requireContext(), R.string.lutfenBosAlanBirakmayiniz, Toast.LENGTH_SHORT).show()
            return
        }

        if (belgeId.isNotEmpty()) {
            val updateMap = mapOf(
                "kullaniciAdi" to kullaniciAdi,
                "isimSoyisim" to isimSoyisim,
                "ePosta" to ePosta,
                "telNo" to telNo
            )

            db.collection("kullaniciBilgileri").document(belgeId)
                .update(updateMap)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), R.string.urunBasariylaGuncellendi, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Diğer fonksiyonlar (parolaGuncelle, hesabiSil vb.) mantık olarak doğru,
    // ancak dialog içindeki "eski parola" kontrolünü Firebase Re-authentication ile yapmak daha güvenlidir.
    // Mevcut showPasswordDialog metodunuzu kullanmaya devam edebilirsiniz.

    private fun parolaGuncelle() {
        val currentUser = auth.currentUser ?: return
        db.collection("kullaniciBilgileri").document(belgeId).get()
            .addOnSuccessListener { doc ->
                val eskiParola = doc.getString("parola") ?: ""
                showPasswordDialog(currentUser, eskiParola, currentUser.uid)
            }
    }

    private fun showPasswordDialog(currentUser: FirebaseUser, eskiParola: String, kullaniciUID: String) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_change_password, null)

        val guncelParolaEditText = view.findViewById<EditText>(R.id.guncelParolaText)
        val yeniParolaEditText = view.findViewById<EditText>(R.id.yeniParolaText)
        val parolaDogrulaEditText = view.findViewById<EditText>(R.id.parolaDogrulaText)
        val guncelleButton = view.findViewById<Button>(R.id.parolaGuncelleButton)

        guncelleButton.isEnabled = false

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val eslesme = guncelParolaEditText.text.toString() == eskiParola
                yeniParolaEditText.isEnabled = eslesme
                parolaDogrulaEditText.isEnabled = eslesme
                guncelleButton.isEnabled = eslesme && yeniParolaEditText.text.isNotEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        guncelParolaEditText.addTextChangedListener(watcher)

        guncelleButton.setOnClickListener {
            val yeni = yeniParolaEditText.text.toString()
            if (yeni == parolaDogrulaEditText.text.toString()) {
                currentUser.updatePassword(yeni).addOnSuccessListener {
                    db.collection("kullaniciBilgileri").document(belgeId)
                        .update("parola", yeni)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), R.string.parolabaşarıylagüncellendi, Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                }
            }
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun hesabiSil() {
        val user = auth.currentUser ?: return
        db.collection("kullaniciBilgileri").document(belgeId).delete().addOnSuccessListener {
            user.delete().addOnSuccessListener {
                val action = hesapAyarlariFragmentDirections.actionHesapAyarlariFragmentToSplashFragment()
                requireView().findNavController().navigate(action)
            }
        }
    }
}