package com.example.icimdekiler

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.Navigator
import com.example.icimdekiler.databinding.FragmentGirisYapBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlin.concurrent.timerTask

class girisYapFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentGirisYapBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGirisYapBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.kayitOlLabel.setOnClickListener {
            val action = girisYapFragmentDirections.actionGirisYapFragmentToKayitOlFragment()
            Navigation.findNavController(view).navigate(action)
        }

        binding.girisYapButton.setOnClickListener {
            girisYap()

            if(binding.beniHatirlaCheckBox.isChecked){
                val guncelKullanici=auth.currentUser
                if(guncelKullanici != null){
                    Toast.makeText(requireContext(), "Hatırladım", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun girisYap(){
        val kullaniciAdi=binding.kullaniciAdiText.text.toString()
        val ePosta=binding.ePostaText.text.toString()
        val parola=binding.parolaText.text.toString()

        if(!ePosta.isNullOrEmpty() && !parola.isNullOrEmpty()){
            auth.signInWithEmailAndPassword(ePosta,parola).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(requireContext(), "Hoşgeldin Kullanıcı", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exeption ->
                Toast.makeText(requireContext(), exeption.localizedMessage, Toast.LENGTH_LONG).show()
            }
        } else{
            Toast.makeText(requireContext(), "Lütfen boş alan bırakmayınız!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}